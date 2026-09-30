import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { PolicyService } from '../../../core/services/policy.service';
import { NotificationService } from '../../../core/services/notification.service';
import {
  PolicyResponse,
  PolicyEndorsementResponse,
  PolicyRenewalResponse,
  PolicyCancellationResponse
} from '../../../core/models/policy.model';

@Component({
  selector: 'app-policy-view',
  templateUrl: './policy-view.component.html',
  styleUrls: ['./policy-view.component.scss']
})
export class PolicyViewComponent implements OnInit {
  policyIdInput: string = '';
  policy: PolicyResponse | null = null;
  endorsements: PolicyEndorsementResponse[] = [];
  renewals: PolicyRenewalResponse[] = [];
  cancellation: PolicyCancellationResponse | null = null;

  isLoading = false;
  isActionLoading = false;
  activeSection: 'details' | 'endorsements' | 'renewals' | 'cancellation' = 'details';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private policyService: PolicyService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe((params) => {
      if (params['id']) {
        this.policyIdInput = params['id'];
        this.fetchPolicy(this.policyIdInput);
      } else {
        this.route.queryParams.subscribe((queryParams) => {
          if (queryParams['policyId']) {
            this.policyIdInput = queryParams['policyId'];
            this.fetchPolicy(this.policyIdInput);
          }
        });
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
        this.loadRelatedData(res.policyId);
      },
      error: () => {
        this.isLoading = false;
        this.policy = null;
        this.notificationService.error('Policy not found with ID: ' + id);
      }
    });
  }

  loadRelatedData(policyId: string): void {
    // Load Endorsements
    this.policyService.getEndorsements(policyId).subscribe({
      next: (data) => (this.endorsements = data || []),
      error: () => (this.endorsements = [])
    });

    // Load Renewals
    this.policyService.getRenewals(policyId).subscribe({
      next: (data) => (this.renewals = data || []),
      error: () => (this.renewals = [])
    });

    // Load Cancellation if exists
    this.policyService.getCancellation(policyId).subscribe({
      next: (data) => (this.cancellation = data),
      error: () => (this.cancellation = null)
    });
  }

  onSearch(): void {
    if (this.policyIdInput) {
      this.fetchPolicy(this.policyIdInput);
    }
  }

  copyToClipboard(text: string, label: string): void {
    navigator.clipboard.writeText(text).then(() => {
      this.notificationService.info(`Copied ${label} to clipboard!`);
    });
  }

  // Quick action: Issue
  onIssue(): void {
    if (!this.policy) return;
    this.isActionLoading = true;
    this.policyService.issuePolicy(this.policy.policyId).subscribe({
      next: (updated) => {
        this.isActionLoading = false;
        this.policy = updated;
        this.notificationService.success(`Policy ${updated.policyNumber} issued (PENDING_PAYMENT)!`);
      },
      error: () => (this.isActionLoading = false)
    });
  }

  // Quick action: Activate
  onActivate(): void {
    if (!this.policy) return;
    this.isActionLoading = true;
    this.policyService.activatePolicy(this.policy.policyId).subscribe({
      next: (updated) => {
        this.isActionLoading = false;
        this.policy = updated;
        this.notificationService.success(`Policy ${updated.policyNumber} is now ACTIVE!`);
      },
      error: () => (this.isActionLoading = false)
    });
  }

  goToPolicies(): void {
    this.router.navigate(['/policies']);
  }

  goToEndorse(): void {
    if (this.policy) {
      this.router.navigate(['/policies/endorse'], { queryParams: { policyId: this.policy.policyId } });
    }
  }

  goToRenew(): void {
    if (this.policy) {
      this.router.navigate(['/policies/renew'], { queryParams: { policyId: this.policy.policyId } });
    }
  }

  goToCancel(): void {
    if (this.policy) {
      this.router.navigate(['/policies/cancel'], { queryParams: { policyId: this.policy.policyId } });
    }
  }
}
