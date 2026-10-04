import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { PremiumService } from '../../../core/services/premium.service';
import { PolicyService } from '../../../core/services/policy.service';
import { NotificationService } from '../../../core/services/notification.service';
import {
  PremiumScheduleResponse,
  PremiumOutstandingResponse
} from '../../../core/models/premium.model';

@Component({
  selector: 'app-premium-view',
  templateUrl: './premium-view.component.html',
  styleUrls: ['./premium-view.component.scss']
})
export class PremiumViewComponent implements OnInit {
  policyIdInput: string = '';
  schedule: PremiumScheduleResponse | null = null;
  outstanding: PremiumOutstandingResponse | null = null;

  isLoading = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private premiumService: PremiumService,
    private policyService: PolicyService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    // Check route param :policyId or queryParam ?policyId=
    this.route.paramMap.subscribe((params) => {
      const pId = params.get('policyId');
      if (pId) {
        this.policyIdInput = pId;
        this.fetchSchedule(pId);
      } else {
        this.route.queryParams.subscribe((q) => {
          if (q['policyId']) {
            this.policyIdInput = q['policyId'];
            this.fetchSchedule(this.policyIdInput);
          }
        });
      }
    });
  }

  onSearch(): void {
    const raw = (this.policyIdInput || '').trim();
    if (!raw) return;

    const isUuid = /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/.test(raw);
    if (isUuid) {
      this.fetchSchedule(raw);
    } else {
      this.isLoading = true;
      this.policyService.getAllPolicies().subscribe({
        next: (policies) => {
          const match = (policies || []).find(
            (p) =>
              (p.policyNumber && p.policyNumber.toLowerCase() === raw.toLowerCase()) ||
              (p.policyId && p.policyId.toLowerCase() === raw.toLowerCase())
          );
          if (match) {
            this.fetchSchedule(match.policyId);
          } else {
            this.fetchSchedule(raw);
          }
        },
        error: () => this.fetchSchedule(raw)
      });
    }
  }

  fetchSchedule(policyId: string): void {
    this.isLoading = true;
    this.premiumService.getPremiumByPolicy(policyId).subscribe({
      next: (res) => {
        this.schedule = res;
        this.isLoading = false;
        this.fetchOutstanding(policyId);
      },
      error: (err) => {
        this.isLoading = false;
        this.schedule = null;
        this.notificationService.error(
          err.error?.message || `Failed to retrieve premium schedule for Policy ID: ${policyId}`
        );
      }
    });
  }

  private fetchOutstanding(policyId: string): void {
    this.premiumService.getOutstanding(policyId).subscribe({
      next: (out) => {
        this.outstanding = out;
      },
      error: () => {}
    });
  }

  get progressPercentage(): number {
    if (!this.schedule || !this.schedule.totalPremium || this.schedule.totalPremium <= 0) {
      return 0;
    }
    const pct = (this.schedule.paidAmount / this.schedule.totalPremium) * 100;
    return Math.min(Math.round(pct), 100);
  }

  goToPayInstallment(): void {
    if (this.schedule) {
      this.router.navigate(['/premium/installments'], {
        queryParams: { policyId: this.schedule.policyId }
      });
    }
  }

  goToRecalculate(): void {
    if (this.schedule) {
      this.router.navigate(['/premium/recalculate'], {
        queryParams: { policyId: this.schedule.policyId }
      });
    }
  }

  goToPolicy(): void {
    if (this.schedule) {
      this.router.navigate(['/policies/view', this.schedule.policyId]);
    }
  }

  printDossier(): void {
    window.print();
  }
}
