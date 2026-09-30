import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { RiskService } from '../../../core/services/risk.service';
import { NotificationService } from '../../../core/services/notification.service';
import { RiskAssessmentResponse } from '../../../core/models/risk.model';

@Component({
  selector: 'app-risk-view',
  templateUrl: './risk-view.component.html',
  styleUrls: ['./risk-view.component.scss']
})
export class RiskViewComponent implements OnInit {
  searchId: string = '';
  assessment: RiskAssessmentResponse | null = null;
  isLoading = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private riskService: RiskService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe((params) => {
      if (params['id']) {
        this.searchId = params['id'];
        this.loadAssessment(this.searchId);
      }
    });

    this.route.queryParams.subscribe((params) => {
      if (params['id'] && !this.assessment) {
        this.searchId = params['id'];
        this.loadAssessment(this.searchId);
      }
    });
  }

  loadAssessment(id: string): void {
    if (!id.trim()) return;
    this.isLoading = true;
    this.riskService.getAssessment(id.trim()).subscribe({
      next: (res) => {
        this.isLoading = false;
        this.assessment = res;
      },
      error: () => {
        this.isLoading = false;
        this.assessment = null;
      }
    });
  }

  onSearch(): void {
    if (this.searchId) {
      this.loadAssessment(this.searchId);
    }
  }

  copyAssessmentId(): void {
    if (this.assessment) {
      navigator.clipboard.writeText(this.assessment.assessmentId);
      this.notificationService.success('Assessment ID copied to clipboard!');
    }
  }

  getLatestScore(): number | string {
    if (!this.assessment?.scores || this.assessment.scores.length === 0) return 'N/A';
    return this.assessment.scores[this.assessment.scores.length - 1].score;
  }

  goToCalculate(): void {
    if (this.assessment) {
      this.router.navigate(['/risk/calculate'], { queryParams: { assessmentId: this.assessment.assessmentId } });
    }
  }

  goToAssess(): void {
    this.router.navigate(['/risk/assess']);
  }
}
