import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ProductPlanService } from '../../../core/services/product-plan.service';
import {
  MasterRuleItem,
  RuleCategory,
  RULE_CATEGORIES
} from '../../../core/models/product-plan.model';

@Component({
  selector: 'app-master-rules',
  templateUrl: './master-rules.component.html',
  styleUrls: ['./master-rules.component.scss']
})
export class MasterRulesComponent implements OnInit {
  ruleCategories = RULE_CATEGORIES;
  selectedCategory: RuleCategory = 'coverages';
  rules: MasterRuleItem[] = [];
  filteredRules: MasterRuleItem[] = [];
  searchQuery: string = '';
  isLoading = false;

  constructor(
    private productPlanService: ProductPlanService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadRules(this.selectedCategory);
  }

  onCategorySelect(category: RuleCategory): void {
    this.selectedCategory = category;
    this.searchQuery = '';
    this.loadRules(category);
  }

  loadRules(category: RuleCategory): void {
    this.isLoading = true;
    this.productPlanService.getMasterRules(category).subscribe({
      next: (rules) => {
        this.isLoading = false;
        this.rules = rules || [];
        this.applyFilter();
      },
      error: () => {
        this.isLoading = false;
        this.rules = [];
        this.filteredRules = [];
      }
    });
  }

  applyFilter(): void {
    const q = this.searchQuery.trim().toLowerCase();
    if (!q) {
      this.filteredRules = [...this.rules];
    } else {
      this.filteredRules = this.rules.filter(
        (r) =>
          r.name?.toLowerCase().includes(q) ||
          r.description?.toLowerCase().includes(q) ||
          r.id?.toLowerCase().includes(q)
      );
    }
  }

  getCurrentCategoryLabel(): string {
    const found = this.ruleCategories.find((c) => c.key === this.selectedCategory);
    return found ? found.label : 'Rules';
  }

  goToRuleMapping(): void {
    this.router.navigate(['/plans/rule-mapping']);
  }

  goToPlans(): void {
    this.router.navigate(['/plans']);
  }
}
