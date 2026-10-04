import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { PaymentService } from '../../../core/services/payment.service';
import { PremiumService } from '../../../core/services/premium.service';
import { PolicyService } from '../../../core/services/policy.service';
import { NotificationService } from '../../../core/services/notification.service';
import {
  PaymentInitiateRequest,
  PaymentResponse,
  PaymentMethod
} from '../../../core/models/payment.model';
import {
  PremiumScheduleResponse,
  PremiumInstallmentResponse
} from '../../../core/models/premium.model';
import { PolicyResponse } from '../../../core/models/policy.model';

export interface PaymentInitiateModalData {
  type: 'success' | 'error';
  title: string;
  message: string;
  paymentId?: string;
  policyNumber?: string;
  policyId?: string;
  installmentId?: string;
  installmentNumber?: number;
  amount?: number;
  currency?: string;
  paymentMethod?: string;
  status?: string;
  createdAt?: string;
  copyPaymentIdSuccess?: boolean;
  copyPolicySuccess?: boolean;
  copyInstallmentSuccess?: boolean;
}

@Component({
  selector: 'app-payment-initiate',
  templateUrl: './payment-initiate.component.html',
  styleUrls: ['./payment-initiate.component.scss']
})
export class PaymentInitiateComponent implements OnInit {
  initiateForm!: FormGroup;
  policyInput: string = '';
  resolvedPolicy: PolicyResponse | null = null;
  schedule: PremiumScheduleResponse | null = null;
  availableInstallments: PremiumInstallmentResponse[] = [];
  selectedInstallment: PremiumInstallmentResponse | null = null;

  isSubmitting = false;
  isLoadingPolicy = false;
  paymentReason: string = '';
  preselectedDeltaAmount: number | null = null;
  modalData: PaymentInitiateModalData | null = null;

  paymentMethods: { value: PaymentMethod; label: string; icon: string; description: string }[] = [
    { value: 'CREDIT_CARD', label: 'Credit Card', icon: 'credit_card', description: 'Visa, MasterCard, Amex' },
    { value: 'DEBIT_CARD', label: 'Debit Card', icon: 'payment', description: 'Direct Bank ATM / Debit Card' },
    { value: 'NET_BANKING', label: 'Net Banking', icon: 'account_balance', description: 'Secure Instant Online Banking' },
    { value: 'UPI', label: 'UPI / QR', icon: 'qr_code_2', description: 'Google Pay, PhonePe, BHIM' }
  ];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private paymentService: PaymentService,
    private premiumService: PremiumService,
    private policyService: PolicyService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.initForm();

    this.route.queryParams.subscribe((params) => {
      if (params['reason']) {
        this.paymentReason = params['reason'];
      }
      const p = params['policyId'] || params['policyNumber'];
      if (p) {
        this.policyInput = p;
        this.searchPolicyAndInstallments(params['installmentId'], params['amount']);
      } else if (params['installmentId']) {
        this.initiateForm.patchValue({ installmentId: params['installmentId'] });
      }
      if (params['amount']) {
        this.preselectedDeltaAmount = Number(params['amount']);
        this.initiateForm.patchValue({ amount: this.preselectedDeltaAmount });
      }
    });
  }

  private initForm(): void {
    this.initiateForm = this.fb.group({
      policyId: ['', [Validators.required]],
      installmentId: ['', [Validators.required]],
      amount: [null, [Validators.required, Validators.min(0.01)]],
      paymentMethod: ['CREDIT_CARD', [Validators.required]]
    });
  }

  searchPolicyAndInstallments(preselectedInstallmentId?: string, preselectedAmount?: string): void {
    const raw = (this.policyInput || '').trim();
    if (!raw) {
      this.notificationService.warning('Please enter a Policy Number or Policy ID');
      return;
    }

    const targetAmount = preselectedAmount || (this.preselectedDeltaAmount ? String(this.preselectedDeltaAmount) : undefined);

    this.isLoadingPolicy = true;
    this.schedule = null;
    this.availableInstallments = [];
    this.selectedInstallment = null;
    this.resolvedPolicy = null;
    this.initiateForm.patchValue({ installmentId: '', amount: targetAmount ? Number(targetAmount) : null });

    const isUuid = /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/.test(raw);

    if (isUuid) {
      this.initiateForm.patchValue({ policyId: raw });
      this.policyService.getPolicy(raw).subscribe({
        next: (p) => (this.resolvedPolicy = p),
        error: () => {}
      });
      this.fetchSchedule(raw, preselectedInstallmentId, targetAmount);
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
            this.initiateForm.patchValue({ policyId: match.policyId });
            this.fetchSchedule(match.policyId, preselectedInstallmentId, targetAmount);
          } else {
            this.initiateForm.patchValue({ policyId: raw });
            this.fetchSchedule(raw, preselectedInstallmentId, targetAmount);
          }
        },
        error: () => {
          this.initiateForm.patchValue({ policyId: raw });
          this.fetchSchedule(raw, preselectedInstallmentId, targetAmount);
        }
      });
    }
  }

  fetchSchedule(policyId: string, preselectedInstallmentId?: string, preselectedAmount?: string): void {
    this.premiumService.getPremiumByPolicy(policyId).subscribe({
      next: (sched) => {
        this.isLoadingPolicy = false;
        this.schedule = sched;
        this.availableInstallments = sched.installments || [];

        if (this.availableInstallments.length > 0) {
          if (preselectedInstallmentId) {
            const match = this.availableInstallments.find((i) => i.installmentId === preselectedInstallmentId);
            if (match) {
              this.selectInstallment(match, preselectedAmount ? Number(preselectedAmount) : undefined);
              return;
            }
          }

          if (preselectedAmount) {
            const targetNum = Number(preselectedAmount);
            const deltaMatch = this.availableInstallments.find((i) =>
              i.status !== 'PAID' && (i.outstandingAmount === targetNum || i.amount === targetNum)
            );
            if (deltaMatch) {
              this.selectInstallment(deltaMatch, targetNum);
              return;
            }
          }

          // Auto-select first pending or partially paid installment, preserving preselected amount
          const firstPending = this.availableInstallments.find((i) => i.status !== 'PAID') || this.availableInstallments[0];
          this.selectInstallment(firstPending, preselectedAmount ? Number(preselectedAmount) : undefined);
        }

        this.notificationService.success(
          `Found ${this.availableInstallments.length} installment(s) for Schedule #${sched.scheduleId.substring(0, 8)}`
        );
      },
      error: () => {
        this.isLoadingPolicy = false;
        this.schedule = null;
        this.availableInstallments = [];
        this.selectedInstallment = null;
        this.notificationService.warning(
          `No active premium schedule found for Policy: "${this.policyInput}"`
        );
      }
    });
  }


  onInstallmentSelect(installmentId: string): void {
    const found = this.availableInstallments.find((i) => i.installmentId === installmentId);
    if (found) {
      this.selectInstallment(found);
    }
  }

  selectInstallment(inst: PremiumInstallmentResponse, overrideAmount?: number): void {
    this.selectedInstallment = inst;
    const dueAmount = overrideAmount != null
      ? overrideAmount
      : (inst.outstandingAmount > 0 ? inst.outstandingAmount : inst.amount);

    this.initiateForm.patchValue({
      installmentId: inst.installmentId,
      amount: dueAmount
    });
  }

  selectMethod(method: PaymentMethod): void {
    this.initiateForm.patchValue({ paymentMethod: method });
  }

  onInitiate(): void {
    if (this.initiateForm.invalid) {
      this.initiateForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    const formVal = this.initiateForm.value;
    const req: PaymentInitiateRequest = {
      policyId: formVal.policyId.trim(),
      installmentId: formVal.installmentId.trim(),
      amount: Number(formVal.amount),
      paymentMethod: formVal.paymentMethod
    };

    this.paymentService.initiatePayment(req).subscribe({
      next: (res) => {
        this.isSubmitting = false;
        // Show centered popup modal with respective info and copyable payment ID
        this.modalData = {
          type: 'success',
          title: 'Secure Payment Initiated Successfully!',
          message: `Payment transaction of $${res.amount.toFixed(2)} has been securely initiated and is ready for gateway settlement.`,
          paymentId: res.paymentId,
          policyNumber: this.resolvedPolicy?.policyNumber,
          policyId: res.policyId,
          installmentId: req.installmentId,
          installmentNumber: this.selectedInstallment?.installmentNumber,
          amount: res.amount,
          currency: res.currency || 'USD',
          paymentMethod: res.paymentMethod,
          status: res.status,
          createdAt: res.createdAt
        };

        this.notificationService.success(
          `Payment transaction initiated! ID: ${res.paymentId.substring(0, 8)}...`
        );
      },
      error: (err) => {
        this.isSubmitting = false;
        this.modalData = {
          type: 'error',
          title: 'Payment Initiation Failed',
          message: err.error?.message || 'Failed to initiate payment transaction. Please verify your contract details.'
        };
        this.notificationService.error(
          err.error?.message || 'Failed to initiate payment transaction'
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
      if (field === 'payment') this.modalData.copyPaymentIdSuccess = true;
      if (field === 'policy') this.modalData.copyPolicySuccess = true;
      if (field === 'installment') this.modalData.copyInstallmentSuccess = true;
      setTimeout(() => {
        if (this.modalData) {
          if (field === 'payment') this.modalData.copyPaymentIdSuccess = false;
          if (field === 'policy') this.modalData.copyPolicySuccess = false;
          if (field === 'installment') this.modalData.copyInstallmentSuccess = false;
        }
      }, 2000);
    });
  }

  goToConfirm(): void {
    if (this.modalData?.paymentId) {
      const pId = this.modalData.paymentId;
      this.closeModal();
      this.router.navigate(['/payments/confirm'], {
        queryParams: { paymentId: pId }
      });
    }
  }

  goToView(): void {
    if (this.modalData?.paymentId) {
      const pId = this.modalData.paymentId;
      this.closeModal();
      this.router.navigate(['/payments/view', pId]);
    }
  }

  resetForm(): void {
    this.modalData = null;
    this.schedule = null;
    this.availableInstallments = [];
    this.selectedInstallment = null;
    this.resolvedPolicy = null;
    this.policyInput = '';
    this.initiateForm.reset({
      paymentMethod: 'CREDIT_CARD'
    });
  }
}
