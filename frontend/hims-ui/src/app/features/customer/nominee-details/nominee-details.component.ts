import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormGroupDirective, Validators } from '@angular/forms';
import { CustomerService } from '../../../core/services/customer.service';
import { CustomerContextService } from '../../../core/services/customer-context.service';
import { NotificationService } from '../../../core/services/notification.service';
import { CustomerResponse, NomineeResponse, RELATIONSHIPS } from '../../../core/models/customer.model';

@Component({
  selector: 'app-nominee-details',
  templateUrl: './nominee-details.component.html',
  styleUrls: ['./nominee-details.component.scss']
})
export class NomineeDetailsComponent implements OnInit {
  nomineeForm!: FormGroup;
  customer: CustomerResponse | null = null;
  nominees: NomineeResponse[] = [];
  isLoading = false;
  isSubmitting = false;
  isEditing = false;
  editingNomineeId: string | null = null;
  relationships = RELATIONSHIPS;

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
        this.loadNominees(c.customerId);
      } else {
        this.nominees = [];
      }
    });
  }

  private initForm(): void {
    this.nomineeForm = this.fb.group({
      name: ['', [Validators.required]],
      relationship: ['SPOUSE', [Validators.required]],
      dateOfBirth: ['', [Validators.required]]
    });
  }

  get nameControl() { return this.nomineeForm.get('name'); }
  get relationshipControl() { return this.nomineeForm.get('relationship'); }
  get dobControl() { return this.nomineeForm.get('dateOfBirth'); }

  loadNominees(customerId: string): void {
    this.isLoading = true;
    this.customerService.getNominees(customerId).subscribe({
      next: (list) => {
        this.isLoading = false;
        this.nominees = list || [];
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  startEdit(n: NomineeResponse): void {
    this.isEditing = true;
    this.editingNomineeId = n.nomineeId;
    this.nomineeForm.patchValue({
      name: n.name,
      relationship: n.relationship,
      dateOfBirth: n.dateOfBirth
    });
  }

  cancelEdit(formDirective?: FormGroupDirective): void {
    this.isEditing = false;
    this.editingNomineeId = null;
    if (formDirective) {
      formDirective.resetForm({
        relationship: 'SPOUSE'
      });
    } else {
      this.nomineeForm.reset({
        relationship: 'SPOUSE'
      });
    }
  }

  onSubmit(formDirective?: FormGroupDirective): void {
    if (!this.customer) {
      this.notificationService.error('Please select an active customer first.');
      return;
    }

    if (this.nomineeForm.invalid || this.isSubmitting) {
      this.nomineeForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    const req = this.nomineeForm.value;

    if (this.isEditing && this.editingNomineeId) {
      this.customerService.updateNominee(this.customer.customerId, this.editingNomineeId, req).subscribe({
        next: (updated) => {
          this.isSubmitting = false;
          this.notificationService.success(`Nominee ${updated.name} updated successfully.`);
          this.cancelEdit(formDirective);
          if (this.customer) this.loadNominees(this.customer.customerId);
        },
        error: () => {
          this.isSubmitting = false;
        }
      });
    } else {
      this.customerService.addNominee(this.customer.customerId, req).subscribe({
        next: (created) => {
          this.isSubmitting = false;
          this.notificationService.success(`Nominee ${created.name} added successfully.`);
          if (formDirective) {
            formDirective.resetForm({
              relationship: 'SPOUSE'
            });
          } else {
            this.nomineeForm.reset({
              relationship: 'SPOUSE'
            });
          }
          if (this.customer) this.loadNominees(this.customer.customerId);
        },
        error: () => {
          this.isSubmitting = false;
        }
      });
    }
  }

  deleteNominee(n: NomineeResponse): void {
    if (!this.customer) return;
    const confirmed = confirm(`Are you sure you want to remove nominee ${n.name}?`);
    if (!confirmed) return;

    this.customerService.removeNominee(this.customer.customerId, n.nomineeId).subscribe({
      next: () => {
        this.notificationService.info(`Nominee ${n.name} removed.`);
        if (this.customer) this.loadNominees(this.customer.customerId);
      }
    });
  }
}
