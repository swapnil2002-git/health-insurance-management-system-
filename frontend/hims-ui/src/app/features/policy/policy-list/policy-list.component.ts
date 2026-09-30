import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { PolicyService } from '../../../core/services/policy.service';
import { NotificationService } from '../../../core/services/notification.service';
import { AuthService } from '../../../core/auth/auth.service';
import { PolicyResponse } from '../../../core/models/policy.model';

@Component({
  selector: 'app-policy-list',
  templateUrl: './policy-list.component.html',
  styleUrls: ['./policy-list.component.scss']
})
export class PolicyListComponent implements OnInit {
  policies: PolicyResponse[] = [];
  filteredPolicies: PolicyResponse[] = [];
  searchTerm: string = '';
  selectedStatus: string = 'ALL';
  isLoading = false;
  isActionLoading: { [key: string]: boolean } = {};

  userRole = '';

  constructor(
    private policyService: PolicyService,
    private notificationService: NotificationService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.userRole = this.authService.getRole() || '';
    this.loadPolicies();
  }

  loadPolicies(): void {
    this.isLoading = true;
    this.policyService.getAllPolicies().subscribe({
      next: (data) => {
        this.isLoading = false;
        this.policies = data || [];
        this.applyFilter();
      },
      error: () => {
        this.isLoading = false;
        this.notificationService.error('Failed to load policies. Please check connectivity.');
      }
    });
  }

  applyFilter(): void {
    let result = [...this.policies];

    if (this.selectedStatus !== 'ALL') {
      result = result.filter((p) => p.status === this.selectedStatus);
    }

    if (this.searchTerm.trim()) {
      const term = this.searchTerm.trim().toLowerCase();
      result = result.filter(
        (p) =>
          p.policyNumber?.toLowerCase().includes(term) ||
          p.policyId?.toLowerCase().includes(term) ||
          p.customerId?.toLowerCase().includes(term) ||
          p.quoteId?.toLowerCase().includes(term)
      );
    }

    this.filteredPolicies = result;
  }

  onSearchChange(): void {
    this.applyFilter();
  }

  onStatusChange(status: string): void {
    this.selectedStatus = status;
    this.applyFilter();
  }

  // Quick action: Issue policy (DRAFT -> PENDING_PAYMENT)
  onIssue(policy: PolicyResponse): void {
    this.isActionLoading[policy.policyId] = true;
    this.policyService.issuePolicy(policy.policyId).subscribe({
      next: (updated) => {
        this.isActionLoading[policy.policyId] = false;
        this.notificationService.success(`Policy ${updated.policyNumber} issued successfully! Status: PENDING_PAYMENT`);
        this.loadPolicies();
      },
      error: () => {
        this.isActionLoading[policy.policyId] = false;
      }
    });
  }

  // Quick action: Activate policy (PENDING_PAYMENT -> ACTIVE)
  onActivate(policy: PolicyResponse): void {
    this.isActionLoading[policy.policyId] = true;
    this.policyService.activatePolicy(policy.policyId).subscribe({
      next: (updated) => {
        this.isActionLoading[policy.policyId] = false;
        this.notificationService.success(`Policy ${updated.policyNumber} ACTIVATED successfully!`);
        this.loadPolicies();
      },
      error: () => {
        this.isActionLoading[policy.policyId] = false;
      }
    });
  }

  viewPolicy(policyId: string): void {
    this.router.navigate(['/policies/view', policyId]);
  }

  endorsePolicy(policyId: string): void {
    this.router.navigate(['/policies/endorse'], { queryParams: { policyId } });
  }

  renewPolicy(policyId: string): void {
    this.router.navigate(['/policies/renew'], { queryParams: { policyId } });
  }

  cancelPolicy(policyId: string): void {
    this.router.navigate(['/policies/cancel'], { queryParams: { policyId } });
  }

  goToIssue(): void {
    this.router.navigate(['/policies/issue']);
  }

  // Count metrics
  get countDraft(): number {
    return this.policies.filter((p) => p.status === 'DRAFT').length;
  }

  get countPending(): number {
    return this.policies.filter((p) => p.status === 'PENDING_PAYMENT').length;
  }

  get countActive(): number {
    return this.policies.filter((p) => p.status === 'ACTIVE').length;
  }

  get countCancelled(): number {
    return this.policies.filter((p) => p.status === 'CANCELLED' || p.status === 'EXPIRED').length;
  }
}
