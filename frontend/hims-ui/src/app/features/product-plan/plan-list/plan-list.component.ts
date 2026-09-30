import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ProductPlanService } from '../../../core/services/product-plan.service';
import { NotificationService } from '../../../core/services/notification.service';
import { ProductResponse, PlanResponse, PlanDetailResponse } from '../../../core/models/product-plan.model';

@Component({
  selector: 'app-plan-list',
  templateUrl: './plan-list.component.html',
  styleUrls: ['./plan-list.component.scss']
})
export class PlanListComponent implements OnInit {
  planForm!: FormGroup;
  plans: PlanResponse[] = [];
  filteredPlans: PlanResponse[] = [];
  products: ProductResponse[] = [];
  selectedProductId: string = 'ALL';
  selectedPlanDetail: PlanDetailResponse | null = null;
  isLoading = false;
  isSubmitting = false;
  isEditing = false;
  isLoadingDetail = false;
  editingPlanId: string | null = null;

  constructor(
    private fb: FormBuilder,
    private productPlanService: ProductPlanService,
    private notificationService: NotificationService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadProductsAndPlans();
  }

  private initForm(): void {
    this.planForm = this.fb.group({
      productId: ['', [Validators.required]],
      name: ['', [Validators.required]],
      description: ['']
    });
  }

  get productControl() { return this.planForm.get('productId'); }
  get nameControl() { return this.planForm.get('name'); }
  get descriptionControl() { return this.planForm.get('description'); }

  loadProductsAndPlans(): void {
    this.isLoading = true;
    this.productPlanService.getProducts().subscribe({
      next: (prods) => {
        this.products = prods || [];
        this.route.queryParams.subscribe((params) => {
          if (params['productId']) {
            this.selectedProductId = params['productId'];
          }
          this.loadPlans();
        });
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  loadPlans(): void {
    this.isLoading = true;
    if (this.selectedProductId && this.selectedProductId !== 'ALL') {
      this.productPlanService.getPlansByProduct(this.selectedProductId).subscribe({
        next: (plans) => {
          this.isLoading = false;
          this.plans = plans || [];
          this.filteredPlans = [...this.plans];
        },
        error: () => {
          this.isLoading = false;
        }
      });
    } else {
      this.productPlanService.getPlans().subscribe({
        next: (plans) => {
          this.isLoading = false;
          this.plans = plans || [];
          this.filteredPlans = [...this.plans];
        },
        error: () => {
          this.isLoading = false;
        }
      });
    }
  }

  onFilterChange(productId: string): void {
    this.selectedProductId = productId;
    this.selectedPlanDetail = null;
    this.loadPlans();
  }

  getProductName(productId: string): string {
    const p = this.products.find((prod) => prod.productId === productId);
    return p ? p.name : productId;
  }

  inspectPlanDetails(planId: string): void {
    this.isLoadingDetail = true;
    this.productPlanService.getPlan(planId).subscribe({
      next: (detail) => {
        this.isLoadingDetail = false;
        this.selectedPlanDetail = detail;
      },
      error: () => {
        this.isLoadingDetail = false;
      }
    });
  }

  closeDetail(): void {
    this.selectedPlanDetail = null;
  }

  startEdit(plan: PlanResponse): void {
    this.isEditing = true;
    this.editingPlanId = plan.planId;
    this.planForm.patchValue({
      productId: plan.productId,
      name: plan.name,
      description: plan.description || ''
    });
  }

  cancelEdit(): void {
    this.isEditing = false;
    this.editingPlanId = null;
    this.planForm.reset();
  }

  onSubmit(): void {
    if (this.planForm.invalid || this.isSubmitting) {
      this.planForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    const req = this.planForm.value;

    if (this.isEditing && this.editingPlanId) {
      this.productPlanService.updatePlan(this.editingPlanId, req).subscribe({
        next: (updated) => {
          this.isSubmitting = false;
          this.notificationService.success(`Plan "${updated.name}" updated successfully.`);
          this.cancelEdit();
          this.loadPlans();
        },
        error: () => {
          this.isSubmitting = false;
        }
      });
    } else {
      this.productPlanService.createPlan(req).subscribe({
        next: (created) => {
          this.isSubmitting = false;
          this.notificationService.success(`Plan "${created.name}" created successfully.`);
          this.planForm.reset();
          this.loadPlans();
        },
        error: () => {
          this.isSubmitting = false;
        }
      });
    }
  }

  goToRuleMapping(planId: string): void {
    this.router.navigate(['/plans/rule-mapping'], { queryParams: { planId } });
  }
}
