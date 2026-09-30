import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormArray, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { PolicyService } from '../../../core/services/policy.service';
import { CustomerService } from '../../../core/services/customer.service';
import { NotificationService } from '../../../core/services/notification.service';
import { PolicyResponse, PolicyCreateRequest } from '../../../core/models/policy.model';
import { MemberResponse } from '../../../core/models/customer.model';

export interface PolicyFeedbackModal {
  type: 'success' | 'error';
  title: string;
  message: string;
  policyId?: string;
  policyNumber?: string;
  customerId?: string;
  copyPolicyIdSuccess?: boolean;
  copyPolicyNumberSuccess?: boolean;
  copyCustomerIdSuccess?: boolean;
}

@Component({
  selector: 'app-policy-issue',
  templateUrl: './policy-issue.component.html',
  styleUrls: ['./policy-issue.component.scss']
})
export class PolicyIssueComponent implements OnInit {
  createForm!: FormGroup;
  lookupPolicyId: string = '';
  lookupCustomerId: string = '';
  customerPolicies: PolicyResponse[] = [];
  customerMembers: MemberResponse[] = [];
  selectedPolicyId: string = '';
  foundPolicy: PolicyResponse | null = null;
  activeTab: 'create' | 'issue' = 'create';
  isLoading = false;
  isLoadingPolicies = false;
  isLoadingMembers = false;
  isSubmitting = false;
  modalData: PolicyFeedbackModal | null = null;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private policyService: PolicyService,
    private customerService: CustomerService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.initForm();

    this.route.queryParams.subscribe((params) => {
      if (params['customerId']) {
        const custId = params['customerId'];
        this.lookupCustomerId = custId;
        this.createForm.patchValue({ customerId: custId });
        this.activeTab = 'issue';
        this.loadCustomerAndPolicies(custId, params['quoteId'], params['policyId']);
      } else if (params['policyId']) {
        this.lookupPolicyId = params['policyId'];
        this.selectedPolicyId = params['policyId'];
        this.activeTab = 'issue';
        this.fetchPolicy(this.lookupPolicyId);
      }
      if (params['quoteId']) {
        this.createForm.patchValue({ quoteId: params['quoteId'] });
      }
      if (params['planId']) {
        this.createForm.patchValue({ planId: params['planId'] });
      }
    });
  }

  private initForm(): void {
    const today = new Date();
    const nextYear = new Date();
    nextYear.setFullYear(today.getFullYear() + 1);

    this.createForm = this.fb.group({
      customerId: ['', [Validators.required]],
      quoteId: ['', [Validators.required]],
      planId: ['', [Validators.required]],
      effectiveDate: [today.toISOString().substring(0, 10), [Validators.required]],
      expiryDate: [nextYear.toISOString().substring(0, 10), [Validators.required]],
      members: this.fb.array([this.createMemberGroup()]),
      coverages: this.fb.array([this.createCoverageGroup()]),
      beneficiaries: this.fb.array([])
    });

    // Automatically load registered members when Customer ID is entered
    this.createForm.get('customerId')?.valueChanges.subscribe((val) => {
      const trimmed = (val || '').trim();
      if (trimmed.length >= 32) {
        this.customerService.getMembers(trimmed).subscribe({
          next: (members) => {
            this.customerMembers = members || [];
            if (this.customerMembers.length > 0) {
              const currentFirst = this.membersArray.at(0)?.get('memberId')?.value;
              if (!currentFirst || this.membersArray.length <= 1) {
                this.membersArray.clear();
                this.customerMembers.forEach((m) => {
                  this.membersArray.push(this.createMemberGroup(m.memberId));
                });
              }
            }
          },
          error: () => {}
        });
      }
    });
  }

  get membersArray(): FormArray {
    return this.createForm.get('members') as FormArray;
  }

  get coveragesArray(): FormArray {
    return this.createForm.get('coverages') as FormArray;
  }

  get beneficiariesArray(): FormArray {
    return this.createForm.get('beneficiaries') as FormArray;
  }

  createMemberGroup(memberId: string = ''): FormGroup {
    return this.fb.group({
      memberId: [memberId, [Validators.required]]
    });
  }

  createCoverageGroup(): FormGroup {
    return this.fb.group({
      coverageName: ['Hospitalization & Inpatient Care', [Validators.required]],
      coverageAmount: [500000, [Validators.required, Validators.min(0)]],
      deductible: [0, [Validators.required, Validators.min(0)]]
    });
  }

  createBeneficiaryGroup(): FormGroup {
    return this.fb.group({
      beneficiaryName: ['', [Validators.required]],
      relationship: ['SPOUSE', [Validators.required]],
      percentage: [100, [Validators.required, Validators.min(1), Validators.max(100)]]
    });
  }

  addMember(): void {
    this.membersArray.push(this.createMemberGroup());
  }

  removeMember(i: number): void {
    if (this.membersArray.length > 1) {
      this.membersArray.removeAt(i);
    }
  }

  addCoverage(): void {
    this.coveragesArray.push(this.createCoverageGroup());
  }

  removeCoverage(i: number): void {
    this.coveragesArray.removeAt(i);
  }

  addBeneficiary(): void {
    this.beneficiariesArray.push(this.createBeneficiaryGroup());
  }

  removeBeneficiary(i: number): void {
    this.beneficiariesArray.removeAt(i);
  }

  onCreatePolicy(): void {
    if (this.createForm.invalid || this.isSubmitting) {
      this.createForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    const formVal = this.createForm.value;

    const req: PolicyCreateRequest = {
      customerId: formVal.customerId,
      quoteId: formVal.quoteId,
      planId: formVal.planId,
      effectiveDate: new Date(formVal.effectiveDate).toISOString(),
      expiryDate: new Date(formVal.expiryDate).toISOString(),
      members: formVal.members,
      coverages: formVal.coverages,
      beneficiaries: formVal.beneficiaries
    };

    this.policyService.createPolicy(req).subscribe({
      next: (created) => {
        this.isSubmitting = false;
        this.foundPolicy = created;
        this.lookupPolicyId = created.policyId;
        this.activeTab = 'issue';
        this.notificationService.success(
          `Draft Policy created successfully! Number: ${created.policyNumber || 'Pending'}`
        );
      },
      error: () => {
        this.isSubmitting = false;
      }
    });
  }

  loadCustomerAndPolicies(customerId: string, targetQuoteId?: string, targetPolicyId?: string): void {
    const trimmed = customerId?.trim();
    if (!trimmed) {
      this.customerPolicies = [];
      this.customerMembers = [];
      this.selectedPolicyId = '';
      return;
    }

    this.isLoadingPolicies = true;
    this.isLoadingMembers = true;

    // 1. Fetch policies associated with customer
    this.policyService.getPoliciesByCustomerId(trimmed).subscribe({
      next: (policies) => {
        this.isLoadingPolicies = false;
        this.customerPolicies = policies || [];

        if (this.customerPolicies.length > 0) {
          let chosen: PolicyResponse | undefined;
          if (targetPolicyId) {
            chosen = this.customerPolicies.find((p) => p.policyId === targetPolicyId);
          }
          if (!chosen && targetQuoteId) {
            chosen = this.customerPolicies.find((p) => p.quoteId === targetQuoteId);
          }
          if (!chosen) {
            chosen = this.customerPolicies.find((p) => p.status === 'DRAFT') || this.customerPolicies[0];
          }

          if (chosen) {
            this.selectedPolicyId = chosen.policyId;
            this.lookupPolicyId = chosen.policyId;
            this.fetchPolicy(chosen.policyId);
          }
        } else {
          this.selectedPolicyId = '';
        }
      },
      error: () => {
        this.isLoadingPolicies = false;
        this.customerPolicies = [];
      }
    });

    // 2. Fetch customer members from Customer Service
    this.customerService.getMembers(trimmed).subscribe({
      next: (members) => {
        this.isLoadingMembers = false;
        this.customerMembers = members || [];
      },
      error: () => {
        this.isLoadingMembers = false;
        this.customerMembers = [];
      }
    });
  }

  onCustomerBlur(): void {
    if (this.lookupCustomerId && this.lookupCustomerId.trim().length >= 32) {
      this.loadCustomerAndPolicies(this.lookupCustomerId);
    }
  }

  onSelectPolicy(policyId: string): void {
    this.selectedPolicyId = policyId;
    this.lookupPolicyId = policyId;
    this.fetchPolicy(policyId);
  }

  fetchPolicy(id: string): void {
    if (!id?.trim()) return;
    this.isLoading = true;
    this.policyService.getPolicy(id.trim()).subscribe({
      next: (res) => {
        this.isLoading = false;
        this.foundPolicy = res;
        this.selectedPolicyId = res.policyId;

        // Auto-sync customer members if not yet loaded or mismatched
        if (res.customerId && (!this.lookupCustomerId || this.lookupCustomerId !== res.customerId || this.customerMembers.length === 0)) {
          this.lookupCustomerId = res.customerId;
          this.isLoadingMembers = true;
          this.customerService.getMembers(res.customerId).subscribe({
            next: (members) => {
              this.isLoadingMembers = false;
              this.customerMembers = members || [];
            },
            error: () => {
              this.isLoadingMembers = false;
              this.customerMembers = [];
            }
          });
        }
      },
      error: () => {
        this.isLoading = false;
        this.foundPolicy = null;
      }
    });
  }

  onIssuePolicy(): void {
    if (!this.foundPolicy || this.isSubmitting) return;
    this.isSubmitting = true;
    const policyToIssue = this.foundPolicy;

    this.policyService.issuePolicy(policyToIssue.policyId).subscribe({
      next: (issued) => {
        this.isSubmitting = false;
        this.foundPolicy = issued;

        // Update in customerPolicies list if present
        const idx = this.customerPolicies.findIndex((p) => p.policyId === issued.policyId);
        if (idx >= 0) {
          this.customerPolicies[idx] = issued;
        }

        this.modalData = {
          type: 'success',
          title: 'Policy Issued Successfully!',
          message: `Policy ${issued.policyNumber} has been officially issued and transitioned to PENDING_PAYMENT status. A PolicyIssued event was published to Kafka for automated premium schedule formulation.`,
          policyId: issued.policyId,
          policyNumber: issued.policyNumber,
          customerId: issued.customerId
        };

        this.notificationService.success(
          `Policy ${issued.policyNumber} issued! Status updated to PENDING_PAYMENT.`
        );
      },
      error: (err) => {
        this.isSubmitting = false;
        const msg = err?.error?.message || err?.message || 'Failed to issue policy.';
        this.modalData = {
          type: 'error',
          title: 'Policy Issuance Failed',
          message: msg,
          policyId: policyToIssue.policyId,
          policyNumber: policyToIssue.policyNumber,
          customerId: policyToIssue.customerId
        };
        this.notificationService.error(msg);
      }
    });
  }

  closeModal(): void {
    this.modalData = null;
  }

  copyToClipboard(text: string | undefined, field: 'policyId' | 'policyNumber' | 'customerId'): void {
    if (!text || !this.modalData) return;
    navigator.clipboard.writeText(text).then(() => {
      if (!this.modalData) return;
      if (field === 'policyId') {
        this.modalData.copyPolicyIdSuccess = true;
        setTimeout(() => { if (this.modalData) this.modalData.copyPolicyIdSuccess = false; }, 2000);
      } else if (field === 'policyNumber') {
        this.modalData.copyPolicyNumberSuccess = true;
        setTimeout(() => { if (this.modalData) this.modalData.copyPolicyNumberSuccess = false; }, 2000);
      } else if (field === 'customerId') {
        this.modalData.copyCustomerIdSuccess = true;
        setTimeout(() => { if (this.modalData) this.modalData.copyCustomerIdSuccess = false; }, 2000);
      }
    });
  }

  onActivatePolicy(): void {
    if (!this.foundPolicy || this.isSubmitting) return;
    this.isSubmitting = true;
    this.policyService.activatePolicy(this.foundPolicy.policyId).subscribe({
      next: (act) => {
        this.isSubmitting = false;
        this.foundPolicy = act;
        const idx = this.customerPolicies.findIndex((p) => p.policyId === act.policyId);
        if (idx >= 0) {
          this.customerPolicies[idx] = act;
        }
        this.notificationService.success(
          `Policy ${act.policyNumber} is now ACTIVE! Coverage is officially in effect.`
        );
      },
      error: () => {
        this.isSubmitting = false;
      }
    });
  }

  viewDossier(): void {
    if (this.foundPolicy) {
      this.router.navigate(['/policies/view', this.foundPolicy.policyId]);
    }
  }

  goToPolicies(): void {
    this.router.navigate(['/policies']);
  }
}
