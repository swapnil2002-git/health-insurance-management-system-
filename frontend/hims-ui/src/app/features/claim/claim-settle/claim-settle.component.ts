import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ClaimService } from '../../../core/services/claim.service';
import { NotificationService } from '../../../core/services/notification.service';
import {
  ClaimResponse,
  ClaimSettlementRequest,
  ClaimPaymentResponse,
  PayeeType
} from '../../../core/models/claim.model';

@Component({
  selector: 'app-claim-settle',
  templateUrl: './claim-settle.component.html',
  styleUrls: ['./claim-settle.component.scss']
})
export class ClaimSettleComponent implements OnInit {
  searchForm!: FormGroup;
  settleForm!: FormGroup;

  claim: ClaimResponse | null = null;
  settlementResult: ClaimPaymentResponse | null = null;
  availableClaims: ClaimResponse[] = [];

  isLoadingClaim = false;
  isLoadingClaimsList = false;
  isSettling = false;

  payeeOptions: PayeeType[] = ['PROVIDER', 'MEMBER'];

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

    this.initSettleForm();
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

  private initSettleForm(): void {
    const randomRef = 'EFT-' + Math.floor(100000 + Math.random() * 900000);

    this.settleForm = this.fb.group({
      paidAmount: [0, [Validators.required, Validators.min(0.01)]],
      payeeType: ['PROVIDER' as PayeeType, [Validators.required]],
      paymentReferenceNumber: [randomRef, [Validators.required, Validators.minLength(3), Validators.maxLength(100)]]
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
    this.settlementResult = null;

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

    // Auto-fill paidAmount with approved/payable amount if available
    let defaultAmount = res.approvedAmount || res.totalClaimAmount || 0;
    if (res.adjudication?.payableAmount) {
      defaultAmount = res.adjudication.payableAmount;
    }

    const defaultPayee: PayeeType = res.claimType === 'CASHLESS' ? 'PROVIDER' : 'MEMBER';

    this.settleForm.patchValue({
      paidAmount: defaultAmount,
      payeeType: defaultPayee
    });
  }

  onSettle(): void {
    if (!this.claim || this.settleForm.invalid) {
      this.settleForm.markAllAsTouched();
      return;
    }

    const payload: ClaimSettlementRequest = {
      paidAmount: Number(this.settleForm.value.paidAmount),
      payeeType: this.settleForm.value.payeeType,
      paymentReferenceNumber: this.settleForm.value.paymentReferenceNumber.trim()
    };

    this.isSettling = true;
    this.claimService.settleClaim(this.claim.claimId, payload).subscribe({
      next: (res) => {
        this.settlementResult = res;
        this.isSettling = false;
        this.notificationService.success('Claim settled and payment disbursed successfully');
        this.loadClaim(this.claim!.claimId);
      },
      error: (err) => {
        this.isSettling = false;
        this.notificationService.error(err.error?.message || 'Failed to settle claim');
      }
    });
  }

  viewEob(): void {
    if (this.claim) {
      this.router.navigate(['/claims/payment'], {
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
