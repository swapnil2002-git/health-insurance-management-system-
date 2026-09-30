import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ProductPlanService } from '../../../core/services/product-plan.service';
import { NotificationService } from '../../../core/services/notification.service';
import {
  PlanResponse,
  PlanDetailResponse,
  MasterRuleItem,
  RuleCategory,
  RULE_CATEGORIES
} from '../../../core/models/product-plan.model';

@Component({
  selector: 'app-plan-rule-mapping',
  templateUrl: './plan-rule-mapping.component.html',
  styleUrls: ['./plan-rule-mapping.component.scss']
})
export class PlanRuleMappingComponent implements OnInit {
  mapForm!: FormGroup;
  plans: PlanResponse[] = [];
  selectedPlanId: string = '';
  planDetail: PlanDetailResponse | null = null;
  ruleCategories = RULE_CATEGORIES;
  availableRules: MasterRuleItem[] = [];

  isLoadingPlans = false;
  isLoadingDetail = false;
  isLoadingRules = false;
  isSubmitting = false;

  constructor(
    private fb: FormBuilder,
    private productPlanService: ProductPlanService,
    private notificationService: NotificationService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadPlans();
  }

  private initForm(): void {
    this.mapForm = this.fb.group({
      category: ['coverages', [Validators.required]],
      ruleId: ['', [Validators.required]]
    });

    this.mapForm.get('category')?.valueChanges.subscribe((category: RuleCategory) => {
      this.loadCategoryRules(category);
    });
  }

  get categoryControl() { return this.mapForm.get('category'); }
  get ruleIdControl() { return this.mapForm.get('ruleId'); }

  loadPlans(): void {
    this.isLoadingPlans = true;
    this.productPlanService.getPlans().subscribe({
      next: (plans) => {
        this.isLoadingPlans = false;
        this.plans = plans || [];
        this.route.queryParams.subscribe((params) => {
          if (params['planId']) {
            this.selectedPlanId = params['planId'];
            this.onPlanChange(this.selectedPlanId);
          } else if (this.plans.length > 0 && !this.selectedPlanId) {
            this.selectedPlanId = this.plans[0].planId;
            this.onPlanChange(this.selectedPlanId);
          }
        });
      },
      error: () => {
        this.isLoadingPlans = false;
      }
    });
  }

  onPlanChange(planId: string): void {
    this.selectedPlanId = planId;
    if (!planId) {
      this.planDetail = null;
      return;
    }
    this.loadPlanDetail(planId);
    const cat = this.mapForm.get('category')?.value || 'coverages';
    this.loadCategoryRules(cat);
  }

  loadPlanDetail(planId: string): void {
    this.isLoadingDetail = true;
    this.productPlanService.getPlan(planId).subscribe({
      next: (detail) => {
        this.isLoadingDetail = false;
        this.planDetail = detail;
      },
      error: () => {
        this.isLoadingDetail = false;
      }
    });
  }

  loadCategoryRules(category: RuleCategory): void {
    if (!category) return;
    this.isLoadingRules = true;
    this.mapForm.get('ruleId')?.reset('');
    this.productPlanService.getMasterRules(category).subscribe({
      next: (rules) => {
        this.isLoadingRules = false;
        this.availableRules = rules || [];
      },
      error: () => {
        this.isLoadingRules = false;
      }
    });
  }

  onMapRule(): void {
    if (this.mapForm.invalid || !this.selectedPlanId || this.isSubmitting) {
      this.mapForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    const category = this.mapForm.value.category as RuleCategory;
    const ruleId = this.mapForm.value.ruleId;

    this.productPlanService.mapRuleToPlan(this.selectedPlanId, category, ruleId).subscribe({
      next: (res) => {
        this.isSubmitting = false;
        this.notificationService.success(`Rule "${res.name}" successfully mapped to ${this.planDetail?.name || 'plan'}.`);
        this.mapForm.get('ruleId')?.reset('');
        this.loadPlanDetail(this.selectedPlanId);
      },
      error: () => {
        this.isSubmitting = false;
      }
    });
  }

  goToMasterRules(): void {
    this.router.navigate(['/plans/rules']);
  }

  goToPlans(): void {
    this.router.navigate(['/plans']);
  }
}
