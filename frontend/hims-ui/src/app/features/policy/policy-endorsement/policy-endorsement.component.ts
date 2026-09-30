import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { PolicyService } from '../../../core/services/policy.service';
import { NotificationService } from '../../../core/services/notification.service';
import { AuthService } from '../../../core/auth/auth.service';
import {
  PolicyResponse,
  PolicyEndorsementResponse,
  PolicyEndorsementRequest,
  EndorsementType
} from '../../../core/models/policy.model';

@Component({
  selector: 'app-policy-endorsement',
  templateUrl: './policy-endorsement.component.html',
  styleUrls: ['./policy-endorsement.component.scss']
})
export class PolicyEndorsementComponent implements OnInit {
  endorseForm!: FormGroup;
  policyIdInput: string = '';
  policy: PolicyResponse | null = null;
  endorsements: PolicyEndorsementResponse[] = [];

  isLoading = false;
  isSubmitting = false;
  userRole = '';

  endorsementTypes: { value: EndorsementType; label: string; desc: string }[] = [
    { value: 'ADD_MEMBER', label: 'Add Family Member', desc: 'Enroll a dependent or newborn to coverage' },
    { value: 'REMOVE_MEMBER', label: 'Remove Member', desc: 'De-enroll member from policy' },
    { value: 'ADDRESS_CHANGE', label: 'Address / Contact Change', desc: 'Update primary residential address' },
    { value: 'NOMINEE_CHANGE', label: 'Nominee / Beneficiary Change', desc: 'Update policy beneficiary shares' },
    { value: 'COVERAGE_CHANGE', label: 'Coverage Limit Adjustment', desc: 'Alter sum insured or deductibles' },
    { value: 'RIDER_ADDITION', label: 'Add Ancillary Rider', desc: 'Attach critical illness or accidental rider' }
  ];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private policyService: PolicyService,
    private notificationService: NotificationService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.userRole = this.authService.getRole() || '';
    this.initForm();

    this.route.queryParams.subscribe((params) => {
      if (params['policyId']) {
        this.policyIdInput = params['policyId'];
        this.fetchPolicy(this.policyIdInput);
      }
    });
  }

  private initForm(): void {
    const username = this.authService.getUsername() || 'Customer / Agent';
    this.endorseForm = this.fb.group({
      endorsementType: ['ADD_MEMBER' as EndorsementType, [Validators.required]],
      description: ['', [Validators.required, Validators.minLength(5)]],
      changeDetails: [''],
      requestedBy: [username, [Validators.required]]
    });
  }

  fetchPolicy(id: string): void {
    if (!id?.trim()) return;
    this.isLoading = true;
    this.policyService.getPolicy(id.trim()).subscribe({
      next: (res) => {
        this.isLoading = false;
        this.policy = res;
        this.loadEndorsements(res.policyId);
      },
      error: () => {
        this.isLoading = false;
        this.policy = null;
        this.notificationService.error('Policy not found with ID: ' + id);
      }
    });
  }

  loadEndorsements(policyId: string): void {
    this.policyService.getEndorsements(policyId).subscribe({
      next: (list) => (this.endorsements = list || []),
      error: () => (this.endorsements = [])
    });
  }

  onSearch(): void {
    if (this.policyIdInput) {
      this.fetchPolicy(this.policyIdInput);
    }
  }

  onSubmitEndorsement(): void {
    if (this.endorseForm.invalid || !this.policy || this.isSubmitting) {
      this.endorseForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    const formVal = this.endorseForm.value;
    const text = formVal.changeDetails || formVal.description;
    const changeData: Record<string, any> = {};

    switch (formVal.endorsementType) {
      case 'ADDRESS_CHANGE':
        changeData['newAddress'] = text;
        break;
      case 'NOMINEE_CHANGE':
        changeData['beneficiaryName'] = text;
        changeData['relationship'] = 'BENEFICIARY';
        break;
      case 'COVERAGE_CHANGE':
        changeData['coverageName'] = 'Enhanced Critical Care';
        changeData['coverageAmount'] = 750000;
        break;
      case 'RIDER_ADDITION':
        changeData['riderName'] = text || 'Critical Illness Rider';
        changeData['riderAmount'] = 250000;
        break;
      case 'ADD_MEMBER':
      case 'REMOVE_MEMBER':
        changeData['memberId'] = (this.policy.members && this.policy.members[0])
          ? this.policy.members[0].memberId
          : '00000000-0000-0000-0000-000000000001';
        break;
      default:
        changeData['details'] = text;
    }

    const req: PolicyEndorsementRequest = {
      endorsementType: formVal.endorsementType,
      description: formVal.description,
      changeData: changeData,
      requestedBy: formVal.requestedBy
    };

    this.policyService.requestEndorsement(this.policy.policyId, req).subscribe({
      next: (created) => {
        this.isSubmitting = false;
        this.notificationService.success(
          `Endorsement request submitted! Endorsement ID: ${created.endorsementId}`
        );
        this.endorseForm.patchValue({ description: '', changeDetails: '' });
        this.loadEndorsements(this.policy!.policyId);
      },
      error: () => {
        this.isSubmitting = false;
      }
    });
  }

  onApprove(endId: string): void {
    if (!this.policy) return;
    const adminUser = this.authService.getUsername() || 'admin';
    this.policyService.approveEndorsement(this.policy.policyId, endId, { approvedBy: adminUser, notes: 'Approved by administrator' }).subscribe({
      next: (res) => {
        this.notificationService.success(`Endorsement ${endId} APPROVED and applied!`);
        this.loadEndorsements(this.policy!.policyId);
      }
    });
  }

  onReject(endId: string): void {
    if (!this.policy) return;
    this.policyService.rejectEndorsement(this.policy.policyId, endId, { rejectionReason: 'Endorsement does not meet underwriting guidelines' }).subscribe({
      next: (res) => {
        this.notificationService.warning(`Endorsement ${endId} REJECTED.`);
        this.loadEndorsements(this.policy!.policyId);
      }
    });
  }

  goToPolicies(): void {
    this.router.navigate(['/policies']);
  }
}
