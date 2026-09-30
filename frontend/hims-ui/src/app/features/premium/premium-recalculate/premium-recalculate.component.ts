import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { PremiumService } from '../../../core/services/premium.service';
import { NotificationService } from '../../../core/services/notification.service';
import {
  PremiumScheduleResponse,
  PremiumRecalculateRequest,
  PaymentFrequency
} from '../../../core/models/premium.model';

@Component({
  selector: 'app-premium-recalculate',
  templateUrl: './premium-recalculate.component.html',
  styleUrls: ['./premium-recalculate.component.scss']
})
export class PremiumRecalculateComponent implements OnInit {
  recalcForm!: FormGroup;
  policyIdInput: string = '';
  schedule: PremiumScheduleResponse | null = null;

  isLoading = false;
  isSubmitting = false;

  frequencies: { value: PaymentFrequency; label: string }[] = [
    { value: 'ANNUAL', label: 'Annual (1 installment/year)' },
    { value: 'QUARTERLY', label: 'Quarterly (4 installments/year)' },
    { value: 'MONTHLY', label: 'Monthly (12 installments/year)' }
  ];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private premiumService: PremiumService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.route.queryParams.subscribe((params) => {
      if (params['policyId']) {
        this.policyIdInput = params['policyId'];
        this.fetchSchedule(this.policyIdInput);
      }
    });
  }

  private initForm(): void {
    this.recalcForm = this.fb.group({
      basePremium: [null, [Validators.min(0)]],
      riderPremium: [null, [Validators.min(0)]],
      riskLoading: [null, [Validators.min(0)]],
      discount: [null, [Validators.min(0)]],
      tax: [null, [Validators.min(0)]],
      paymentFrequency: ['MONTHLY' as PaymentFrequency]
    });
  }

  fetchSchedule(policyId: string): void {
    if (!policyId?.trim()) return;
    this.isLoading = true;
    this.premiumService.getPremiumByPolicy(policyId.trim()).subscribe({
      next: (res) => {
        this.isLoading = false;
        this.schedule = res;
        this.recalcForm.patchValue({
          paymentFrequency: res.paymentFrequency
        });
      },
      error: () => {
        this.isLoading = false;
        this.schedule = null;
        this.notificationService.error('Could not find active premium schedule for policy: ' + policyId);
      }
    });
  }

  onSearch(): void {
    if (this.policyIdInput) {
      this.fetchSchedule(this.policyIdInput);
    }
  }

  onRecalculate(): void {
    if (this.recalcForm.invalid || !this.schedule || this.isSubmitting) {
      return;
    }

    this.isSubmitting = true;
    const val = this.recalcForm.value;

    const req: PremiumRecalculateRequest = {};
    if (val.basePremium !== null && val.basePremium !== '') req.basePremium = Number(val.basePremium);
    if (val.riderPremium !== null && val.riderPremium !== '') req.riderPremium = Number(val.riderPremium);
    if (val.riskLoading !== null && val.riskLoading !== '') req.riskLoading = Number(val.riskLoading);
    if (val.discount !== null && val.discount !== '') req.discount = Number(val.discount);
    if (val.tax !== null && val.tax !== '') req.tax = Number(val.tax);
    if (val.paymentFrequency) req.paymentFrequency = val.paymentFrequency;

    this.premiumService.recalculatePremium(this.schedule.policyId, req).subscribe({
      next: (updated) => {
        this.isSubmitting = false;
        this.schedule = updated;
        this.notificationService.success(
          `Premium recalculated successfully! New Total: $${updated.totalPremium}, Outstanding: $${updated.outstandingAmount}`
        );
      },
      error: () => {
        this.isSubmitting = false;
      }
    });
  }

  get paidInstallmentsCount(): number {
    return this.schedule?.installments.filter((i) => i.status === 'PAID').length || 0;
  }

  get remainingInstallmentsCount(): number {
    return this.schedule?.installments.filter((i) => i.status !== 'PAID').length || 0;
  }

  goToSchedules(): void {
    this.router.navigate(['/premium/schedules']);
  }

  goToPay(): void {
    if (this.schedule) {
      this.router.navigate(['/premium/installments'], { queryParams: { policyId: this.schedule.policyId } });
    }
  }
}
