import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { RiskService } from '../../../core/services/risk.service';
import { NotificationService } from '../../../core/services/notification.service';
import { RiskAssessmentResponse } from '../../../core/models/risk.model';

@Component({
  selector: 'app-risk-calculate',
  templateUrl: './risk-calculate.component.html',
  styleUrls: ['./risk-calculate.component.scss']
})
export class RiskCalculateComponent implements OnInit {
  assessmentIdInput: string = '';
  assessment: RiskAssessmentResponse | null = null;
  isLoading = false;
  isCalculating = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private riskService: RiskService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe((params) => {
      if (params['assessmentId']) {
        this.assessmentIdInput = params['assessmentId'];
        this.fetchAssessment(this.assessmentIdInput);
      }
    });
  }

  fetchAssessment(id: string): void {
    if (!id || !id.trim()) return;
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
    if (this.assessmentIdInput) {
      this.fetchAssessment(this.assessmentIdInput);
    }
  }

  calculateRisk(): void {
    if (!this.assessment || this.isCalculating) return;
    this.isCalculating = true;

    this.riskService.calculateRisk(this.assessment.assessmentId).subscribe({
      next: (res) => {
        this.isCalculating = false;
        this.assessment = res;
        this.notificationService.success(
          `Risk Score Calculated: ${this.getLatestScore()} (${res.classification})! Event published to Underwriting.`
        );
      },
      error: () => {
        this.isCalculating = false;
      }
    });
  }

  getLatestScore(): number | string {
    if (!this.assessment?.scores || this.assessment.scores.length === 0) return 'N/A';
    return this.assessment.scores[this.assessment.scores.length - 1].score;
  }

  goToAssess(): void {
    this.router.navigate(['/risk/assess']);
  }

  goToView(): void {
    if (this.assessment) {
      this.router.navigate(['/risk/view'], { queryParams: { id: this.assessment.assessmentId } });
    } else {
      this.router.navigate(['/risk/view']);
    }
  }
}
