import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { ReportService } from '../../../core/services/report.service';
import { NotificationService } from '../../../core/services/notification.service';
import { PremiumReportResponse } from '../../../core/models/report.model';

@Component({
  selector: 'app-report-premiums',
  templateUrl: './report-premiums.component.html',
  styleUrls: ['./report-premiums.component.scss']
})
export class ReportPremiumsComponent implements OnInit {
  filterForm!: FormGroup;
  report: PremiumReportResponse | null = null;
  isLoading = false;

  constructor(
    private fb: FormBuilder,
    private reportService: ReportService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    const today = new Date();
    const thirtyDaysAgo = new Date();
    thirtyDaysAgo.setDate(today.getDate() - 30);

    this.filterForm = this.fb.group({
      startDate: [thirtyDaysAgo.toISOString().split('T')[0]],
      endDate: [today.toISOString().split('T')[0]]
    });

    this.loadReport();
  }

  loadReport(): void {
    this.isLoading = true;
    const { startDate, endDate } = this.filterForm.value;

    this.reportService.getPremiumReport(startDate, endDate).subscribe({
      next: (data) => {
        this.isLoading = false;
        this.report = data;
      },
      error: (err) => {
        this.isLoading = false;
        this.notificationService.error(err.error?.message || 'Failed to load premium revenue report');
      }
    });
  }

  resetFilter(): void {
    const today = new Date();
    const thirtyDaysAgo = new Date();
    thirtyDaysAgo.setDate(today.getDate() - 30);

    this.filterForm.patchValue({
      startDate: thirtyDaysAgo.toISOString().split('T')[0],
      endDate: today.toISOString().split('T')[0]
    });
    this.loadReport();
  }

  getAverageTicketSize(): number {
    if (!this.report || !this.report.overallPaymentsCollected || this.report.overallPaymentsCollected === 0) return 0;
    return Math.round((this.report.overallPremiumAmount / this.report.overallPaymentsCollected) * 100) / 100;
  }
}
