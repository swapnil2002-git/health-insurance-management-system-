import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ClaimService } from '../../../core/services/claim.service';
import { NotificationService } from '../../../core/services/notification.service';
import { ClaimResponse, ClaimValidationResponse } from '../../../core/models/claim.model';

@Component({
  selector: 'app-claim-validate',
  templateUrl: './claim-validate.component.html',
  styleUrls: ['./claim-validate.component.scss']
})
export class ClaimValidateComponent implements OnInit {
  searchForm!: FormGroup;
  claim: ClaimResponse | null = null;
  validations: ClaimValidationResponse[] = [];
  availableClaims: ClaimResponse[] = [];

  isLoadingClaim = false;
  isLoadingClaimsList = false;
  isValidating = false;

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
    this.validations = [];

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
      // First try getClaimByNumber
      this.claimService.getClaimByNumber(raw).subscribe({
        next: (res) => {
          this.handleClaimLoaded(res);
        },
        error: () => {
          // Fallback: search in getAllClaims()
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
      this.validations = res.validations;
    }
    this.notificationService.success(`Claim "${res.claimNumber}" retrieved successfully`);
  }

  runValidation(): void {
    if (!this.claim) return;

    this.isValidating = true;
    this.claimService.validateClaim(this.claim.claimId).subscribe({
      next: (results) => {
        this.validations = results || [];
        this.isValidating = false;
        const failedCount = this.validations.filter((v) => v.status === 'FAILED').length;
        if (failedCount === 0) {
          this.notificationService.success('All validation rules passed successfully');
        } else {
          this.notificationService.warning(`${failedCount} validation rule(s) failed or require attention`);
        }
        // Refresh claim status
        this.loadClaim(this.claim!.claimId);
      },
      error: (err) => {
        this.isValidating = false;
        this.notificationService.error(err.error?.message || 'Failed to validate claim');
      }
    });
  }

  get allPassed(): boolean {
    return this.validations.length > 0 && this.validations.every((v) => v.status === 'PASSED');
  }

  proceedToEligibility(): void {
    if (this.claim) {
      this.router.navigate(['/claims/eligibility'], {
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
