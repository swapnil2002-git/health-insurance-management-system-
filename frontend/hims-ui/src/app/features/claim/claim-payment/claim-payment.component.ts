import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ClaimService } from '../../../core/services/claim.service';
import { NotificationService } from '../../../core/services/notification.service';
import {
  ExplanationOfBenefitsResponse,
  ClaimPaymentResponse,
  ClaimResponse
} from '../../../core/models/claim.model';

@Component({
  selector: 'app-claim-payment',
  templateUrl: './claim-payment.component.html',
  styleUrls: ['./claim-payment.component.scss']
})
export class ClaimPaymentComponent implements OnInit {
  searchForm!: FormGroup;

  eob: ExplanationOfBenefitsResponse | null = null;
  payments: ClaimPaymentResponse[] = [];
  claim: ClaimResponse | null = null;

  isLoading = false;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private claimService: ClaimService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.searchForm = this.fb.group({
      query: ['', [Validators.required]]
    });

    this.route.queryParams.subscribe((params) => {
      if (params['claimId']) {
        this.searchForm.patchValue({ query: params['claimId'] });
        this.loadByClaimId(params['claimId']);
      } else if (params['eobNumber']) {
        this.searchForm.patchValue({ query: params['eobNumber'] });
        this.loadByEobNumber(params['eobNumber']);
      }
    });
  }

  onSearch(): void {
    if (this.searchForm.invalid) {
      this.searchForm.markAllAsTouched();
      return;
    }

    const q = this.searchForm.value.query.trim();
    if (/^[0-9a-fA-F-]{36}$/.test(q)) {
      this.loadByClaimId(q);
    } else {
      this.loadByEobNumber(q);
    }
  }

  loadByClaimId(claimId: string): void {
    this.isLoading = true;
    this.eob = null;
    this.payments = [];
    this.claim = null;

    // Fetch EOB
    this.claimService.getEobByClaimId(claimId).subscribe({
      next: (res) => {
        this.eob = res;
        this.isLoading = false;
      },
      error: (err) => {
        this.isLoading = false;
        this.notificationService.error(err.error?.message || 'EOB not found for this claim ID');
      }
    });

    // Also fetch claim context and payments
    this.claimService.getClaimById(claimId).subscribe({
      next: (c) => { this.claim = c; },
      error: () => {}
    });

    this.claimService.getPaymentsByClaimId(claimId).subscribe({
      next: (p) => { this.payments = p || []; },
      error: () => {}
    });
  }

  loadByEobNumber(eobNumber: string): void {
    this.isLoading = true;
    this.eob = null;
    this.payments = [];
    this.claim = null;

    this.claimService.getEobByNumber(eobNumber).subscribe({
      next: (res) => {
        this.eob = res;
        this.isLoading = false;

        // Fetch associated claim & payments
        if (res.claimId) {
          this.claimService.getClaimById(res.claimId).subscribe({
            next: (c) => { this.claim = c; }
          });
          this.claimService.getPaymentsByClaimId(res.claimId).subscribe({
            next: (p) => { this.payments = p || []; }
          });
        }
      },
      error: (err) => {
        this.isLoading = false;
        this.notificationService.error(err.error?.message || 'EOB not found for this EOB number');
      }
    });
  }

  printEob(): void {
    window.print();
  }

  viewDossier(): void {
    if (this.eob) {
      this.router.navigate(['/claims/view', this.eob.claimId]);
    }
  }
}
