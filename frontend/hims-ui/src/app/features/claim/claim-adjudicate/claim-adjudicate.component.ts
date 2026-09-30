import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ClaimService } from '../../../core/services/claim.service';
import { NotificationService } from '../../../core/services/notification.service';
import { ClaimResponse, ClaimAdjudicationResponse } from '../../../core/models/claim.model';

@Component({
  selector: 'app-claim-adjudicate',
  templateUrl: './claim-adjudicate.component.html',
  styleUrls: ['./claim-adjudicate.component.scss']
})
export class ClaimAdjudicateComponent implements OnInit {
  searchForm!: FormGroup;
  claim: ClaimResponse | null = null;
  adjudication: ClaimAdjudicationResponse | null = null;
  availableClaims: ClaimResponse[] = [];

  isLoadingClaim = false;
  isLoadingClaimsList = false;
  isAdjudicating = false;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private claimService: ClaimService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.searchForm = this.fb.group({
      claimQuery: ['', [Validators.required]]
    });

    this.loadAvailableClaims();

    this.route.queryParams.subscribe((params) => {
      if (params['claimId']) {
        this.searchForm.patchValue({ claimQuery: params['claimId'] });
        this.loadClaim(params['claimId']);
      } else if (params['claimNumber']) {
        this.searchForm.patchValue({ claimQuery: params['claimNumber'] });
        this.loadClaim(params['claimNumber']);
      }
    });
  }

  loadAvailableClaims(): void {
    this.isLoadingClaimsList = true;
    this.claimService.getAllClaims().subscribe({
      next: (claims) => {
        this.isLoadingClaimsList = false;
        this.availableClaims = claims || [];
      },
      error: () => {
        this.isLoadingClaimsList = false;
      }
    });
  }

  onClaimSelect(claimId: string): void {
    if (!claimId) return;
    this.searchForm.patchValue({ claimQuery: claimId });
    this.loadClaim(claimId);
  }

  onSearch(): void {
    if (this.searchForm.invalid) {
      this.searchForm.markAllAsTouched();
      return;
    }
    const raw = (this.searchForm.value.claimQuery || '').trim();
    if (!raw) {
      this.notificationService.warning('Please enter a Claim Number or Claim ID');
      return;
    }
    this.loadClaim(raw);
  }

  loadClaim(query: string): void {
    const raw = (query || '').trim();
    if (!raw) return;

    this.isLoadingClaim = true;
    this.claim = null;
    this.adjudication = null;

    const isUuid = /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/.test(raw);

    if (isUuid) {
      this.claimService.getClaimById(raw).subscribe({
        next: (res) => {
          this.handleClaimLoaded(res);
        },
        error: (err) => {
          this.isLoadingClaim = false;
          this.notificationService.error(err.error?.message || `Failed to find claim by ID: ${raw}`);
        }
      });
    } else {
      this.claimService.getClaimByNumber(raw).subscribe({
        next: (res) => {
          this.handleClaimLoaded(res);
        },
        error: () => {
          this.claimService.getAllClaims().subscribe({
            next: (claims) => {
              const match = (claims || []).find(
                (c) =>
                  (c.claimNumber && c.claimNumber.toLowerCase() === raw.toLowerCase()) ||
                  (c.claimId && c.claimId.toLowerCase() === raw.toLowerCase())
              );
              if (match) {
                this.handleClaimLoaded(match);
              } else {
                this.isLoadingClaim = false;
                this.notificationService.error(`No claim found matching "${raw}"`);
              }
            },
            error: (err) => {
              this.isLoadingClaim = false;
              this.notificationService.error(err.error?.message || `No claim found matching "${raw}"`);
            }
          });
        }
      });
    }
  }

  private handleClaimLoaded(res: ClaimResponse): void {
    this.claim = res;
    this.isLoadingClaim = false;
    this.searchForm.patchValue({ claimQuery: res.claimNumber || res.claimId });
    this.notificationService.success(`Claim "${res.claimNumber}" retrieved successfully`);

    if (res.adjudication) {
      this.adjudication = res.adjudication;
    } else {
      // Try fetching adjudication directly
      this.claimService.getAdjudication(res.claimId).subscribe({
        next: (adj) => { this.adjudication = adj; },
        error: () => { /* Not adjudicated yet */ }
      });
    }
  }

  adjudicate(): void {
    if (!this.claim) return;

    this.isAdjudicating = true;
    this.claimService.adjudicateClaim(this.claim.claimId).subscribe({
      next: (res) => {
        this.adjudication = res;
        this.isAdjudicating = false;
        this.notificationService.success(`Claim adjudicated with decision: ${res.decision}`);
        this.loadClaim(this.claim!.claimId);
      },
      error: (err) => {
        this.isAdjudicating = false;
        this.notificationService.error(err.error?.message || 'Failed to adjudicate claim');
      }
    });
  }

  proceedToSettle(): void {
    if (this.claim) {
      this.router.navigate(['/claims/settle'], {
        queryParams: { claimId: this.claim.claimId }
      });
    }
  }

  viewDossier(): void {
    if (this.claim) {
      this.router.navigate(['/claims/view', this.claim.claimId]);
    }
  }
}
