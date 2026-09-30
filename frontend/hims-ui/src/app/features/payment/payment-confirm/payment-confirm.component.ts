import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { PaymentService } from '../../../core/services/payment.service';
import { NotificationService } from '../../../core/services/notification.service';
import {
  PaymentResponse,
  PaymentConfirmRequest
} from '../../../core/models/payment.model';

@Component({
  selector: 'app-payment-confirm',
  templateUrl: './payment-confirm.component.html',
  styleUrls: ['./payment-confirm.component.scss']
})
export class PaymentConfirmComponent implements OnInit {
  confirmForm!: FormGroup;
  paymentIdInput: string = '';
  payment: PaymentResponse | null = null;

  isLoading = false;
  isConfirming = false;
  confirmedPayment: PaymentResponse | null = null;

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
    const randomRef = 'GW-' + Math.random().toString(36).substring(2, 9).toUpperCase();
    this.confirmForm = this.fb.group({
      gatewayReference: [randomRef, [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
      isSuccess: [true, [Validators.required]],
      failureReason: ['', [Validators.maxLength(500)]]
    });

    this.confirmForm.get('isSuccess')?.valueChanges.subscribe((isSuccess) => {
      const reasonControl = this.confirmForm.get('failureReason');
      if (!isSuccess) {
        reasonControl?.setValidators([Validators.required, Validators.minLength(3), Validators.maxLength(500)]);
      } else {
        reasonControl?.setValidators([Validators.maxLength(500)]);
      }
      reasonControl?.updateValueAndValidity();
    });
  }

  onSearch(): void {
    if (this.paymentIdInput.trim()) {
      this.fetchPayment(this.paymentIdInput.trim());
    }
  }

  fetchPayment(id: string): void {
    this.isLoading = true;
    this.confirmedPayment = null;

    this.paymentService.getPayment(id).subscribe({
      next: (res) => {
        this.payment = res;
        this.isLoading = false;
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

  onConfirm(): void {
    if (!this.payment || this.confirmForm.invalid) return;

    this.isConfirming = true;
    const req: PaymentConfirmRequest = {
      gatewayReference: this.confirmForm.value.gatewayReference.trim(),
      isSuccess: this.confirmForm.value.isSuccess,
      success: this.confirmForm.value.isSuccess,
      failureReason: this.confirmForm.value.isSuccess ? undefined : this.confirmForm.value.failureReason
    };

    this.paymentService.confirmPayment(this.payment.paymentId, req).subscribe({
      next: (res) => {
        this.isConfirming = false;
        this.confirmedPayment = res;
        this.payment = res;
        this.notificationService.success(
          `Payment successfully confirmed! Status: ${res.status}`
        );
      },
      error: (err) => {
        this.isConfirming = false;
        this.notificationService.error(
          err.error?.message || 'Failed to confirm payment transaction'
        );
      }
    });
  }

  goToView(): void {
    if (this.payment) {
      this.router.navigate(['/payments/view', this.payment.paymentId]);
    }
  }

  goToRefund(): void {
    if (this.payment) {
      this.router.navigate(['/payments/refund'], {
        queryParams: { paymentId: this.payment.paymentId }
      });
    }
  }
}
