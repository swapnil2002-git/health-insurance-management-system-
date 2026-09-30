import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ClaimService } from '../../../core/services/claim.service';
import { NotificationService } from '../../../core/services/notification.service';
import { ClaimResponse, ClaimStatus } from '../../../core/models/claim.model';

@Component({
  selector: 'app-claim-list',
  templateUrl: './claim-list.component.html',
  styleUrls: ['./claim-list.component.scss']
})
export class ClaimListComponent implements OnInit {
  claims: ClaimResponse[] = [];
  filteredClaims: ClaimResponse[] = [];

  statusFilter: string = 'ALL';
  searchText: string = '';
  isLoading = false;

  constructor(
    private router: Router,
    private claimService: ClaimService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.loadClaims();
  }

  loadClaims(): void {
    this.isLoading = true;
    const paramStatus = this.statusFilter === 'ALL' ? undefined : (this.statusFilter as ClaimStatus);

    this.claimService.getAllClaims(paramStatus).subscribe({
      next: (list) => {
        this.claims = list || [];
        this.applyFilter();
        this.isLoading = false;
      },
      error: (err) => {
        this.isLoading = false;
        this.claims = [];
        this.filteredClaims = [];
        this.notificationService.error(err.error?.message || 'Failed to retrieve claims');
      }
    });
  }

  onFilterChange(status: string): void {
    this.statusFilter = status;
    this.loadClaims();
  }

  applyFilter(): void {
    if (!this.searchText.trim()) {
      this.filteredClaims = [...this.claims];
    } else {
      const q = this.searchText.toLowerCase().trim();
      this.filteredClaims = this.claims.filter(
        (c) =>
          c.claimNumber.toLowerCase().includes(q) ||
          c.claimId.toLowerCase().includes(q) ||
          c.policyId.toLowerCase().includes(q) ||
          c.memberId.toLowerCase().includes(q) ||
          c.claimType.toLowerCase().includes(q) ||
          c.status.toLowerCase().includes(q)
      );
    }
  }

  get totalCount(): number {
    return this.claims.length;
  }

  get pendingCount(): number {
    return this.claims.filter(
      (c) => c.status === 'SUBMITTED' || c.status === 'VALIDATING' || c.status === 'ELIGIBILITY_CHECK'
    ).length;
  }

  get approvedCount(): number {
    return this.claims.filter((c) => c.status === 'APPROVED' || c.status === 'PAYMENT_PENDING').length;
  }

  get settledCount(): number {
    return this.claims.filter((c) => c.status === 'SETTLED').length;
  }

  get totalClaimAmount(): number {
    return this.claims.reduce((acc, c) => acc + (c.totalClaimAmount || 0), 0);
  }

  viewClaim(id: string): void {
    this.router.navigate(['/claims/view', id]);
  }

  validateClaim(id: string, event: Event): void {
    event.stopPropagation();
    this.router.navigate(['/claims/validate'], { queryParams: { claimId: id } });
  }

  verifyEligibility(id: string, event: Event): void {
    event.stopPropagation();
    this.router.navigate(['/claims/eligibility'], { queryParams: { claimId: id } });
  }

  adjudicateClaim(id: string, event: Event): void {
    event.stopPropagation();
    this.router.navigate(['/claims/adjudicate'], { queryParams: { claimId: id } });
  }

  settleClaim(id: string, event: Event): void {
    event.stopPropagation();
    this.router.navigate(['/claims/settle'], { queryParams: { claimId: id } });
  }

  viewEob(id: string, event: Event): void {
    event.stopPropagation();
    this.router.navigate(['/claims/payment'], { queryParams: { claimId: id } });
  }
}
