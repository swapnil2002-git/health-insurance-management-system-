import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { PaymentService } from '../../../core/services/payment.service';
import { NotificationService } from '../../../core/services/notification.service';
import {
  PaymentResponse,
  RefundResponse,
  RefundRequest
} from '../../../core/models/payment.model';

@Component({
  selector: 'app-payment-refund',
  templateUrl: './payment-refund.component.html',
  styleUrls: ['./payment-refund.component.scss']
})
export class PaymentRefundComponent implements OnInit {
  refundForm!: FormGroup;
  paymentIdInput: string = '';
  payment: PaymentResponse | null = null;
  refundResult: RefundResponse | null = null;

  isLoading = false;
  isProcessing = false;

  commonReasons: string[] = [
    'Duplicate payment charge on customer bank account',
    'Customer cancelled policy during cooling-off statutory grace period',
    'Actuarial rate adjustment / excess premium paid',
    'Overbilling error / duplicate installment transaction'
  ];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private paymentService: PaymentService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.initForm();

    this.route.queryParams.subscribe((params) => {
      if (params['paymentId']) {
        this.paymentIdInput = params['paymentId'];
        this.fetchPayment(this.paymentIdInput);
      }
    });
  }

  private initForm(): void {
    this.refundForm = this.fb.group({
      amount: [null, [Validators.required, Validators.min(0.01)]],
      reason: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(500)]]
    });
  }

  onSearch(): void {
    if (this.paymentIdInput.trim()) {
      this.fetchPayment(this.paymentIdInput.trim());
    }
  }

  fetchPayment(id: string): void {
    this.isLoading = true;
    this.refundResult = null;

    this.paymentService.getPayment(id).subscribe({
      next: (res) => {
        this.payment = res;
        this.isLoading = false;
        if (res.status === 'SUCCESS') {
          this.refundForm.patchValue({
            amount: res.amount,
            reason: this.commonReasons[0]
          });
        }
      },
      error: (err) => {
        this.isLoading = false;
        this.payment = null;
        this.notificationService.error(
          err.error?.message || `Payment transaction not found for ID: ${id}`
        );
      }
    });
  }

  selectCommonReason(reason: string): void {
    this.refundForm.patchValue({ reason });
  }

  onRefund(): void {
    if (!this.payment || this.refundForm.invalid) return;

    this.isProcessing = true;
    const req: RefundRequest = {
      amount: Number(this.refundForm.value.amount),
      reason: this.refundForm.value.reason.trim()
    };

    this.paymentService.processRefund(this.payment.paymentId, req).subscribe({
      next: (res) => {
        this.isProcessing = false;
        this.refundResult = res;
        this.notificationService.success(
          `Refund processed successfully! Reference: ${res.refundReference}`
        );
        // Refresh payment
        this.fetchPayment(this.payment!.paymentId);
      },
      error: (err) => {
        this.isProcessing = false;
        this.notificationService.error(
          err.error?.message || 'Failed to process refund request'
        );
      }
    });
  }

  goToView(): void {
    if (this.payment) {
      this.router.navigate(['/payments/view', this.payment.paymentId]);
    }
  }
}
