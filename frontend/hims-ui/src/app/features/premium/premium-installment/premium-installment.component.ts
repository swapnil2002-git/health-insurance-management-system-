import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { PremiumService } from '../../../core/services/premium.service';
import { PolicyService } from '../../../core/services/policy.service';
import { NotificationService } from '../../../core/services/notification.service';
import {
  PremiumScheduleResponse,
  PremiumOutstandingResponse,
  PremiumInstallmentResponse,
  InstallmentPaymentRequest
} from '../../../core/models/premium.model';
import { PolicyResponse } from '../../../core/models/policy.model';

export interface PaymentModalData {
  type: 'success' | 'error';
  title: string;
  message: string;
  installmentNumber?: number;
  installmentId?: string;
  amountPaid?: number;
  outstandingAmount?: number;
  status?: string;
  paymentReference?: string;
  scheduleId?: string;
  policyNumber?: string;
  policyId?: string;
  paidDate?: Date;
  copyTxnSuccess?: boolean;
  copyPolicySuccess?: boolean;
  copyScheduleSuccess?: boolean;
}

@Component({
  selector: 'app-premium-installment',
  templateUrl: './premium-installment.component.html',
  styleUrls: ['./premium-installment.component.scss']
})
export class PremiumInstallmentComponent implements OnInit {
  policyInput: string = '';
  availableSchedules: PremiumScheduleResponse[] = [];
  selectedScheduleId: string = '';
  resolvedPolicy: PolicyResponse | null = null;
  schedule: PremiumScheduleResponse | null = null;
  outstanding: PremiumOutstandingResponse | null = null;

  isLoading = false;
  isPaying = false;

  // Selected installment for payment
  selectedInstallment: PremiumInstallmentResponse | null = null;
  paymentForm!: FormGroup;

  // Modal dialog for payment confirmation
  modalData: PaymentModalData | null = null;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private premiumService: PremiumService,
    private policyService: PolicyService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.initPaymentForm();

    this.route.queryParams.subscribe((params) => {
      const p = params['policyId'] || params['policyNumber'];
      if (p) {
        this.policyInput = p;
        this.searchPolicySchedules();
      }
    });
  }

  private initPaymentForm(): void {
    this.paymentForm = this.fb.group({
      amount: [0, [Validators.required, Validators.min(0.01)]],
      paymentReference: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]]
    });
  }

  searchPolicySchedules(): void {
    const raw = (this.policyInput || '').trim();
    if (!raw) {
      this.notificationService.warning('Please enter a Policy Number or Policy ID');
      return;
    }

    this.isLoading = true;
    this.availableSchedules = [];
    this.selectedScheduleId = '';
    this.schedule = null;
    this.outstanding = null;
    this.resolvedPolicy = null;
    this.selectedInstallment = null;

    const isUuid = /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/.test(raw);

    if (isUuid) {
      this.loadScheduleForPolicyId(raw);
      this.policyService.getPolicy(raw).subscribe({
        next: (p) => (this.resolvedPolicy = p),
        error: () => {}
      });
    } else {
      // Find policy by Policy Number
      this.policyService.getAllPolicies().subscribe({
        next: (policies) => {
          const match = (policies || []).find(
            (p) =>
              (p.policyNumber && p.policyNumber.toLowerCase() === raw.toLowerCase()) ||
              (p.policyId && p.policyId.toLowerCase() === raw.toLowerCase())
          );
          if (match) {
            this.resolvedPolicy = match;
            this.loadScheduleForPolicyId(match.policyId);
          } else {
            this.loadScheduleForPolicyId(raw);
          }
        },
        error: () => {
          this.loadScheduleForPolicyId(raw);
        }
      });
    }
  }

  private loadScheduleForPolicyId(policyId: string): void {
    this.premiumService.getPremiumByPolicy(policyId).subscribe({
      next: (sched) => {
        this.isLoading = false;
        this.availableSchedules = [sched];
        this.selectedScheduleId = sched.scheduleId;
        this.schedule = sched;
        this.loadOutstanding(policyId);
        this.notificationService.success(
          `Installment ledger loaded for Schedule #${sched.scheduleId}`
        );
      },
      error: (err) => {
        this.isLoading = false;
        this.schedule = null;
        this.availableSchedules = [];
        this.selectedScheduleId = '';
        this.notificationService.warning(
          err.error?.message || `No premium schedule found for policy: "${this.policyInput}"`
        );
      }
    });
  }

  onSelectSchedule(scheduleId: string): void {
    if (!scheduleId) return;
    this.selectedScheduleId = scheduleId;
    const found = this.availableSchedules.find((s) => s.scheduleId === scheduleId);
    if (found) {
      this.schedule = found;
      this.loadOutstanding(found.policyId);
    } else {
      this.isLoading = true;
      this.premiumService.getPremiumScheduleById(scheduleId).subscribe({
        next: (sched) => {
          this.isLoading = false;
          this.schedule = sched;
          this.loadOutstanding(sched.policyId);
        },
        error: () => {
          this.isLoading = false;
        }
      });
    }
  }

  private loadOutstanding(policyId: string): void {
    this.premiumService.getOutstanding(policyId).subscribe({
      next: (out) => {
        this.outstanding = out;
      },
      error: () => {
        // Outstanding is supplementary
      }
    });
  }

  openPayment(installment: PremiumInstallmentResponse): void {
    this.selectedInstallment = installment;
    const defaultRef = 'TXN-' + Math.floor(100000 + Math.random() * 900000);
    this.paymentForm.setValue({
      amount: installment.outstandingAmount > 0 ? installment.outstandingAmount : installment.amount,
      paymentReference: defaultRef
    });
    // Scroll down smoothly to payment card
    setTimeout(() => {
      const el = document.getElementById('payment-card-section');
      if (el) el.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }, 100);
  }

  cancelPayment(): void {
    this.selectedInstallment = null;
    this.paymentForm.reset();
  }

  submitPayment(): void {
    if (this.paymentForm.invalid || !this.selectedInstallment) return;

    this.isPaying = true;
    const formVal = this.paymentForm.value;
    const req: InstallmentPaymentRequest = {
      amount: Number(formVal.amount),
      paymentReference: formVal.paymentReference
    };

    const instNum = this.selectedInstallment.installmentNumber;

    this.premiumService
      .recordInstallmentPayment(this.selectedInstallment.installmentId, req)
      .subscribe({
        next: (res) => {
          this.isPaying = false;
          // Set modal data to display middle confirmation box
          this.modalData = {
            type: 'success',
            title: 'Payment Recorded Successfully!',
            message: `Payment of $${req.amount.toFixed(2)} for Installment #${instNum} has been confirmed.`,
            installmentNumber: res.installmentNumber || instNum,
            installmentId: res.installmentId,
            amountPaid: req.amount,
            outstandingAmount: res.outstandingAmount,
            status: res.status,
            paymentReference: req.paymentReference,
            scheduleId: this.schedule?.scheduleId,
            policyNumber: this.resolvedPolicy?.policyNumber,
            policyId: this.schedule?.policyId,
            paidDate: new Date()
          };

          this.notificationService.success(
            `Installment #${res.installmentNumber} payment recorded successfully! Status: ${res.status}`
          );
          this.selectedInstallment = null;
          this.paymentForm.reset();

          // Refresh current schedule
          if (this.selectedScheduleId) {
            this.onSelectSchedule(this.selectedScheduleId);
          } else if (this.schedule?.policyId) {
            this.loadScheduleForPolicyId(this.schedule.policyId);
          }
        },
        error: (err) => {
          this.isPaying = false;
          this.modalData = {
            type: 'error',
            title: 'Payment Processing Failed',
            message: err.error?.message || 'Failed to record installment payment. Please verify the amount and transaction reference.'
          };
          this.notificationService.error(
            err.error?.message || 'Failed to record installment payment'
          );
        }
      });
  }

  closeModal(): void {
    this.modalData = null;
  }

  copyToClipboard(text: string | undefined, field: string): void {
    if (!text) return;
    navigator.clipboard.writeText(text).then(() => {
      if (!this.modalData) return;
      if (field === 'txn') this.modalData.copyTxnSuccess = true;
      if (field === 'policy') this.modalData.copyPolicySuccess = true;
      if (field === 'schedule') this.modalData.copyScheduleSuccess = true;
      setTimeout(() => {
        if (this.modalData) {
          if (field === 'txn') this.modalData.copyTxnSuccess = false;
          if (field === 'policy') this.modalData.copyPolicySuccess = false;
          if (field === 'schedule') this.modalData.copyScheduleSuccess = false;
        }
      }, 2000);
    });
  }

  goToRecalculate(): void {
    if (this.schedule) {
      this.router.navigate(['/premium/recalculate'], {
        queryParams: { policyId: this.schedule.policyId }
      });
    } else {
      this.router.navigate(['/premium/recalculate']);
    }
  }

  goToView(): void {
    if (this.schedule) {
      this.router.navigate(['/premium/view', this.schedule.policyId]);
    } else {
      this.router.navigate(['/premium/view']);
    }
  }

  goToSchedules(): void {
    if (this.schedule) {
      this.router.navigate(['/premium/schedules'], {
        queryParams: { policyId: this.schedule.policyId }
      });
    } else {
      this.router.navigate(['/premium/schedules']);
    }
  }
}
