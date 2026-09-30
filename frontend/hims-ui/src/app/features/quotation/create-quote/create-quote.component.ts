import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, FormGroupDirective, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription, forkJoin, of } from 'rxjs';
import { catchError, debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
import { QuotationService } from '../../../core/services/quotation.service';
import { ProductPlanService } from '../../../core/services/product-plan.service';
import { CustomerService } from '../../../core/services/customer.service';
import { CustomerContextService } from '../../../core/services/customer-context.service';
import { NotificationService } from '../../../core/services/notification.service';
import { PlanResponse } from '../../../core/models/product-plan.model';
import { CustomerResponse, MemberResponse } from '../../../core/models/customer.model';

export interface QuoteFeedbackModal {
  type: 'success' | 'error';
  title: string;
  message: string;
  quoteId?: string;
  quoteNumber?: string;
}

@Component({
  selector: 'app-create-quote',
  templateUrl: './create-quote.component.html',
  styleUrls: ['./create-quote.component.scss']
})
export class CreateQuoteComponent implements OnInit, OnDestroy {
  quoteForm!: FormGroup;
  plans: PlanResponse[] = [];
  activeCustomer: CustomerResponse | null = null;
  customerMembers: MemberResponse[] = [];
  isLoading = false;
  isSubmitting = false;
  isLoadingCustomer = false;
  lastLoadedCustomerId = '';
  modalData: QuoteFeedbackModal | null = null;
  copiedField: 'number' | 'id' | null = null;
  private subscription = new Subscription();

  relationshipOptions = [
    { value: 'SELF', label: 'Primary Insured (Self)' },
    { value: 'SPOUSE', label: 'Spouse' },
    { value: 'CHILD', label: 'Child / Dependent' },
    { value: 'PARENT', label: 'Parent' },
    { value: 'OTHER', label: 'Other Dependent' }
  ];

  genderOptions = [
    { value: 'MALE', label: 'Male' },
    { value: 'FEMALE', label: 'Female' },
    { value: 'OTHER', label: 'Other' }
  ];

  constructor(
    private fb: FormBuilder,
    private quotationService: QuotationService,
    private productPlanService: ProductPlanService,
    private customerService: CustomerService,
    public customerContext: CustomerContextService,
    private notificationService: NotificationService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadPlans();
    this.setupCustomerAutoDetection();
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  private initForm(): void {
    this.quoteForm = this.fb.group({
      customerId: ['', [Validators.required]],
      planId: ['', [Validators.required]],
      members: this.fb.array([])
    });

    if (this.membersArray.length === 0) {
      this.addMember();
    }
  }

  private setupCustomerAutoDetection(): void {
    // 1. Only auto-load from route query parameters (when navigated directly from customer pages/shortcuts)
    this.subscription.add(
      this.route.queryParams.subscribe((params) => {
        const queryCustId = params['customerId']?.trim();
        if (queryCustId) {
          this.quoteForm.patchValue({ customerId: queryCustId }, { emitEvent: false });
          this.loadCustomerAndMembers(queryCustId);
        } else {
          // Navigating from sidebar sub-tab without query parameters: keep form clean and empty!
          this.clearCustomerData(true);
        }
      })
    );

    // 3. Auto-load on Customer ID input change with debounce
    const custIdControl = this.quoteForm.get('customerId');
    if (custIdControl) {
      this.subscription.add(
        custIdControl.valueChanges.pipe(
          debounceTime(500),
          distinctUntilChanged()
        ).subscribe((val: string) => {
          const trimmed = val ? val.trim() : '';
          // Typical UUID length is 36 chars; auto-trigger if valid UUID or at least 32 characters
          if (trimmed && trimmed.length >= 32 && trimmed !== this.lastLoadedCustomerId) {
            this.loadCustomerAndMembers(trimmed);
          } else if (!trimmed) {
            this.clearCustomerData(false);
          }
        })
      );
    }
  }

  get membersArray(): FormArray {
    return this.quoteForm.get('members') as FormArray;
  }

  createMemberGroup(initialValues?: Partial<{ memberName: string; dateOfBirth: string; relationship: string; gender: string }>): FormGroup {
    return this.fb.group({
      memberName: [initialValues?.memberName || '', [Validators.required, Validators.minLength(2), Validators.maxLength(100), Validators.pattern(/^[a-zA-Z\s'-]+$/)]],
      dateOfBirth: [initialValues?.dateOfBirth || '', [Validators.required]],
      relationship: [initialValues?.relationship || 'SELF', [Validators.required]],
      gender: [initialValues?.gender || 'MALE', [Validators.required]]
    });
  }

  addMember(): void {
    if (this.membersArray.length >= 10) {
      this.notificationService.warning('Maximum of 10 members allowed per quotation.');
      return;
    }
    this.membersArray.push(this.createMemberGroup());
  }

  removeMember(index: number): void {
    if (this.membersArray.length > 1) {
      this.membersArray.removeAt(index);
    } else {
      this.notificationService.warning('At least one member is required for an insurance quote.');
    }
  }

  loadPlans(): void {
    this.isLoading = true;
    this.productPlanService.getPlans().subscribe({
      next: (plans) => {
        this.isLoading = false;
        this.plans = plans || [];
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  onCustomerIdBlur(): void {
    const custId = this.quoteForm.get('customerId')?.value?.trim();
    if (custId && custId !== this.lastLoadedCustomerId) {
      this.loadCustomerAndMembers(custId);
    }
  }

  onCustomerIdEnter(): void {
    const custId = this.quoteForm.get('customerId')?.value?.trim();
    if (custId && custId !== this.lastLoadedCustomerId) {
      this.loadCustomerAndMembers(custId);
    }
  }

  verifyCustomer(): void {
    const custId = this.quoteForm.get('customerId')?.value?.trim();
    if (!custId) {
      this.notificationService.warning('Please enter a Customer ID to verify.');
      return;
    }
    this.loadCustomerAndMembers(custId);
  }

  loadCustomerAndMembers(custId: string, prefetchedCustomer?: CustomerResponse): void {
    const cleanId = custId?.trim();
    if (!cleanId) return;

    if (cleanId === this.lastLoadedCustomerId && this.activeCustomer && this.membersArray.length > 0) {
      return;
    }

    this.isLoadingCustomer = true;
    const fetchCustomer$ = prefetchedCustomer
      ? of(prefetchedCustomer)
      : this.customerService.getCustomer(cleanId);

    fetchCustomer$.pipe(
      switchMap((customer) => {
        this.activeCustomer = customer;
        this.customerContext.setActiveCustomer(customer);
        return forkJoin({
          customer: of(customer),
          members: this.customerService.getMembers(cleanId).pipe(
            catchError(() => of([] as MemberResponse[]))
          )
        });
      })
    ).subscribe({
      next: ({ customer, members }) => {
        this.isLoadingCustomer = false;
        this.lastLoadedCustomerId = cleanId;
        this.customerMembers = members || [];
        this.populateMembersRoster(customer, this.customerMembers);
        this.notificationService.success(
          `Customer ${customer.firstName} ${customer.lastName} loaded with ${this.membersArray.length} insured member(s).`
        );
      },
      error: (err) => {
        this.isLoadingCustomer = false;
        this.activeCustomer = null;
        this.customerMembers = [];
        this.lastLoadedCustomerId = '';
        this.notificationService.error(
          err?.error?.message || `Customer '${cleanId}' not found. Please verify the ID.`
        );
      }
    });
  }

  populateMembersRoster(customer: CustomerResponse, members: MemberResponse[]): void {
    this.membersArray.clear();

    // 1. Primary Insured is the customer themselves (SELF)
    const customerFullName = [customer.firstName, customer.lastName].filter(Boolean).join(' ');
    this.membersArray.push(this.createMemberGroup({
      memberName: customerFullName,
      dateOfBirth: customer.dateOfBirth || '',
      relationship: 'SELF',
      gender: customer.gender || 'MALE'
    }));

    // 2. Add all registered family members / dependents
    if (members && members.length > 0) {
      members.forEach((m) => {
        if (m.relationshipToCustomer && m.relationshipToCustomer.toUpperCase() === 'SELF') {
          return;
        }
        const memberFullName = [m.firstName, m.lastName].filter(Boolean).join(' ');
        const relationship = this.mapRelationship(m.relationshipToCustomer);
        this.membersArray.push(this.createMemberGroup({
          memberName: memberFullName,
          dateOfBirth: m.dateOfBirth || '',
          relationship: relationship,
          gender: m.gender || 'MALE'
        }));
      });
    }
  }

  private mapRelationship(rel?: string): string {
    if (!rel) return 'OTHER';
    const upper = rel.toUpperCase();
    if (upper === 'SELF' || upper === 'SPOUSE' || upper === 'CHILD' || upper === 'PARENT') {
      return upper;
    }
    if (upper === 'SIBLING' || upper === 'DEPENDENT') {
      return 'OTHER';
    }
    return 'OTHER';
  }

  clearCustomerData(resetInput = true): void {
    this.activeCustomer = null;
    this.customerMembers = [];
    this.lastLoadedCustomerId = '';
    if (resetInput) {
      this.quoteForm.patchValue({ customerId: '' }, { emitEvent: false });
    }
    this.membersArray.clear();
    this.addMember();
    this.customerContext.setActiveCustomer(null);
  }

  reimportMembers(): void {
    const custId = this.quoteForm.get('customerId')?.value?.trim();
    if (!custId) {
      this.notificationService.warning('Please enter a Customer ID first.');
      return;
    }
    this.lastLoadedCustomerId = '';
    this.loadCustomerAndMembers(custId);
  }

  onSubmit(formDirective?: FormGroupDirective): void {
    if (this.quoteForm.invalid || this.isSubmitting) {
      this.quoteForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    const req = this.quoteForm.value;

    this.quotationService.createQuote(req).subscribe({
      next: (res) => {
        this.isSubmitting = false;
        this.modalData = {
          type: 'success',
          title: 'Quotation Created Successfully!',
          message: `Quotation ${res.quoteNumber} has been successfully initialized in DRAFT state. You can now calculate its premium or review its details.`,
          quoteId: res.quoteId,
          quoteNumber: res.quoteNumber
        };

        // Clean reset with formDirective so inputs DO NOT turn red!
        if (formDirective) {
          formDirective.resetForm({ planId: '' });
        } else {
          this.quoteForm.reset({ planId: '' });
        }
        this.clearCustomerData(true);
      },
      error: (err) => {
        this.isSubmitting = false;
        this.modalData = {
          type: 'error',
          title: 'Quotation Creation Error',
          message: err?.error?.message || 'Failed to create quotation draft. Please verify customer, plan, and insured member details.'
        };
      }
    });
  }

  closeModal(): void {
    this.modalData = null;
    this.copiedField = null;
  }

  copyText(text?: string, fieldName: 'number' | 'id' = 'id'): void {
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

  goToAllQuotes(): void {
    this.router.navigate(['/quotes']);
  }
}
