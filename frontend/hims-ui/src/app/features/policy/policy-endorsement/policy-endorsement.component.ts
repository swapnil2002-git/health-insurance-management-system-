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
    { value: 'NOMINEE_CHANGE', label: 'Nominee / Beneficiary Change', desc: 'Update policy beneficiary shares' },
    { value: 'ADDRESS_CHANGE', label: 'Address / Contact Change', desc: 'Update primary residential address' },
    { value: 'COVERAGE_CHANGE', label: 'Coverage Limit Adjustment', desc: 'Alter sum insured or deductibles' },
    { value: 'RIDER_ADDITION', label: 'Add Ancillary Rider', desc: 'Attach critical illness or accidental rider' }
  ];

  relationships: string[] = ['SPOUSE', 'CHILD', 'PARENT', 'DEPENDENT'];
  nomineeRelationships: string[] = ['SPOUSE', 'SON', 'DAUGHTER', 'FATHER', 'MOTHER', 'SIBLING', 'LEGAL_HEIR'];
  riderOptions: string[] = [
    'Critical Illness Protection Rider',
    'Accidental Death & Permanent Disability',
    'Hospital Daily Cash Allowance',
    'Maternity & Newborn Care Rider'
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

  generateUuid(): string {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
      const r = (Math.random() * 16) | 0;
      const v = c === 'x' ? r : (r & 0x3) | 0x8;
      return v.toString(16);
    });
  }

  private initForm(): void {
    const username = this.authService.getUsername() || 'Customer / Agent';
    this.endorseForm = this.fb.group({
      endorsementType: ['ADD_MEMBER' as EndorsementType, [Validators.required]],
      description: ['Add dependent member to policy coverage', [Validators.required, Validators.minLength(5)]],
      requestedBy: [username, [Validators.required]],

      // ADD_MEMBER controls
      newMemberId: [this.generateUuid(), [Validators.required]],
      newMemberName: ['Jane Doe', []],
      newMemberRelation: ['SPOUSE', []],

      // REMOVE_MEMBER control
      selectedMemberId: ['', []],

      // NOMINEE_CHANGE controls
      nomineeName: ['Jane Doe', []],
      nomineeRelationship: ['SPOUSE', []],
      nomineePercentage: [100, []],

      // ADDRESS_CHANGE control
      newAddress: ['123 Elm Street, Suite 400, New York, NY 10001', []],

      // RIDER_ADDITION controls
      riderName: ['Critical Illness Protection Rider', []],
      riderAmount: [250000, []],

      // COVERAGE_CHANGE controls
      coverageName: ['Comprehensive Inpatient Care', []],
      coverageAmount: [750000, []],

      changeDetails: ['']
    });

    this.endorseForm.get('endorsementType')?.valueChanges.subscribe((type: EndorsementType) => {
      this.onTypeChange(type);
    });
  }

  onTypeChange(type: EndorsementType): void {
    switch (type) {
      case 'ADD_MEMBER':
        this.endorseForm.patchValue({
          description: 'Add dependent member to policy coverage',
          newMemberId: this.generateUuid()
        }, { emitEvent: false });
        break;
      case 'REMOVE_MEMBER':
        const firstMem = this.policy?.members && this.policy.members[0] ? this.policy.members[0].memberId : '';
        this.endorseForm.patchValue({
          description: 'Remove enrolled member from active policy coverage',
          selectedMemberId: firstMem
        }, { emitEvent: false });
        break;
      case 'NOMINEE_CHANGE':
        this.endorseForm.patchValue({
          description: 'Update primary beneficiary / nominee allocation'
        }, { emitEvent: false });
        break;
      case 'ADDRESS_CHANGE':
        this.endorseForm.patchValue({
          description: 'Update policyholder primary residence address'
        }, { emitEvent: false });
        break;
      case 'COVERAGE_CHANGE':
        this.endorseForm.patchValue({
          description: 'Adjust maximum inpatient coverage sum insured'
        }, { emitEvent: false });
        break;
      case 'RIDER_ADDITION':
        this.endorseForm.patchValue({
          description: 'Attach optional ancillary rider to active policy'
        }, { emitEvent: false });
        break;
    }
  }

  refreshNewMemberId(): void {
    this.endorseForm.patchValue({ newMemberId: this.generateUuid() });
    this.notificationService.info('New Member UUID generated.');
  }

  fetchPolicy(id: string): void {
    if (!id?.trim()) return;
    this.isLoading = true;
    this.policyService.getPolicy(id.trim()).subscribe({
      next: (res) => {
        this.isLoading = false;
        this.policy = res;
        if (res.members && res.members.length > 0 && !this.endorseForm.get('selectedMemberId')?.value) {
          this.endorseForm.patchValue({ selectedMemberId: res.members[0].memberId });
        }
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
    const changeData: Record<string, any> = {};

    switch (formVal.endorsementType) {
      case 'ADD_MEMBER':
        const memberIdToAdd = formVal.newMemberId || this.generateUuid();
        changeData['memberId'] = memberIdToAdd;
        changeData['memberName'] = formVal.newMemberName || 'Dependent Member';
        changeData['relationship'] = formVal.newMemberRelation || 'SPOUSE';
        break;

      case 'REMOVE_MEMBER':
        const memberIdToRemove = formVal.selectedMemberId || (this.policy.members && this.policy.members[0] ? this.policy.members[0].memberId : '');
        if (!memberIdToRemove) {
          this.notificationService.error('Please select an active member to remove.');
          this.isSubmitting = false;
          return;
        }
        changeData['memberId'] = memberIdToRemove;
        break;

      case 'NOMINEE_CHANGE':
        changeData['beneficiaryName'] = formVal.nomineeName || formVal.changeDetails || 'Primary Nominee';
        changeData['relationship'] = formVal.nomineeRelationship || 'SPOUSE';
        changeData['percentage'] = formVal.nomineePercentage || 100;
        break;

      case 'ADDRESS_CHANGE':
        changeData['newAddress'] = formVal.newAddress || formVal.changeDetails || '123 New Healthcare Way, Suite 400';
        break;

      case 'COVERAGE_CHANGE':
        changeData['coverageName'] = formVal.coverageName || 'Base Health Shield';
        changeData['coverageAmount'] = formVal.coverageAmount || 750000;
        break;

      case 'RIDER_ADDITION':
        changeData['riderName'] = formVal.riderName || 'Critical Illness Protection Rider';
        changeData['riderAmount'] = formVal.riderAmount || 250000;
        break;

      default:
        changeData['details'] = formVal.changeDetails || formVal.description;
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
          `Endorsement request submitted! ID: ${created.endorsementId}. Status: ${created.status}`
        );
        this.endorseForm.patchValue({ newMemberId: this.generateUuid() });
        this.loadEndorsements(this.policy!.policyId);
      },
      error: (err) => {
        this.isSubmitting = false;
        const msg = err.error?.message || err.error?.error || 'Failed to submit endorsement request';
        this.notificationService.error(msg);
      }
    });
  }

  onApprove(endId: string): void {
    if (!this.policy) return;
    const adminUser = this.authService.getUsername() || 'admin';
    this.policyService.approveEndorsement(this.policy.policyId, endId, { approvedBy: adminUser, notes: 'Approved by administrator' }).subscribe({
      next: (res) => {
        this.notificationService.success(`Endorsement APPROVED and applied to policy! Status: ${res.status}`);
        this.loadEndorsements(this.policy!.policyId);
        this.fetchPolicy(this.policy!.policyId);
      },
      error: (err) => {
        const msg = err.error?.message || 'Failed to approve endorsement';
        this.notificationService.error(msg);
      }
    });
  }

  onReject(endId: string): void {
    if (!this.policy) return;
    this.policyService.rejectEndorsement(this.policy.policyId, endId, { rejectionReason: 'Endorsement does not meet underwriting guidelines' }).subscribe({
      next: (res) => {
        this.notificationService.warning(`Endorsement ${endId} REJECTED.`);
        this.loadEndorsements(this.policy!.policyId);
      },
      error: (err) => {
        const msg = err.error?.message || 'Failed to reject endorsement';
        this.notificationService.error(msg);
      }
    });
  }

  getEndorsementDelta(e: PolicyEndorsementResponse): number {
    switch (e.endorsementType) {
      case 'ADD_MEMBER': return 150.00;
      case 'RIDER_ADDITION': return 50.00;
      case 'COVERAGE_CHANGE': return 80.00;
      default: return 150.00;
    }
  }

  payEndorsementPremium(e: PolicyEndorsementResponse): void {
    if (!this.policy) return;
    const deltaAmount = this.getEndorsementDelta(e);
    this.router.navigate(['/payments/pay'], {
      queryParams: {
        policyId: this.policy.policyId,
        amount: deltaAmount,
        endorsementId: e.endorsementId,
        reason: `Endorsement Adjustment (${e.endorsementType})`
      }
    });
  }

  goToPolicies(): void {
    this.router.navigate(['/policies']);
  }
}
