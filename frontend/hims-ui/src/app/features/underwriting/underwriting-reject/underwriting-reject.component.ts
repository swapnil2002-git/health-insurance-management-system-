import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { UnderwritingService } from '../../../core/services/underwriting.service';
import { NotificationService } from '../../../core/services/notification.service';
import { UnderwritingCaseResponse } from '../../../core/models/underwriting.model';

export interface UnderwritingFeedbackModal {
  type: 'success' | 'error';
  title: string;
  message: string;
  caseId?: string;
  quoteId?: string;
  customerId?: string;
  reason?: string;
  copyCaseSuccess?: boolean;
  copyQuoteSuccess?: boolean;
  copyCustomerSuccess?: boolean;
}

@Component({
  selector: 'app-underwriting-reject',
  templateUrl: './underwriting-reject.component.html',
  styleUrls: ['./underwriting-reject.component.scss']
})
export class UnderwritingRejectComponent implements OnInit {
  rejectForm!: FormGroup;
  caseIdInput: string = '';
  uwCase: UnderwritingCaseResponse | null = null;
  isLoading = false;
  isSubmitting = false;
  modalData: UnderwritingFeedbackModal | null = null;

  commonReasons: string[] = [
    'Risk assessment score exceeds maximum acceptable threshold',
    'Severe unmanaged chronic pre-existing medical conditions',
    'Material non-disclosure or discrepancy in application declaration',
    'High-risk occupation or hazardous recreational activities',
    'Previous adverse policy cancellation or fraudulent history'
  ];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private underwritingService: UnderwritingService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.route.queryParams.subscribe((params) => {
      if (params['caseId']) {
        this.caseIdInput = params['caseId'];
        this.fetchCase(this.caseIdInput);
      }
    });
  }

  private initForm(): void {
    this.rejectForm = this.fb.group({
      reason: ['', [Validators.required, Validators.minLength(5)]],
      notes: ['']
    });
  }

  fetchCase(caseId: string): void {
    if (!caseId?.trim()) return;
    this.isLoading = true;
    this.underwritingService.getCase(caseId.trim()).subscribe({
      next: (res) => {
        this.isLoading = false;
        this.uwCase = res;
      },
      error: () => {
        this.isLoading = false;
        this.uwCase = null;
      }
    });
  }

  onSearch(): void {
    if (this.caseIdInput) {
      this.fetchCase(this.caseIdInput);
    }
  }

  selectReason(r: string): void {
    this.rejectForm.patchValue({ reason: r });
  }

  onReject(): void {
    if (this.rejectForm.invalid || !this.uwCase || this.isSubmitting) {
      this.rejectForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    const req = this.rejectForm.value;

    this.underwritingService.rejectCase(this.uwCase.caseId, req).subscribe({
      next: (updated) => {
        this.isSubmitting = false;
        this.uwCase = updated;
        this.modalData = {
          type: 'success',
          title: 'Underwriting Case Declined',
          message: `Case ${updated.caseId} has been formally REJECTED. The decline decision and clinical/actuarial reason have been recorded.`,
          caseId: updated.caseId,
          quoteId: updated.quoteId,
          customerId: updated.customerId,
          reason: req.reason
        };
        this.notificationService.warning(
          `Case ${updated.caseId} REJECTED. Underwriting decision recorded.`
        );
      },
      error: (err) => {
        this.isSubmitting = false;
        const errMsg = err?.error?.message || err?.message || 'Failed to decline underwriting case.';
        this.modalData = {
          type: 'error',
          title: 'Decline Action Failed',
          message: errMsg,
          caseId: this.uwCase?.caseId
        };
        this.notificationService.error(errMsg);
      }
    });
  }

  closeModal(): void {
    this.modalData = null;
  }

  copyToClipboard(text: string | undefined, field: 'case' | 'quote' | 'customer'): void {
    if (!text || !this.modalData) return;
    navigator.clipboard.writeText(text).then(() => {
      if (!this.modalData) return;
      if (field === 'case') {
        this.modalData.copyCaseSuccess = true;
        setTimeout(() => { if (this.modalData) this.modalData.copyCaseSuccess = false; }, 2000);
      } else if (field === 'quote') {
        this.modalData.copyQuoteSuccess = true;
        setTimeout(() => { if (this.modalData) this.modalData.copyQuoteSuccess = false; }, 2000);
      } else if (field === 'customer') {
        this.modalData.copyCustomerSuccess = true;
        setTimeout(() => { if (this.modalData) this.modalData.copyCustomerSuccess = false; }, 2000);
      }
    });
  }

  goToCases(): void {
    this.router.navigate(['/underwriting/cases']);
  }

  goToView(): void {
    if (this.uwCase) {
      this.router.navigate(['/underwriting/view', this.uwCase.caseId]);
    }
  }
}
