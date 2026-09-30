import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormGroupDirective, Validators } from '@angular/forms';
import { CustomerService } from '../../../core/services/customer.service';
import { CustomerContextService } from '../../../core/services/customer-context.service';
import { NotificationService } from '../../../core/services/notification.service';
import { CustomerResponse, MemberResponse, GENDERS, RELATIONSHIPS } from '../../../core/models/customer.model';

@Component({
  selector: 'app-member-details',
  templateUrl: './member-details.component.html',
  styleUrls: ['./member-details.component.scss']
})
export class MemberDetailsComponent implements OnInit {
  memberForm!: FormGroup;
  customer: CustomerResponse | null = null;
  members: MemberResponse[] = [];
  isLoading = false;
  isSubmitting = false;
  isEditing = false;
  editingMemberId: string | null = null;
  genders = GENDERS;
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
        this.loadMembers(c.customerId);
      } else {
        this.members = [];
      }
    });
  }

  private initForm(): void {
    this.memberForm = this.fb.group({
      firstName: ['', [Validators.required]],
      lastName: ['', [Validators.required]],
      dateOfBirth: ['', [Validators.required]],
      relationshipToCustomer: ['SPOUSE', [Validators.required]],
      gender: ['MALE', [Validators.required]]
    });
  }

  get firstNameControl() { return this.memberForm.get('firstName'); }
  get lastNameControl() { return this.memberForm.get('lastName'); }
  get dobControl() { return this.memberForm.get('dateOfBirth'); }
  get relationshipControl() { return this.memberForm.get('relationshipToCustomer'); }
  get genderControl() { return this.memberForm.get('gender'); }

  loadMembers(customerId: string): void {
    this.isLoading = true;
    this.customerService.getMembers(customerId).subscribe({
      next: (list) => {
        this.isLoading = false;
        this.members = list || [];
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  startEdit(member: MemberResponse): void {
    this.isEditing = true;
    this.editingMemberId = member.memberId;
    this.memberForm.patchValue({
      firstName: member.firstName,
      lastName: member.lastName,
      dateOfBirth: member.dateOfBirth,
      relationshipToCustomer: member.relationshipToCustomer,
      gender: member.gender
    });
  }

  cancelEdit(formDirective?: FormGroupDirective): void {
    this.isEditing = false;
    this.editingMemberId = null;
    if (formDirective) {
      formDirective.resetForm({
        relationshipToCustomer: 'SPOUSE',
        gender: 'MALE'
      });
    } else {
      this.memberForm.reset({
        relationshipToCustomer: 'SPOUSE',
        gender: 'MALE'
      });
    }
  }

  onSubmit(formDirective?: FormGroupDirective): void {
    if (!this.customer) {
      this.notificationService.error('Please select an active customer first.');
      return;
    }

    if (this.memberForm.invalid || this.isSubmitting) {
      this.memberForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    const req = this.memberForm.value;

    if (this.isEditing && this.editingMemberId) {
      this.customerService.updateMember(this.customer.customerId, this.editingMemberId, req).subscribe({
        next: (updated) => {
          this.isSubmitting = false;
          this.notificationService.success(`Member ${updated.firstName} ${updated.lastName} updated successfully.`);
          this.cancelEdit(formDirective);
          if (this.customer) this.loadMembers(this.customer.customerId);
        },
        error: () => {
          this.isSubmitting = false;
        }
      });
    } else {
      this.customerService.addMember(this.customer.customerId, req).subscribe({
        next: (created) => {
          this.isSubmitting = false;
          this.notificationService.success(`Member ${created.firstName} ${created.lastName} enrolled successfully.`);
          if (formDirective) {
            formDirective.resetForm({
              relationshipToCustomer: 'SPOUSE',
              gender: 'MALE'
            });
          } else {
            this.memberForm.reset({
              relationshipToCustomer: 'SPOUSE',
              gender: 'MALE'
            });
          }
          if (this.customer) this.loadMembers(this.customer.customerId);
        },
        error: () => {
          this.isSubmitting = false;
        }
      });
    }
  }

  deleteMember(member: MemberResponse): void {
    if (!this.customer) return;
    const confirmed = confirm(`Are you sure you want to remove member ${member.firstName} ${member.lastName}?`);
    if (!confirmed) return;

    this.customerService.removeMember(this.customer.customerId, member.memberId).subscribe({
      next: () => {
        this.notificationService.info(`Member ${member.firstName} ${member.lastName} removed.`);
        if (this.customer) this.loadMembers(this.customer.customerId);
      }
    });
  }
}
