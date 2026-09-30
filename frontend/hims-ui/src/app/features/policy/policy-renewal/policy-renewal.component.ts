import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { PolicyService } from '../../../core/services/policy.service';
import { NotificationService } from '../../../core/services/notification.service';
import {
  PolicyResponse,
  RenewalEligibilityResponse,
  PolicyRenewalResponse
} from '../../../core/models/policy.model';

@Component({
  selector: 'app-policy-renewal',
  templateUrl: './policy-renewal.component.html',
  styleUrls: ['./policy-renewal.component.scss']
})
export class PolicyRenewalComponent implements OnInit {
  policyIdInput: string = '';
  policy: PolicyResponse | null = null;
  eligibility: RenewalEligibilityResponse | null = null;
  renewals: PolicyRenewalResponse[] = [];

  isLoading = false;
  isGeneratingQuote = false;
  isProcessingAction: { [key: string]: boolean } = {};

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private policyService: PolicyService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe((params) => {
      if (params['policyId']) {
        this.policyIdInput = params['policyId'];
        this.fetchPolicy(this.policyIdInput);
      }
    });
  }

  fetchPolicy(id: string): void {
    if (!id?.trim()) return;
    this.isLoading = true;
    this.policyService.getPolicy(id.trim()).subscribe({
      next: (res) => {
        this.isLoading = false;
        this.policy = res;
        this.checkEligibility(res.policyId);
        this.loadRenewals(res.policyId);
      },
      error: () => {
        this.isLoading = false;
        this.policy = null;
        this.notificationService.error('Policy not found with ID: ' + id);
      }
    });
  }

  checkEligibility(policyId: string): void {
    this.policyService.checkRenewalEligibility(policyId).subscribe({
      next: (data) => (this.eligibility = data),
      error: () => (this.eligibility = null)
    });
  }

  loadRenewals(policyId: string): void {
    this.policyService.getRenewals(policyId).subscribe({
      next: (list) => (this.renewals = list || []),
      error: () => (this.renewals = [])
    });
  }

  onSearch(): void {
    if (this.policyIdInput) {
      this.fetchPolicy(this.policyIdInput);
    }
  }

  onGenerateQuote(): void {
    if (!this.policy || this.isGeneratingQuote) return;
    this.isGeneratingQuote = true;
    this.policyService.generateRenewalQuote(this.policy.policyId).subscribe({
      next: (quote) => {
        this.isGeneratingQuote = false;
        this.notificationService.success(
          `Renewal quote generated! Quote ID: ${quote.renewalQuoteId || quote.renewalId}`
        );
        this.loadRenewals(this.policy!.policyId);
      },
      error: () => {
        this.isGeneratingQuote = false;
      }
    });
  }

  onAccept(ren: PolicyRenewalResponse): void {
    if (!this.policy) return;
    this.isProcessingAction[ren.renewalId] = true;
    this.policyService.acceptRenewal(this.policy.policyId, ren.renewalId).subscribe({
      next: (updated) => {
        this.isProcessingAction[ren.renewalId] = false;
        this.notificationService.success('Renewal quote accepted! Status: ACCEPTED');
        this.loadRenewals(this.policy!.policyId);
      },
      error: () => {
        this.isProcessingAction[ren.renewalId] = false;
      }
    });
  }

  onComplete(ren: PolicyRenewalResponse): void {
    if (!this.policy) return;
    this.isProcessingAction[ren.renewalId] = true;
    const req = {
      paymentId: 'PAY-' + Math.random().toString(36).substring(2, 9).toUpperCase(),
      amountPaid: ren.renewalPremium || 500
    };

    this.policyService.completeRenewal(this.policy.policyId, ren.renewalId, req).subscribe({
      next: (completed) => {
        this.isProcessingAction[ren.renewalId] = false;
        this.notificationService.success(
          `Renewal COMPLETED! Policy coverage term extended to ${completed.newExpiryDate}`
        );
        this.fetchPolicy(this.policy!.policyId);
      },
      error: () => {
        this.isProcessingAction[ren.renewalId] = false;
      }
    });
  }

  onReject(ren: PolicyRenewalResponse): void {
    if (!this.policy) return;
    this.isProcessingAction[ren.renewalId] = true;
    this.policyService.rejectRenewal(this.policy.policyId, ren.renewalId, {
      rejectionReason: 'Declined by customer'
    }).subscribe({
      next: () => {
        this.isProcessingAction[ren.renewalId] = false;
        this.notificationService.info('Renewal quote declined.');
        this.loadRenewals(this.policy!.policyId);
      },
      error: () => {
        this.isProcessingAction[ren.renewalId] = false;
      }
    });
  }

  goToPolicies(): void {
    this.router.navigate(['/policies']);
  }
}
