import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { PaymentService } from '../../../core/services/payment.service';
import { NotificationService } from '../../../core/services/notification.service';
import { PaymentResponse } from '../../../core/models/payment.model';

@Component({
  selector: 'app-payment-view',
  templateUrl: './payment-view.component.html',
  styleUrls: ['./payment-view.component.scss']
})
export class PaymentViewComponent implements OnInit {
  paymentIdInput: string = '';
  payment: PaymentResponse | null = null;
  isLoading = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private paymentService: PaymentService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe((params) => {
      const id = params.get('id');
      if (id) {
        this.paymentIdInput = id;
        this.fetchPayment(id);
      } else {
        this.route.queryParams.subscribe((q) => {
          if (q['paymentId']) {
            this.paymentIdInput = q['paymentId'];
            this.fetchPayment(this.paymentIdInput);
          }
        });
      }
    });
  }

  onSearch(): void {
    if (this.paymentIdInput.trim()) {
      this.fetchPayment(this.paymentIdInput.trim());
    }
  }

  fetchPayment(id: string): void {
    this.isLoading = true;
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

  goToConfirm(): void {
    if (this.payment) {
      this.router.navigate(['/payments/confirm'], {
        queryParams: { paymentId: this.payment.paymentId }
      });
    }
  }

  goToRefund(): void {
    if (this.payment) {
      this.router.navigate(['/payments/refund'], {
        queryParams: { paymentId: this.payment.paymentId }
      });
    }
  }

  goToPolicy(): void {
    if (this.payment) {
      this.router.navigate(['/policies/view', this.payment.policyId]);
    }
  }

  goToPremium(): void {
    if (this.payment) {
      this.router.navigate(['/premium/view', this.payment.policyId]);
    }
  }

  printReceipt(): void {
    window.print();
  }
}
