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
  selector: 'app-underwriting-refer',
  templateUrl: './underwriting-refer.component.html',
  styleUrls: ['./underwriting-refer.component.scss']
})
export class UnderwritingReferComponent implements OnInit {
  referForm!: FormGroup;
  caseIdInput: string = '';
  uwCase: UnderwritingCaseResponse | null = null;
  isLoading = false;
  isSubmitting = false;
  modalData: UnderwritingFeedbackModal | null = null;

  referralReasons: string[] = [
    'Requires senior chief medical officer secondary review',
    'Specialist pathology or tele-medical examination records pending',
    'High sum insured requires higher discretionary underwriting authority',
    'Complex multi-morbidity risk profile needing collaborative assessment',
    'Financial underwriting / income verification audit required'
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
    this.referForm = this.fb.group({
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
    this.referForm.patchValue({ reason: r });
  }

  onRefer(): void {
    if (this.referForm.invalid || !this.uwCase || this.isSubmitting) {
      this.referForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    const req = this.referForm.value;

    this.underwritingService.referCase(this.uwCase.caseId, req).subscribe({
      next: (updated) => {
        this.isSubmitting = false;
        this.uwCase = updated;
        this.modalData = {
          type: 'success',
          title: 'Case Escalated / Referred',
          message: `Case ${updated.caseId} has been escalated for senior underwriter review. Its status is now UNDER_REVIEW.`,
          caseId: updated.caseId,
          quoteId: updated.quoteId,
          customerId: updated.customerId,
          reason: req.reason
        };
        this.notificationService.info(
          `Case ${updated.caseId} escalated / referred. Status updated to UNDER_REVIEW.`
        );
      },
      error: (err) => {
        this.isSubmitting = false;
        const errMsg = err?.error?.message || err?.message || 'Failed to refer underwriting case.';
        this.modalData = {
          type: 'error',
          title: 'Referral Failed',
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
