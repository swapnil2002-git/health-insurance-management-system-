import { Component, OnInit } from '@angular/core';
import { ReportService } from '../../../core/services/report.service';
import { NotificationService } from '../../../core/services/notification.service';
import { DashboardSummary } from '../../../core/models/report.model';

@Component({
  selector: 'app-report-summary',
  templateUrl: './report-summary.component.html',
  styleUrls: ['./report-summary.component.scss']
})
export class ReportSummaryComponent implements OnInit {
  summary: DashboardSummary | null = null;
  isLoading = false;

  constructor(
    private reportService: ReportService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.loadSummary();
  }

  loadSummary(): void {
    this.isLoading = true;
    this.reportService.getDashboardSummary().subscribe({
      next: (data) => {
        this.isLoading = false;
        this.summary = data;
      },
      error: (err) => {
        this.isLoading = false;
        this.notificationService.error(err.error?.message || 'Failed to fetch dashboard executive summary');
      }
    });
  }

  getPolicyActiveRate(): number {
    if (!this.summary || !this.summary.totalPoliciesIssued || this.summary.totalPoliciesIssued === 0) return 0;
    return Math.round((this.summary.totalPoliciesActive / this.summary.totalPoliciesIssued) * 100);
  }

  getClaimSettlementRate(): number {
    if (!this.summary || !this.summary.totalClaimsSubmitted || this.summary.totalClaimsSubmitted === 0) return 0;
    return Math.round((this.summary.totalClaimsSettled / this.summary.totalClaimsSubmitted) * 100);
  }
}
