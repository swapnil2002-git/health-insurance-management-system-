import { Component, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, FormGroupDirective, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { RiskService } from '../../../core/services/risk.service';
import { CustomerService } from '../../../core/services/customer.service';
import { QuotationService } from '../../../core/services/quotation.service';
import { CustomerContextService } from '../../../core/services/customer-context.service';
import { NotificationService } from '../../../core/services/notification.service';
import { CustomerResponse } from '../../../core/models/customer.model';
import { QuoteResponse } from '../../../core/models/quotation.model';

export interface RiskFeedbackModal {
  type: 'success' | 'error';
  title: string;
  message: string;
  assessmentId?: string;
  customerId?: string;
  quoteId?: string;
  status?: string;
}

@Component({
  selector: 'app-risk-assess',
  templateUrl: './risk-assess.component.html',
  styleUrls: ['./risk-assess.component.scss']
})
export class RiskAssessComponent implements OnInit {
  assessForm!: FormGroup;
  activeCustomer: CustomerResponse | null = null;
  quotes: QuoteResponse[] = [];
  allQuotes: QuoteResponse[] = [];
  isSubmitting = false;
  isVerifyingCustomer = false;
  isLoadingQuotes = false;
  modalData: RiskFeedbackModal | null = null;
  copiedField: string | null = null;
  lastLoadedCustomerId = '';

  standardPresets = [
    { name: 'Smoker', value: 'NO', description: 'Tobacco or nicotine consumption frequency' },
    { name: 'ChronicCondition', value: 'NONE', description: 'Pre-existing chronic illness (Diabetes, Cardiac, Hypertension)' },
    { name: 'AlcoholConsumption', value: 'MODERATE', description: 'Alcohol consumption habits' },
    { name: 'HazardousOccupation', value: 'NO', description: 'High-risk occupation or industrial hazards' },
    { name: 'PriorHospitalization', value: 'NONE', description: 'Hospitalization history within past 3 years' }
  ];

  constructor(
    private fb: FormBuilder,
    private riskService: RiskService,
    private customerService: CustomerService,
    private quotationService: QuotationService,
    public customerContext: CustomerContextService,
    private notificationService: NotificationService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.preloadAllQuotes();

    // Check query params: only auto-fill if explicitly passed (e.g. from Decision page)
    this.route.queryParams.subscribe((params) => {
      const qCustId = params['customerId'];
      const qQuoteId = params['quoteId'];

      if (qCustId) {
        this.assessForm.patchValue({ customerId: qCustId });
        if (qQuoteId) {
          this.assessForm.patchValue({ quoteId: qQuoteId });
        }
        this.verifyCustomerAndLoadQuotes(qCustId, qQuoteId);
      } else {
        // Navigating via left sidebar: fields start completely clean and empty!
        this.assessForm.patchValue({ customerId: '', quoteId: '' });
        this.activeCustomer = null;
        this.quotes = [];
        this.lastLoadedCustomerId = '';
      }
    });

    // Populate with default questionnaire baseline
    this.loadStandardQuestionnaire();
  }

  private initForm(): void {
    this.assessForm = this.fb.group({
      customerId: ['', [Validators.required]],
      quoteId: ['', [Validators.required]],
      factors: this.fb.array([])
    });
  }

  get factorsArray(): FormArray {
    return this.assessForm.get('factors') as FormArray;
  }

  createFactorGroup(initialValues?: Partial<{ factorName: string; factorValue: string; description: string }>): FormGroup {
    return this.fb.group({
      factorName: [initialValues?.factorName || '', [Validators.required]],
      factorValue: [initialValues?.factorValue || '', [Validators.required]],
      description: [initialValues?.description || '']
    });
  }

  addFactor(): void {
    this.factorsArray.push(this.createFactorGroup());
  }

  removeFactor(index: number): void {
    if (this.factorsArray.length > 1) {
      this.factorsArray.removeAt(index);
    } else {
      this.notificationService.warning('At least one risk factor is required for risk assessment.');
    }
  }

  loadStandardQuestionnaire(): void {
    this.factorsArray.clear();
    this.standardPresets.forEach((p) => {
      this.factorsArray.push(this.createFactorGroup({
        factorName: p.name,
        factorValue: p.value,
        description: p.description
      }));
    });
  }

  preloadAllQuotes(): void {
    this.quotationService.getAllQuotes().subscribe({
      next: (quotes) => {
        this.allQuotes = quotes || [];
      },
      error: () => {}
    });
  }

  onCustomerIdBlur(): void {
    const custId = this.assessForm.get('customerId')?.value?.trim();
    if (custId && custId !== this.lastLoadedCustomerId) {
      this.verifyCustomerAndLoadQuotes(custId);
    }
  }

  onCustomerIdEnter(): void {
    const custId = this.assessForm.get('customerId')?.value?.trim();
    if (custId && custId !== this.lastLoadedCustomerId) {
      this.verifyCustomerAndLoadQuotes(custId);
    }
  }

  onSearchCustomer(): void {
    const custId = this.assessForm.get('customerId')?.value?.trim();
    if (!custId) {
      this.notificationService.warning('Please enter a Customer ID to verify.');
      return;
    }
    this.verifyCustomerAndLoadQuotes(custId);
  }

  clearCustomer(): void {
    this.activeCustomer = null;
    this.quotes = [];
    this.lastLoadedCustomerId = '';
    this.assessForm.patchValue({ customerId: '', quoteId: '' });
  }

  verifyCustomerAndLoadQuotes(custId: string, targetQuoteId?: string): void {
    const cleanId = custId?.trim();
    if (!cleanId) return;
    this.lastLoadedCustomerId = cleanId;

    this.isVerifyingCustomer = true;
    this.isLoadingQuotes = true;

    forkJoin({
      customer: this.customerService.getCustomer(cleanId).pipe(catchError(() => of(null))),
      allQuotes: this.quotationService.getAllQuotes().pipe(catchError(() => of([])))
    }).subscribe({
      next: ({ customer, allQuotes }) => {
        this.isVerifyingCustomer = false;
        this.isLoadingQuotes = false;
        this.allQuotes = allQuotes || [];

        if (customer) {
          this.activeCustomer = customer;
          this.customerContext.setActiveCustomer(customer);
          this.notificationService.success(`Customer verified: ${customer.firstName} ${customer.lastName}`);
        } else {
          this.activeCustomer = null;
          this.notificationService.warning('Customer ID not found in HIMS registry.');
        }

        // Filter quotes for this specific customer
        const custQuotes = (this.allQuotes || []).filter((q) => q.customerId === cleanId);
        this.quotes = custQuotes;

        if (targetQuoteId) {
          this.assessForm.patchValue({ quoteId: targetQuoteId });
        } else if (custQuotes.length === 1) {
          // Auto-select if exactly 1 quote
          this.assessForm.patchValue({ quoteId: custQuotes[0].quoteId });
        } else if (custQuotes.length === 0) {
          this.notificationService.info('No quotations found for this customer. Please create a quote first.');
        }
      },
      error: () => {
        this.isVerifyingCustomer = false;
        this.isLoadingQuotes = false;
      }
    });
  }

  onSubmit(formDirective?: FormGroupDirective): void {
    if (this.assessForm.invalid || this.isSubmitting) {
      this.assessForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    const req = this.assessForm.value;

    this.riskService.createAssessment(req).subscribe({
      next: (res) => {
        this.isSubmitting = false;
        this.modalData = {
          type: 'success',
          title: 'Risk Assessment Initialized Successfully!',
          message: `Underwriting risk assessment case has been created in ${res.status || 'CREATED'} status. You can now calculate risk scores and evaluate risk classifications.`,
          assessmentId: res.assessmentId,
          customerId: res.customerId,
          quoteId: res.quoteId,
          status: res.status || 'CREATED'
        };

        // Clean reset with formDirective so inputs DO NOT turn red!
        if (formDirective) {
          formDirective.resetForm();
        } else {
          this.assessForm.reset();
        }
        this.activeCustomer = null;
        this.quotes = [];
        this.lastLoadedCustomerId = '';
        this.loadStandardQuestionnaire();
      },
      error: (err) => {
        this.isSubmitting = false;
        this.modalData = {
          type: 'error',
          title: 'Assessment Creation Error',
          message: err?.error?.message || 'Failed to initialize risk assessment. Please check customer, quote, and risk factors.'
        };
      }
    });
  }

  closeModal(): void {
    this.modalData = null;
    this.copiedField = null;
  }

  copyText(text?: string, fieldName: string = 'id'): void {
    if (!text) return;
    navigator.clipboard.writeText(text).then(() => {
      this.copiedField = fieldName;
      setTimeout(() => {
        if (this.copiedField === fieldName) {
          this.copiedField = null;
        }
      }, 2000);
    });
  }

  goToCalculate(): void {
    this.router.navigate(['/risk/calculate']);
  }

  goToView(): void {
    this.router.navigate(['/risk/view']);
  }
}
