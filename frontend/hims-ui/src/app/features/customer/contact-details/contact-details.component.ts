import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormGroupDirective, Validators } from '@angular/forms';
import { CustomerService } from '../../../core/services/customer.service';
import { CustomerContextService } from '../../../core/services/customer-context.service';
import { NotificationService } from '../../../core/services/notification.service';
import { CustomerResponse, ContactResponse, CONTACT_TYPES } from '../../../core/models/customer.model';

@Component({
  selector: 'app-contact-details',
  templateUrl: './contact-details.component.html',
  styleUrls: ['./contact-details.component.scss']
})
export class ContactDetailsComponent implements OnInit {
  contactForm!: FormGroup;
  customer: CustomerResponse | null = null;
  contacts: ContactResponse[] = [];
  isLoading = false;
  isSubmitting = false;
  isEditing = false;
  editingContactId: string | null = null;
  contactTypes = CONTACT_TYPES;

  constructor(
    private fb: FormBuilder,
    private customerService: CustomerService,
    private customerContextService: CustomerContextService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.customerContextService.activeCustomer$.subscribe((c) => {
      this.customer = c;
      if (c) {
        this.loadContacts(c.customerId);
      } else {
        this.contacts = [];
      }
    });
  }

  private initForm(): void {
    this.contactForm = this.fb.group({
      contactType: ['MOBILE', [Validators.required]],
      contactValue: ['', [Validators.required]],
      isPrimary: [false]
    });
  }

  get contactTypeControl() { return this.contactForm.get('contactType'); }
  get contactValueControl() { return this.contactForm.get('contactValue'); }

  loadContacts(customerId: string): void {
    this.isLoading = true;
    this.customerService.getContacts(customerId).subscribe({
      next: (list) => {
        this.isLoading = false;
        this.contacts = list || [];
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  startEdit(contact: ContactResponse): void {
    this.isEditing = true;
    this.editingContactId = contact.contactId;
    this.contactForm.patchValue({
      contactType: contact.contactType,
      contactValue: contact.contactValue,
      isPrimary: contact.isPrimary
    });
  }

  cancelEdit(formDirective?: FormGroupDirective): void {
    this.isEditing = false;
    this.editingContactId = null;
    if (formDirective) {
      formDirective.resetForm({
        contactType: 'MOBILE',
        isPrimary: false
      });
    } else {
      this.contactForm.reset({
        contactType: 'MOBILE',
        isPrimary: false
      });
    }
  }

  onSubmit(formDirective?: FormGroupDirective): void {
    if (!this.customer) {
      this.notificationService.error('Please select an active customer first.');
      return;
    }

    if (this.contactForm.invalid || this.isSubmitting) {
      this.contactForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    const req = this.contactForm.value;

    if (this.isEditing && this.editingContactId) {
      this.customerService.updateContact(this.customer.customerId, this.editingContactId, req).subscribe({
        next: (updated) => {
          this.isSubmitting = false;
          this.notificationService.success(`Contact (${updated.contactType}) updated successfully.`);
          this.cancelEdit(formDirective);
          if (this.customer) this.loadContacts(this.customer.customerId);
        },
        error: () => {
          this.isSubmitting = false;
        }
      });
    } else {
      this.customerService.addContact(this.customer.customerId, req).subscribe({
        next: (created) => {
          this.isSubmitting = false;
          this.notificationService.success(`Contact (${created.contactType}) added successfully.`);
          if (formDirective) {
            formDirective.resetForm({
              contactType: 'MOBILE',
              isPrimary: false
            });
          } else {
            this.contactForm.reset({
              contactType: 'MOBILE',
              isPrimary: false
            });
          }
          if (this.customer) this.loadContacts(this.customer.customerId);
        },
        error: () => {
          this.isSubmitting = false;
        }
      });
    }
  }

  deleteContact(contact: ContactResponse): void {
    if (!this.customer) return;
    const confirmed = confirm(`Are you sure you want to remove contact ${contact.contactValue}?`);
    if (!confirmed) return;

    this.customerService.removeContact(this.customer.customerId, contact.contactId).subscribe({
      next: () => {
        this.notificationService.info(`Contact ${contact.contactValue} removed.`);
        if (this.customer) this.loadContacts(this.customer.customerId);
      }
    });
  }
}
