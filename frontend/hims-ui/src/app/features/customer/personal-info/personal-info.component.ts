import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, FormGroupDirective, Validators } from '@angular/forms';
import { Subscription } from 'rxjs';
import { CustomerService } from '../../../core/services/customer.service';
import { CustomerContextService } from '../../../core/services/customer-context.service';
import { NotificationService } from '../../../core/services/notification.service';
import { CustomerResponse, GENDERS } from '../../../core/models/customer.model';

export interface FeedbackModal {
  type: 'success' | 'error';
  title: string;
  message: string;
  customerId?: string;
  customerName?: string;
  copySuccess?: boolean;
}

@Component({
  selector: 'app-personal-info',
  templateUrl: './personal-info.component.html',
  styleUrls: ['./personal-info.component.scss']
})
export class PersonalInfoComponent implements OnInit, OnDestroy {
  customerForm!: FormGroup;
  searchId = '';
  isSearching = false;
  isRefreshing = false;
  isSubmitting = false;
  isEditMode = false;
  currentCustomer: CustomerResponse | null = null;
  genders = GENDERS;
  modalData: FeedbackModal | null = null;
  private sub!: Subscription;

  constructor(
    private fb: FormBuilder,
    private customerService: CustomerService,
    private customerContextService: CustomerContextService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.initForm();

    // Subscribe to active customer changes so navigating between tabs always stays synchronized
    this.sub = this.customerContextService.activeCustomer$.subscribe((cust) => {
      this.currentCustomer = cust;
      this.searchId = cust ? cust.customerId : '';
    });

    // Auto-refresh fresh customer details from server on entry if customer is active
    if (this.currentCustomer) {
      this.refreshCustomer(false);
    }
  }

  ngOnDestroy(): void {
    if (this.sub) {
      this.sub.unsubscribe();
    }
  }

  private initForm(): void {
    this.customerForm = this.fb.group({
      firstName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50), Validators.pattern(/^[a-zA-Z\s'-]+$/)]],
      lastName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50), Validators.pattern(/^[a-zA-Z\s'-]+$/)]],
      dateOfBirth: ['', [Validators.required]],
      gender: ['MALE', [Validators.required]],
      identificationNumber: ['', [Validators.required, Validators.minLength(4), Validators.maxLength(30), Validators.pattern(/^[A-Za-z0-9\-_]+$/)]]
    });
  }

  get firstNameControl() { return this.customerForm.get('firstName'); }
  get lastNameControl() { return this.customerForm.get('lastName'); }
  get dobControl() { return this.customerForm.get('dateOfBirth'); }
  get genderControl() { return this.customerForm.get('gender'); }
  get idNumberControl() { return this.customerForm.get('identificationNumber'); }

  onSearch(): void {
    const id = this.searchId.trim();
    if (!id) {
      this.modalData = {
        type: 'error',
        title: 'Search Error',
        message: 'Please enter a valid Customer ID (UUID) to lookup.'
      };
      return;
    }

    this.isSearching = true;
    this.customerService.getCustomer(id).subscribe({
      next: (cust) => {
        this.isSearching = false;
        this.currentCustomer = cust;
        this.customerContextService.setActiveCustomer(cust);
        this.notificationService.success(`Loaded customer: ${cust.firstName} ${cust.lastName}`);
      },
      error: (err) => {
        this.isSearching = false;
        this.modalData = {
          type: 'error',
          title: 'Customer Not Found',
          message: err?.error?.message || `No customer found with ID '${id}'. Please verify and try again.`
        };
      }
    });
  }

  /**
   * Re-fetches the latest customer record from the backend database
   */
  refreshCustomer(showToast = true): void {
    if (!this.currentCustomer) return;

    this.isRefreshing = true;
    this.customerService.getCustomer(this.currentCustomer.customerId).subscribe({
      next: (cust) => {
        this.isRefreshing = false;
        this.currentCustomer = cust;
        this.customerContextService.setActiveCustomer(cust);
        if (showToast) {
          this.notificationService.success('Customer profile refreshed from server.');
        }
      },
      error: () => {
        this.isRefreshing = false;
      }
    });
  }

  /**
   * Clears the current active customer to start a fresh enrollment or search another ID
   */
  clearCustomer(formDirective?: FormGroupDirective): void {
    this.customerContextService.setActiveCustomer(null);
    this.currentCustomer = null;
    this.searchId = '';
    this.isEditMode = false;
    if (formDirective) {
      formDirective.resetForm({ gender: 'MALE' });
    } else {
      this.customerForm.reset({ gender: 'MALE' });
    }
    this.notificationService.info('Customer selection cleared. Ready to create a new customer or search another ID.');
  }

  startEdit(): void {
    if (!this.currentCustomer) return;
    this.isEditMode = true;
    this.customerForm.patchValue({
      firstName: this.currentCustomer.firstName,
      lastName: this.currentCustomer.lastName,
      dateOfBirth: this.currentCustomer.dateOfBirth,
      gender: this.currentCustomer.gender,
      identificationNumber: this.currentCustomer.identificationNumber
    });
  }

  cancelEdit(formDirective?: FormGroupDirective): void {
    this.isEditMode = false;
    if (formDirective) {
      formDirective.resetForm({ gender: 'MALE' });
    } else {
      this.customerForm.reset({ gender: 'MALE' });
    }
  }

  onSubmit(formDirective?: FormGroupDirective): void {
    if (this.customerForm.invalid || this.isSubmitting) {
      this.customerForm.markAllAsTouched();
      this.modalData = {
        type: 'error',
        title: 'Incomplete Details',
        message: 'Please fill in all required customer fields (First Name, Last Name, Date of Birth, and Identification Number) before submitting.'
      };
      return;
    }

    this.isSubmitting = true;
    const req = this.customerForm.value;

    if (this.isEditMode && this.currentCustomer) {
      this.customerService.updateCustomer(this.currentCustomer.customerId, req).subscribe({
        next: (updated) => {
          this.isSubmitting = false;
          this.isEditMode = false;
          this.currentCustomer = updated;
          this.customerContextService.setActiveCustomer(updated);
          this.modalData = {
            type: 'success',
            title: 'Customer Updated Successfully!',
            message: `Profile details for ${updated.firstName} ${updated.lastName} have been successfully updated in the system.`,
            customerId: updated.customerId,
            customerName: `${updated.firstName} ${updated.lastName}`
          };
          if (formDirective) {
            formDirective.resetForm({ gender: 'MALE' });
          } else {
            this.customerForm.reset({ gender: 'MALE' });
          }
        },
        error: (err) => {
          this.isSubmitting = false;
          this.modalData = {
            type: 'error',
            title: 'Update Error',
            message: err?.error?.message || 'Failed to update customer details. Please check connection and try again.'
          };
        }
      });
    } else {
      this.customerService.createCustomer(req).subscribe({
        next: (created) => {
          this.isSubmitting = false;
          this.currentCustomer = created;
          this.searchId = created.customerId;
          this.customerContextService.setActiveCustomer(created);

          // Centered popup modal with Customer ID and cross close mark
          this.modalData = {
            type: 'success',
            title: 'Customer Registered Successfully!',
            message: `Customer ${created.firstName} ${created.lastName} has been enrolled into HIMS.`,
            customerId: created.customerId,
            customerName: `${created.firstName} ${created.lastName}`
          };

          // Clean formDirective reset: clears submitted state so fields DO NOT turn red!
          if (formDirective) {
            formDirective.resetForm({ gender: 'MALE' });
          } else {
            this.customerForm.reset({ gender: 'MALE' });
          }
        },
        error: (err) => {
          this.isSubmitting = false;
          this.modalData = {
            type: 'error',
            title: 'Registration Error',
            message: err?.error?.message || 'Failed to register customer. Please verify that Identification Number is unique.'
          };
        }
      });
    }
  }

  resetForm(formDirective?: FormGroupDirective): void {
    this.isEditMode = false;
    if (formDirective) {
      formDirective.resetForm({ gender: 'MALE' });
    } else {
      this.customerForm.reset({ gender: 'MALE' });
    }
  }

  closeModal(): void {
    this.modalData = null;
  }

  copyCustomerId(): void {
    if (this.modalData?.customerId) {
      navigator.clipboard.writeText(this.modalData.customerId).then(() => {
        if (this.modalData) {
          this.modalData.copySuccess = true;
          setTimeout(() => {
            if (this.modalData) this.modalData.copySuccess = false;
          }, 2000);
        }
      });
    }
  }
}
