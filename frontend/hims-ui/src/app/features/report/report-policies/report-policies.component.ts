import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { ReportService } from '../../../core/services/report.service';
import { NotificationService } from '../../../core/services/notification.service';
import { PolicyReportResponse } from '../../../core/models/report.model';

@Component({
  selector: 'app-report-policies',
  templateUrl: './report-policies.component.html',
  styleUrls: ['./report-policies.component.scss']
})
export class ReportPoliciesComponent implements OnInit {
  filterForm!: FormGroup;
  report: PolicyReportResponse | null = null;
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

    this.reportService.getPolicyReport(startDate, endDate).subscribe({
      next: (data) => {
        this.isLoading = false;
        this.report = data;
      },
      error: (err) => {
        this.isLoading = false;
        this.notificationService.error(err.error?.message || 'Failed to load policy daily report');
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
}
