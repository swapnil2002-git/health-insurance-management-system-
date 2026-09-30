import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ClaimService } from '../../../core/services/claim.service';
import { NotificationService } from '../../../core/services/notification.service';
import { ClaimResponse, ClaimValidationResponse } from '../../../core/models/claim.model';

@Component({
  selector: 'app-claim-eligibility',
  templateUrl: './claim-eligibility.component.html',
  styleUrls: ['./claim-eligibility.component.scss']
})
export class ClaimEligibilityComponent implements OnInit {
  searchForm!: FormGroup;
  claim: ClaimResponse | null = null;
  eligibilityChecks: ClaimValidationResponse[] = [];
  availableClaims: ClaimResponse[] = [];

  isLoadingClaim = false;
  isLoadingClaimsList = false;
  isVerifying = false;

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
    this.eligibilityChecks = [];

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
    if (res.validations && res.validations.length > 0) {
      this.eligibilityChecks = res.validations;
    }
    this.notificationService.success(`Claim "${res.claimNumber}" retrieved successfully`);
  }

  runEligibilityVerification(): void {
    if (!this.claim) return;

    this.isVerifying = true;
    this.claimService.verifyEligibility(this.claim.claimId).subscribe({
      next: (results) => {
        this.eligibilityChecks = results || [];
        this.isVerifying = false;
        const failedCount = this.eligibilityChecks.filter((v) => v.status === 'FAILED').length;
        if (failedCount === 0) {
          this.notificationService.success('Policy, member and provider eligibility verified successfully');
        } else {
          this.notificationService.warning(`${failedCount} external eligibility check(s) failed`);
        }
        // Reload claim to reflect updated status
        this.loadClaim(this.claim!.claimId);
      },
      error: (err) => {
        this.isVerifying = false;
        this.notificationService.error(err.error?.message || 'Failed to verify external eligibility');
      }
    });
  }

  get allPassed(): boolean {
    return this.eligibilityChecks.length > 0 && this.eligibilityChecks.every((v) => v.status === 'PASSED');
  }

  proceedToAdjudicate(): void {
    if (this.claim) {
      this.router.navigate(['/claims/adjudicate'], {
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
