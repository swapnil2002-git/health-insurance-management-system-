import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { UnderwritingService } from '../../../core/services/underwriting.service';
import { QuotationService } from '../../../core/services/quotation.service';
import { CustomerService } from '../../../core/services/customer.service';
import { CustomerContextService } from '../../../core/services/customer-context.service';
import { NotificationService } from '../../../core/services/notification.service';
import { UnderwritingCaseResponse } from '../../../core/models/underwriting.model';
import { QuoteResponse } from '../../../core/models/quotation.model';

@Component({
  selector: 'app-underwriting-cases',
  templateUrl: './underwriting-cases.component.html',
  styleUrls: ['./underwriting-cases.component.scss']
})
export class UnderwritingCasesComponent implements OnInit {
  createForm!: FormGroup;

  // Search & Filter State
  customerIdQuery: string = '';
  quoteIdQuery: string = '';
  searchCaseId: string = '';

  selectedCaseId: string = '';
  selectedCase: UnderwritingCaseResponse | null = null;
  customerCases: UnderwritingCaseResponse[] = [];
  selectedCustomerName: string = '';

  get recentCases(): UnderwritingCaseResponse[] {
    return this.customerCases;
  }

  quotes: QuoteResponse[] = [];
  showCreateForm = false;
  isLoading = false;
  isLoadingCase = false;
  isLoadingCases = false;
  isSubmitting = false;
  copiedField: string | null = null;

  constructor(
    private fb: FormBuilder,
    private underwritingService: UnderwritingService,
    private quotationService: QuotationService,
    private customerService: CustomerService,
    public customerContext: CustomerContextService,
    private notificationService: NotificationService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadQuotes();

    this.route.queryParams.subscribe((params) => {
      if (params['customerId']) {
        this.customerIdQuery = params['customerId'];
        this.searchCasesByCustomer();
      } else if (params['quoteId']) {
        this.quoteIdQuery = params['quoteId'];
        this.searchCasesByQuote();
      } else if (params['caseId']) {
        this.selectedCaseId = params['caseId'];
        this.loadCaseDetails(this.selectedCaseId);
      } else {
        // Check active customer context
        const activeCust = this.customerContext.getActiveCustomer();
        if (activeCust?.customerId) {
          this.customerIdQuery = activeCust.customerId;
          this.searchCasesByCustomer();
        } else {
          // Default: load all recent cases from DB
          this.loadAllCasesFromDb();
        }
      }
    });

    const activeCust = this.customerContext.getActiveCustomer();
    if (activeCust) {
      this.createForm.patchValue({ customerId: activeCust.customerId });
    }
  }

  private initForm(): void {
    this.createForm = this.fb.group({
      customerId: ['', [Validators.required]],
      quoteId: ['', [Validators.required]],
      assessmentId: ['', [Validators.required]]
    });
  }

  searchCasesByCustomer(): void {
    const custId = this.customerIdQuery.trim();
    if (!custId) {
      this.loadAllCasesFromDb();
      return;
    }

    this.isLoadingCases = true;
    this.selectedCustomerName = '';
    this.customerCases = [];

    // 1. Fetch cases for this customer directly from DB via underwriting-service
    this.underwritingService.getCasesByCustomer(custId).subscribe({
      next: (cases) => {
        this.isLoadingCases = false;
        this.customerCases = this.sortCases(cases || []);
        if (this.customerCases.length > 0) {
          this.notificationService.success(`Loaded ${this.customerCases.length} case(s) from DB for Customer.`);
          // If a case is already selected or we have an open case, select it
          const openCase = this.customerCases.find((c) => c.status !== 'COMPLETED');
          if (openCase) {
            this.onSelectCase(openCase.caseId);
          } else {
            this.onSelectCase(this.customerCases[0].caseId);
          }
        } else {
          this.notificationService.info('No underwriting cases found in database for this Customer ID.');
          this.selectedCase = null;
          this.selectedCaseId = '';
        }
      },
      error: () => {
        this.isLoadingCases = false;
        this.notificationService.error('Failed to query customer cases from database.');
      }
    });

    // 2. Fetch customer details to display customer name
    this.customerService.getCustomer(custId).subscribe({
      next: (cust) => {
        if (cust) {
          this.selectedCustomerName = `${cust.firstName || ''} ${cust.lastName || ''}`.trim();
        }
      },
      error: () => {
        this.selectedCustomerName = '';
      }
    });
  }

  searchCasesByQuote(): void {
    const qId = this.quoteIdQuery.trim();
    if (!qId) return;

    this.isLoadingCases = true;
    this.customerCases = [];

    this.underwritingService.getCasesByQuote(qId).subscribe({
      next: (cases) => {
        this.isLoadingCases = false;
        this.customerCases = this.sortCases(cases || []);
        if (this.customerCases.length > 0) {
          this.notificationService.success(`Loaded ${this.customerCases.length} case(s) for Quote ID.`);
          this.onSelectCase(this.customerCases[0].caseId);
        } else {
          this.notificationService.info('No underwriting cases found in DB for this Quote ID.');
          this.selectedCase = null;
          this.selectedCaseId = '';
        }
      },
      error: () => {
        this.isLoadingCases = false;
        this.notificationService.error('Failed to query cases for Quote ID.');
      }
    });
  }

  loadAllCasesFromDb(): void {
    this.isLoadingCases = true;
    this.underwritingService.getAllCases().subscribe({
      next: (cases) => {
        this.isLoadingCases = false;
        this.customerCases = this.sortCases(cases || []);
      },
      error: () => {
        this.isLoadingCases = false;
      }
    });
  }

  loadRecentCases(): void {
    if (this.customerIdQuery.trim()) {
      this.searchCasesByCustomer();
    } else {
      this.loadAllCasesFromDb();
    }
  }

  private sortCases(cases: UnderwritingCaseResponse[]): UnderwritingCaseResponse[] {
    return cases.sort((a, b) => {
      if (a.status !== 'COMPLETED' && b.status === 'COMPLETED') return -1;
      if (a.status === 'COMPLETED' && b.status !== 'COMPLETED') return 1;
      return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime();
    });
  }

  loadQuotes(): void {
    this.quotationService.getAllQuotes().subscribe({
      next: (quotes) => {
        this.quotes = quotes || [];
      },
      error: () => {}
    });
  }

  onDirectCaseSearch(): void {
    if (!this.searchCaseId.trim()) return;
    this.isLoading = true;
    this.underwritingService.getCase(this.searchCaseId.trim()).subscribe({
      next: (res) => {
        this.isLoading = false;
        this.selectedCase = res;
        this.selectedCaseId = res.caseId;
        this.router.navigate(['/underwriting/view', res.caseId]);
      },
      error: (err) => {
        this.isLoading = false;
        this.notificationService.error(err.error?.message || 'Case not found in database.');
      }
    });
  }

  onSelectCase(caseId: string): void {
    if (!caseId) {
      this.selectedCaseId = '';
      this.selectedCase = null;
      return;
    }
    this.selectedCaseId = caseId;
    this.loadCaseDetails(caseId);
  }

  loadCaseDetails(caseId: string): void {
    if (!caseId?.trim()) return;
    this.isLoadingCase = true;
    this.underwritingService.getCase(caseId.trim()).subscribe({
      next: (res) => {
        this.isLoadingCase = false;
        this.selectedCase = res;
      },
      error: () => {
        this.isLoadingCase = false;
        this.selectedCase = null;
        this.notificationService.warning('Failed to load selected case details from database.');
      }
    });
  }

  onCreateCase(): void {
    if (this.createForm.invalid || this.isSubmitting) {
      this.createForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    const req = this.createForm.value;

    this.underwritingService.createCase(req).subscribe({
      next: (res) => {
        this.isSubmitting = false;
        this.notificationService.success(`Underwriting Case ${res.caseId} initialized successfully in DB!`);
        this.showCreateForm = false;
        this.customerIdQuery = res.customerId;
        this.searchCasesByCustomer();
        this.router.navigate(['/underwriting/view', res.caseId]);
      },
      error: () => {
        this.isSubmitting = false;
      }
    });
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

  viewCase(caseId: string): void {
    this.router.navigate(['/underwriting/view', caseId]);
  }

  approveCase(caseId: string): void {
    this.router.navigate(['/underwriting/approve'], { queryParams: { caseId } });
  }

  rejectCase(caseId: string): void {
    this.router.navigate(['/underwriting/reject'], { queryParams: { caseId } });
  }

  referCase(caseId: string): void {
    this.router.navigate(['/underwriting/refer'], { queryParams: { caseId } });
  }
}
