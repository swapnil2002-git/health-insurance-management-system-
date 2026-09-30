import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { PaymentService } from '../../../core/services/payment.service';
import { NotificationService } from '../../../core/services/notification.service';
import { PaymentResponse } from '../../../core/models/payment.model';

@Component({
  selector: 'app-payment-list',
  templateUrl: './payment-list.component.html',
  styleUrls: ['./payment-list.component.scss']
})
export class PaymentListComponent implements OnInit {
  policyIdInput: string = '';
  payments: PaymentResponse[] = [];
  recentPayments: PaymentResponse[] = [];

  isLoading = false;
  hasSearched = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private paymentService: PaymentService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.loadAllPayments();

    this.route.queryParams.subscribe((params) => {
      if (params['policyId']) {
        this.policyIdInput = params['policyId'];
        this.fetchPaymentsByPolicy(this.policyIdInput);
      }
    });
  }

  loadAllPayments(): void {
    this.isLoading = true;
    this.paymentService.getAllPayments().subscribe({
      next: (res) => {
        this.recentPayments = res || [];
        this.isLoading = false;
      },
      error: () => {
        this.recentPayments = this.paymentService.getRecentPayments();
        this.isLoading = false;
      }
    });
  }

  onSearch(): void {
    if (this.policyIdInput.trim()) {
      this.fetchPaymentsByPolicy(this.policyIdInput.trim());
    } else {
      this.hasSearched = false;
      this.payments = [];
    }
  }

  fetchPaymentsByPolicy(policyId: string): void {
    this.isLoading = true;
    this.hasSearched = true;

    this.paymentService.getPaymentsByPolicy(policyId).subscribe({
      next: (res) => {
        this.payments = res || [];
        this.isLoading = false;
        // refresh recent
        this.recentPayments = this.paymentService.getRecentPayments();
      },
      error: (err) => {
        this.isLoading = false;
        this.payments = [];
        this.notificationService.error(
          err.error?.message || `Failed to fetch payments for policy ID: ${policyId}`
        );
      }
    });
  }

  get totalVolume(): number {
    const list = this.hasSearched ? this.payments : this.recentPayments;
    return list.reduce((acc, p) => acc + (p.status === 'SUCCESS' ? Number(p.amount) : 0), 0);
  }

  get successCount(): number {
    const list = this.hasSearched ? this.payments : this.recentPayments;
    return list.filter((p) => p.status === 'SUCCESS').length;
  }

  get pendingCount(): number {
    const list = this.hasSearched ? this.payments : this.recentPayments;
    return list.filter((p) => p.status === 'INITIATED' || p.status === 'PENDING').length;
  }

  get refundedCount(): number {
    const list = this.hasSearched ? this.payments : this.recentPayments;
    return list.filter((p) => p.status === 'REFUNDED').length;
  }

  viewPayment(id: string): void {
    this.router.navigate(['/payments/view', id]);
  }

  confirmPayment(id: string): void {
    this.router.navigate(['/payments/confirm'], {
      queryParams: { paymentId: id }
    });
  }

  refundPayment(id: string): void {
    this.router.navigate(['/payments/refund'], {
      queryParams: { paymentId: id }
    });
  }
}
