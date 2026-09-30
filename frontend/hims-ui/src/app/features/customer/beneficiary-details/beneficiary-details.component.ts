import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormGroupDirective, Validators } from '@angular/forms';
import { CustomerService } from '../../../core/services/customer.service';
import { CustomerContextService } from '../../../core/services/customer-context.service';
import { NotificationService } from '../../../core/services/notification.service';
import { CustomerResponse, BeneficiaryResponse, RELATIONSHIPS } from '../../../core/models/customer.model';

@Component({
  selector: 'app-beneficiary-details',
  templateUrl: './beneficiary-details.component.html',
  styleUrls: ['./beneficiary-details.component.scss']
})
export class BeneficiaryDetailsComponent implements OnInit {
  beneficiaryForm!: FormGroup;
  customer: CustomerResponse | null = null;
  beneficiaries: BeneficiaryResponse[] = [];
  isLoading = false;
  isSubmitting = false;
  isEditing = false;
  editingBeneficiaryId: string | null = null;
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
        this.loadBeneficiaries(c.customerId);
      } else {
        this.beneficiaries = [];
      }
    });
  }

  private initForm(): void {
    this.beneficiaryForm = this.fb.group({
      name: ['', [Validators.required]],
      allocationPercentage: [100, [Validators.required, Validators.min(1), Validators.max(100)]],
      relationship: ['SPOUSE', [Validators.required]]
    });
  }

  get nameControl() { return this.beneficiaryForm.get('name'); }
  get allocationControl() { return this.beneficiaryForm.get('allocationPercentage'); }
  get relationshipControl() { return this.beneficiaryForm.get('relationship'); }

  get totalAllocation(): number {
    return this.beneficiaries.reduce((sum, b) => sum + (Number(b.allocationPercentage) || 0), 0);
  }

  loadBeneficiaries(customerId: string): void {
    this.isLoading = true;
    this.customerService.getBeneficiaries(customerId).subscribe({
      next: (list) => {
        this.isLoading = false;
        this.beneficiaries = list || [];
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  startEdit(b: BeneficiaryResponse): void {
    this.isEditing = true;
    this.editingBeneficiaryId = b.beneficiaryId;
    this.beneficiaryForm.patchValue({
      name: b.name,
      allocationPercentage: b.allocationPercentage,
      relationship: b.relationship
    });
  }

  cancelEdit(formDirective?: FormGroupDirective): void {
    this.isEditing = false;
    this.editingBeneficiaryId = null;
    if (formDirective) {
      formDirective.resetForm({
        allocationPercentage: 100,
        relationship: 'SPOUSE'
      });
    } else {
      this.beneficiaryForm.reset({
        allocationPercentage: 100,
        relationship: 'SPOUSE'
      });
    }
  }

  onSubmit(formDirective?: FormGroupDirective): void {
    if (!this.customer) {
      this.notificationService.error('Please select an active customer first.');
      return;
    }

    if (this.beneficiaryForm.invalid || this.isSubmitting) {
      this.beneficiaryForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    const req = this.beneficiaryForm.value;

    if (this.isEditing && this.editingBeneficiaryId) {
      this.customerService.updateBeneficiary(this.customer.customerId, this.editingBeneficiaryId, req).subscribe({
        next: (updated) => {
          this.isSubmitting = false;
          this.notificationService.success(`Beneficiary ${updated.name} updated successfully.`);
          this.cancelEdit(formDirective);
          if (this.customer) this.loadBeneficiaries(this.customer.customerId);
        },
        error: () => {
          this.isSubmitting = false;
        }
      });
    } else {
      this.customerService.addBeneficiary(this.customer.customerId, req).subscribe({
        next: (created) => {
          this.isSubmitting = false;
          this.notificationService.success(`Beneficiary ${created.name} added successfully.`);
          if (formDirective) {
            formDirective.resetForm({
              allocationPercentage: 100,
              relationship: 'SPOUSE'
            });
          } else {
            this.beneficiaryForm.reset({
              allocationPercentage: 100,
              relationship: 'SPOUSE'
            });
          }
          if (this.customer) this.loadBeneficiaries(this.customer.customerId);
        },
        error: () => {
          this.isSubmitting = false;
        }
      });
    }
  }

  deleteBeneficiary(b: BeneficiaryResponse): void {
    if (!this.customer) return;
    const confirmed = confirm(`Are you sure you want to remove beneficiary ${b.name}?`);
    if (!confirmed) return;

    this.customerService.removeBeneficiary(this.customer.customerId, b.beneficiaryId).subscribe({
      next: () => {
        this.notificationService.info(`Beneficiary ${b.name} removed.`);
        if (this.customer) this.loadBeneficiaries(this.customer.customerId);
      }
    });
  }
}
