import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { UnderwritingService } from '../../../core/services/underwriting.service';
import { NotificationService } from '../../../core/services/notification.service';
import { UnderwritingCaseResponse } from '../../../core/models/underwriting.model';

@Component({
  selector: 'app-underwriting-view',
  templateUrl: './underwriting-view.component.html',
  styleUrls: ['./underwriting-view.component.scss']
})
export class UnderwritingViewComponent implements OnInit {
  caseIdInput: string = '';
  uwCase: UnderwritingCaseResponse | null = null;
  isLoading = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private underwritingService: UnderwritingService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe((params) => {
      if (params['id']) {
        this.caseIdInput = params['id'];
        this.fetchCase(this.caseIdInput);
      } else {
        this.route.queryParams.subscribe((queryParams) => {
          if (queryParams['caseId']) {
            this.caseIdInput = queryParams['caseId'];
            this.fetchCase(this.caseIdInput);
          }
        });
      }
    });
  }

  fetchCase(caseId: string): void {
    if (!caseId?.trim()) return;
    this.isLoading = true;
    this.underwritingService.getCase(caseId.trim()).subscribe({
      next: (res) => {
        this.isLoading = false;
        this.uwCase = res;
      },
      error: () => {
        this.isLoading = false;
        this.uwCase = null;
      }
    });
  }

  onSearch(): void {
    if (this.caseIdInput) {
      this.fetchCase(this.caseIdInput);
    }
  }

  copyToClipboard(text: string, label: string): void {
    navigator.clipboard.writeText(text).then(() => {
      this.notificationService.info(`Copied ${label} to clipboard!`);
    });
  }

  goToCases(): void {
    this.router.navigate(['/underwriting/cases']);
  }

  goToApprove(): void {
    if (this.uwCase) {
      this.router.navigate(['/underwriting/approve'], { queryParams: { caseId: this.uwCase.caseId } });
    }
  }

  goToReject(): void {
    if (this.uwCase) {
      this.router.navigate(['/underwriting/reject'], { queryParams: { caseId: this.uwCase.caseId } });
    }
  }

  goToRefer(): void {
    if (this.uwCase) {
      this.router.navigate(['/underwriting/refer'], { queryParams: { caseId: this.uwCase.caseId } });
    }
  }
}
