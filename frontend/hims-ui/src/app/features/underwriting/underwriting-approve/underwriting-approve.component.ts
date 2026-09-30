import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { UnderwritingService } from '../../../core/services/underwriting.service';
import { NotificationService } from '../../../core/services/notification.service';
import { UnderwritingCaseResponse, UnderwritingDecisionType } from '../../../core/models/underwriting.model';

export interface UnderwritingFeedbackModal {
  type: 'success' | 'error';
  title: string;
  message: string;
  caseId?: string;
  quoteId?: string;
  customerId?: string;
  decisionType?: string;
  copyCaseSuccess?: boolean;
  copyQuoteSuccess?: boolean;
  copyCustomerSuccess?: boolean;
}

@Component({
  selector: 'app-underwriting-approve',
  templateUrl: './underwriting-approve.component.html',
  styleUrls: ['./underwriting-approve.component.scss']
})
export class UnderwritingApproveComponent implements OnInit {
  approveForm!: FormGroup;
  caseIdInput: string = '';
  uwCase: UnderwritingCaseResponse | null = null;
  isLoading = false;
  isSubmitting = false;
  modalData: UnderwritingFeedbackModal | null = null;
  availableCases: UnderwritingCaseResponse[] = [];
  customerIdInput: string = '';

  decisionOptions: { value: UnderwritingDecisionType; label: string; desc: string }[] = [
    { value: 'APPROVED', label: 'Standard Approval', desc: 'Standard terms without premium adjustments' },
    { value: 'APPROVED_WITH_LOADING', label: 'Approved with Premium Loading', desc: 'Higher premium due to elevated medical/lifestyle risk' },
    { value: 'APPROVED_WITH_EXCLUSION', label: 'Approved with Specific Exclusion', desc: 'Approval excluding pre-existing conditions or specific treatments' }
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
    this.loadAvailableCases();
    this.route.queryParams.subscribe((params) => {
      if (params['caseId']) {
        this.caseIdInput = params['caseId'];
        this.fetchCase(this.caseIdInput);
      } else if (params['customerId']) {
        this.customerIdInput = params['customerId'];
        this.searchByCustomer();
      }
    });
  }

  searchByCustomer(): void {
    const custId = this.customerIdInput.trim();
    if (!custId) {
      this.loadAvailableCases();
      return;
    }
    this.isLoading = true;
    this.underwritingService.getCasesByCustomer(custId).subscribe({
      next: (cases) => {
        this.isLoading = false;
        this.availableCases = (cases || []).sort((a, b) => {
          if (a.status !== 'COMPLETED' && b.status === 'COMPLETED') return -1;
          if (a.status === 'COMPLETED' && b.status !== 'COMPLETED') return 1;
          return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime();
        });
        if (this.availableCases.length > 0) {
          const firstOpen = this.availableCases.find((c) => c.status !== 'COMPLETED') || this.availableCases[0];
          this.onCaseSelect(firstOpen.caseId);
          this.notificationService.success(`Loaded ${this.availableCases.length} case(s) from DB for customer.`);
        } else {
          this.notificationService.info('No underwriting cases found in DB for this Customer ID.');
        }
      },
      error: () => {
        this.isLoading = false;
        this.notificationService.error('Failed to query customer cases from database.');
      }
    });
  }

  loadAvailableCases(): void {
    this.underwritingService.getAllCases().subscribe({
      next: (cases) => {
        this.availableCases = (cases || []).sort((a, b) => {
          if (a.status !== 'COMPLETED' && b.status === 'COMPLETED') return -1;
          if (a.status === 'COMPLETED' && b.status !== 'COMPLETED') return 1;
          return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime();
        });
      },
      error: () => {
        this.availableCases = this.underwritingService.getRecentCases();
      }
    });
  }

  onCaseSelect(caseId: string): void {
    if (!caseId) return;
    this.caseIdInput = caseId;
    this.fetchCase(caseId);
  }

  private initForm(): void {
    this.approveForm = this.fb.group({
      decisionType: ['APPROVED', [Validators.required]],
      reason: ['Standard underwriting criteria satisfied.'],
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

  onApprove(): void {
    if (this.approveForm.invalid || !this.uwCase || this.isSubmitting) {
      this.approveForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    const req = this.approveForm.value;

    this.underwritingService.approveCase(this.uwCase.caseId, req).subscribe({
      next: (updated) => {
        this.isSubmitting = false;
        this.uwCase = updated;
        this.modalData = {
          type: 'success',
          title: 'Underwriting Approved Successfully!',
          message: `Case ${updated.caseId} has been authorized (${req.decisionType}). An UnderwritingApprovedEvent has been published to Kafka, enabling Policy Service to issue the insurance policy.`,
          caseId: updated.caseId,
          quoteId: updated.quoteId,
          customerId: updated.customerId,
          decisionType: req.decisionType
        };
        this.notificationService.success(
          `Case ${updated.caseId} APPROVED (${req.decisionType})!`
        );
      },
      error: (err) => {
        this.isSubmitting = false;
        const errMsg = err?.error?.message || err?.message || 'Failed to approve underwriting case.';
        this.modalData = {
          type: 'error',
          title: 'Approval Failed',
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
