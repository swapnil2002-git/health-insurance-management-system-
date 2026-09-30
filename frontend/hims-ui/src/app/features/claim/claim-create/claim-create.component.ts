import { Component, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ClaimService } from '../../../core/services/claim.service';
import { PolicyService } from '../../../core/services/policy.service';
import { CustomerService } from '../../../core/services/customer.service';
import { NotificationService } from '../../../core/services/notification.service';
import { ClaimCreateRequest, ClaimType } from '../../../core/models/claim.model';
import { PolicyResponse } from '../../../core/models/policy.model';

export interface PolicyMemberOption {
  memberId: string;
  displayName: string;
  relationship?: string;
  dateOfBirth?: string;
}

export interface ClaimModalData {
  type: 'success' | 'error';
  title: string;
  message: string;
  claimId?: string;
  claimNumber?: string;
  policyNumber?: string;
  policyId?: string;
  memberId?: string;
  memberName?: string;
  totalClaimAmount?: number;
  claimType?: string;
  status?: string;
  copyClaimNumberSuccess?: boolean;
  copyClaimIdSuccess?: boolean;
  copyPolicySuccess?: boolean;
}

@Component({
  selector: 'app-claim-create',
  templateUrl: './claim-create.component.html',
  styleUrls: ['./claim-create.component.scss']
})
export class ClaimCreateComponent implements OnInit {
  claimForm!: FormGroup;
  isSubmitting = false;

  claimTypeOptions: ClaimType[] = ['CASHLESS', 'REIMBURSEMENT'];

  // Policy Search & Member resolution
  policyInput = '';
  isLoadingPolicy = false;
  resolvedPolicy: PolicyResponse | null = null;
  availableMembers: PolicyMemberOption[] = [];
  selectedMember: PolicyMemberOption | null = null;

  // Centered Popup Feedback Modal Data
  modalData: ClaimModalData | null = null;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private claimService: ClaimService,
    private policyService: PolicyService,
    private customerService: CustomerService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.initForm();
    // Add default initial service line and diagnosis
    this.addServiceLine();
    this.addDiagnosis(true);

    // Read optional query params (e.g. from Policy / Claims navigation)
    this.route.queryParams.subscribe((params) => {
      if (params['policyNumber']) {
        this.policyInput = params['policyNumber'];
        this.searchPolicyAndMembers();
      } else if (params['policyId']) {
        this.policyInput = params['policyId'];
        this.searchPolicyAndMembers();
      }
    });
  }

  private initForm(): void {
    const today = new Date().toISOString().split('T')[0];

    this.claimForm = this.fb.group({
      policyId: ['', [Validators.required, Validators.pattern(/^[0-9a-fA-F-]{36}$/)]],
      memberId: ['', [Validators.required, Validators.pattern(/^[0-9a-fA-F-]{36}$/)]],
      providerId: ['', [Validators.required, Validators.pattern(/^[0-9a-fA-F-]{36}$/)]],
      claimType: ['CASHLESS' as ClaimType, [Validators.required]],
      serviceDate: [today, [Validators.required]],
      admissionDate: [''],
      dischargeDate: [''],
      totalClaimAmount: [0, [Validators.required, Validators.min(1)]],
      remarks: [''],
      idempotencyKey: [this.generateUuid()],
      serviceLines: this.fb.array([]),
      diagnoses: this.fb.array([])
    });
  }

  get serviceLines(): FormArray {
    return this.claimForm.get('serviceLines') as FormArray;
  }

  get diagnoses(): FormArray {
    return this.claimForm.get('diagnoses') as FormArray;
  }

  searchPolicyAndMembers(): void {
    const raw = (this.policyInput || '').trim();
    if (!raw) {
      this.notificationService.warning('Please enter a Policy Number or Policy ID');
      return;
    }

    this.isLoadingPolicy = true;
    this.resolvedPolicy = null;
    this.availableMembers = [];
    this.selectedMember = null;
    this.claimForm.patchValue({ policyId: '', memberId: '' });

    const isUuid = /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/.test(raw);

    if (isUuid) {
      this.policyService.getPolicy(raw).subscribe({
        next: (policy) => {
          this.handleResolvedPolicy(policy);
        },
        error: (err) => {
          this.isLoadingPolicy = false;
          // Fallback: put raw UUID in form directly
          this.claimForm.patchValue({ policyId: raw });
          this.notificationService.error(err.error?.message || 'Could not find policy by UUID');
        }
      });
    } else {
      // Find policy by Policy Number
      this.policyService.getAllPolicies().subscribe({
        next: (policies) => {
          const match = (policies || []).find(
            (p) =>
              (p.policyNumber && p.policyNumber.toLowerCase() === raw.toLowerCase()) ||
              (p.policyId && p.policyId.toLowerCase() === raw.toLowerCase())
          );
          if (match) {
            this.handleResolvedPolicy(match);
          } else {
            this.isLoadingPolicy = false;
            this.notificationService.error(`No policy found matching "${raw}"`);
          }
        },
        error: (err) => {
          this.isLoadingPolicy = false;
          this.notificationService.error('Failed to search policies');
        }
      });
    }
  }

  private handleResolvedPolicy(policy: PolicyResponse): void {
    this.resolvedPolicy = policy;
    this.claimForm.patchValue({ policyId: policy.policyId });

    if (!policy.customerId) {
      this.isLoadingPolicy = false;
      this.populatePolicyMembersFallback(policy);
      return;
    }

    // Fetch registered members from Customer Service
    this.customerService.getMembers(policy.customerId).subscribe({
      next: (customerMembers) => {
        this.isLoadingPolicy = false;
        const membersList: PolicyMemberOption[] = [];
        const policyMemberIds = new Set((policy.members || []).map((pm) => pm.memberId));

        if (customerMembers && customerMembers.length > 0) {
          customerMembers.forEach((m) => {
            const isPolicyMember = policyMemberIds.size === 0 || policyMemberIds.has(m.memberId);
            if (isPolicyMember) {
              membersList.push({
                memberId: m.memberId,
                displayName: `${m.firstName} ${m.lastName} (${m.relationshipToCustomer || 'Primary Insured'})`,
                relationship: m.relationshipToCustomer,
                dateOfBirth: m.dateOfBirth
              });
            }
          });

          // Fallback: if no members matched policyMemberIds filter, include all customer members
          if (membersList.length === 0) {
            customerMembers.forEach((m) => {
              membersList.push({
                memberId: m.memberId,
                displayName: `${m.firstName} ${m.lastName} (${m.relationshipToCustomer || 'Member'})`,
                relationship: m.relationshipToCustomer,
                dateOfBirth: m.dateOfBirth
              });
            });
          }
        }

        // If policy has member IDs not found in customer service, append them
        if (policy.members && policy.members.length > 0) {
          policy.members.forEach((pm, idx) => {
            if (!membersList.some((m) => m.memberId === pm.memberId)) {
              membersList.push({
                memberId: pm.memberId,
                displayName: `Policy Member #${idx + 1} (${pm.memberId.substring(0, 8)}...)`
              });
            }
          });
        }

        this.availableMembers = membersList;
        if (this.availableMembers.length > 0) {
          this.selectMember(this.availableMembers[0]);
        }
        this.notificationService.success(`Policy "${policy.policyNumber}" resolved. ${this.availableMembers.length} member(s) available.`);
      },
      error: () => {
        this.isLoadingPolicy = false;
        this.populatePolicyMembersFallback(policy);
      }
    });
  }

  private populatePolicyMembersFallback(policy: PolicyResponse): void {
    if (policy.members && policy.members.length > 0) {
      this.availableMembers = policy.members.map((pm, idx) => ({
        memberId: pm.memberId,
        displayName: `Policy Member #${idx + 1} (${pm.memberId.substring(0, 8)}...)`
      }));
      this.selectMember(this.availableMembers[0]);
      this.notificationService.success(`Policy "${policy.policyNumber}" resolved with ${this.availableMembers.length} member(s).`);
    } else {
      this.availableMembers = [];
      this.notificationService.info(`Policy "${policy.policyNumber}" resolved, but has no registered members yet.`);
    }
  }

  onMemberSelect(memberId: string): void {
    const found = this.availableMembers.find((m) => m.memberId === memberId);
    if (found) {
      this.selectMember(found);
    } else {
      this.selectedMember = null;
      this.claimForm.patchValue({ memberId });
    }
  }

  private selectMember(member: PolicyMemberOption): void {
    this.selectedMember = member;
    this.claimForm.patchValue({ memberId: member.memberId });
  }

  addServiceLine(): void {
    if (this.serviceLines.length >= 20) {
      this.notificationService.warning('Maximum of 20 service lines allowed per claim submission.');
      return;
    }
    const today = this.claimForm?.get('serviceDate')?.value || new Date().toISOString().split('T')[0];
    const group = this.fb.group({
      serviceCode: ['SRV-GEN-01', [Validators.required, Validators.minLength(2), Validators.maxLength(30), Validators.pattern(/^[A-Za-z0-9-_]+$/)]],
      serviceDescription: ['General Medical Consultation & Care', [Validators.required, Validators.minLength(2), Validators.maxLength(255)]],
      serviceDate: [today, [Validators.required]],
      unitPrice: [500, [Validators.required, Validators.min(0.01)]],
      quantity: [1, [Validators.required, Validators.min(1)]]
    });

    group.valueChanges.subscribe(() => this.recalculateTotal());
    this.serviceLines.push(group);
    this.recalculateTotal();
  }

  removeServiceLine(index: number): void {
    if (this.serviceLines.length > 1) {
      this.serviceLines.removeAt(index);
      this.recalculateTotal();
    }
  }

  addDiagnosis(isPrimary = false): void {
    if (this.diagnoses.length >= 10) {
      this.notificationService.warning('Maximum of 10 diagnoses allowed per claim submission.');
      return;
    }
    const group = this.fb.group({
      diagnosisCode: [isPrimary ? 'J06.9' : 'R50.9', [Validators.required, Validators.minLength(2), Validators.maxLength(20), Validators.pattern(/^[A-Za-z0-9.]+$/)]],
      description: [isPrimary ? 'Acute upper respiratory infection, unspecified' : 'Fever, unspecified', [Validators.required, Validators.minLength(2), Validators.maxLength(255)]],
      primary: [isPrimary]
    });
    this.diagnoses.push(group);
  }

  removeDiagnosis(index: number): void {
    if (this.diagnoses.length > 1) {
      this.diagnoses.removeAt(index);
    }
  }

  recalculateTotal(): void {
    let sum = 0;
    this.serviceLines.controls.forEach((ctrl) => {
      const price = Number(ctrl.get('unitPrice')?.value) || 0;
      const qty = Number(ctrl.get('quantity')?.value) || 0;
      sum += price * qty;
    });

    if (sum > 0) {
      this.claimForm.patchValue({ totalClaimAmount: sum }, { emitEvent: false });
    }
  }

  generateUuid(): string {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
      const r = (Math.random() * 16) | 0;
      const v = c === 'x' ? r : (r & 0x3) | 0x8;
      return v.toString(16);
    });
  }

  onSubmit(): void {
    if (this.claimForm.invalid) {
      this.claimForm.markAllAsTouched();
      return;
    }

    const formVal = this.claimForm.value;

    const payload: ClaimCreateRequest = {
      policyId: formVal.policyId.trim(),
      memberId: formVal.memberId.trim(),
      providerId: formVal.providerId.trim(),
      claimType: formVal.claimType,
      serviceDate: formVal.serviceDate,
      admissionDate: formVal.admissionDate || undefined,
      dischargeDate: formVal.dischargeDate || undefined,
      totalClaimAmount: Number(formVal.totalClaimAmount),
      remarks: formVal.remarks ? formVal.remarks.trim() : undefined,
      serviceLines: formVal.serviceLines.map((s: any) => ({
        serviceCode: s.serviceCode.trim(),
        serviceDescription: s.serviceDescription.trim(),
        serviceDate: s.serviceDate,
        unitPrice: Number(s.unitPrice),
        quantity: Number(s.quantity)
      })),
      diagnoses: formVal.diagnoses.map((d: any) => ({
        diagnosisCode: d.diagnosisCode.trim(),
        description: d.description.trim(),
        primary: !!d.primary
      }))
    };

    this.isSubmitting = true;
    const idempotencyKey = formVal.idempotencyKey || undefined;

    this.claimService.createClaim(payload, idempotencyKey).subscribe({
      next: (res) => {
        this.isSubmitting = false;
        this.notificationService.success(`Claim "${res.claimNumber}" submitted successfully`);

        const memberDisplay = this.selectedMember
          ? this.selectedMember.displayName
          : res.memberId;

        // Display centered feedback modal with details and validate navigation
        this.modalData = {
          type: 'success',
          title: 'Claim Submitted Successfully!',
          message: `Healthcare claim "${res.claimNumber}" has been registered in the system. You can copy the identifiers below or navigate directly to validate and adjudicate this claim.`,
          claimId: res.claimId,
          claimNumber: res.claimNumber,
          policyNumber: this.resolvedPolicy?.policyNumber || formVal.policyId,
          policyId: res.policyId,
          memberId: res.memberId,
          memberName: memberDisplay,
          totalClaimAmount: res.totalClaimAmount,
          claimType: res.claimType,
          status: res.status || 'SUBMITTED'
        };
      },
      error: (err) => {
        this.isSubmitting = false;
        this.notificationService.error(err.error?.message || 'Failed to submit claim');
      }
    });
  }

  goToValidateClaim(): void {
    if (this.modalData?.claimId) {
      const claimId = this.modalData.claimId;
      this.modalData = null;
      this.router.navigate(['/claims/validate'], { queryParams: { claimId } });
    }
  }

  closeModal(): void {
    this.modalData = null;
    this.router.navigate(['/claims']);
  }

  copyToClipboard(text?: string, type: 'claimId' | 'claimNumber' | 'policy' = 'claimNumber'): void {
    if (!text) return;
    navigator.clipboard.writeText(text).then(() => {
      if (this.modalData) {
        if (type === 'claimNumber') {
          this.modalData.copyClaimNumberSuccess = true;
          setTimeout(() => {
            if (this.modalData) this.modalData.copyClaimNumberSuccess = false;
          }, 2500);
        } else if (type === 'claimId') {
          this.modalData.copyClaimIdSuccess = true;
          setTimeout(() => {
            if (this.modalData) this.modalData.copyClaimIdSuccess = false;
          }, 2500);
        } else if (type === 'policy') {
          this.modalData.copyPolicySuccess = true;
          setTimeout(() => {
            if (this.modalData) this.modalData.copyPolicySuccess = false;
          }, 2500);
        }
      }
      this.notificationService.info(`Copied to clipboard: ${text}`);
    });
  }

  cancel(): void {
    this.router.navigate(['/claims']);
  }
}
