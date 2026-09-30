import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { PremiumService } from '../../../core/services/premium.service';
import { PolicyService } from '../../../core/services/policy.service';
import { NotificationService } from '../../../core/services/notification.service';
import {
  PremiumScheduleResponse,
  PremiumScheduleCreateRequest,
  PaymentFrequency
} from '../../../core/models/premium.model';
import { PolicyResponse } from '../../../core/models/policy.model';

@Component({
  selector: 'app-premium-schedule',
  templateUrl: './premium-schedule.component.html',
  styleUrls: ['./premium-schedule.component.scss']
})
export class PremiumScheduleComponent implements OnInit {
  createForm!: FormGroup;
  policyInput: string = '';
  availableSchedules: PremiumScheduleResponse[] = [];
  selectedScheduleId: string = '';
  currentSchedule: PremiumScheduleResponse | null = null;
  recentSchedules: PremiumScheduleResponse[] = [];
  resolvedPolicy: PolicyResponse | null = null;
  activeMode: 'inspect' | 'create' = 'inspect';

  isLoading = false;
  isSubmitting = false;

  frequencies: { value: PaymentFrequency; label: string; desc: string }[] = [
    { value: 'ANNUAL', label: 'Annual (Single Lump Sum)', desc: '1 installment per policy year' },
    { value: 'QUARTERLY', label: 'Quarterly (Every 3 Months)', desc: '4 installments per policy year' },
    { value: 'MONTHLY', label: 'Monthly (Every Month)', desc: '12 installments per policy year' }
  ];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private premiumService: PremiumService,
    private policyService: PolicyService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadAllSchedules();

    this.route.queryParams.subscribe((params) => {
      const p = params['policyId'] || params['policyNumber'];
      if (p) {
        this.policyInput = p;
        this.searchPolicySchedules();
      }
    });
  }

  loadAllSchedules(): void {
    this.premiumService.getAllSchedules().subscribe({
      next: (schedules) => {
        if (schedules && schedules.length > 0) {
          this.recentSchedules = schedules;
        } else {
          this.recentSchedules = this.premiumService.getRecentSchedules();
        }
      },
      error: () => {
        this.recentSchedules = this.premiumService.getRecentSchedules();
      }
    });
  }

  private initForm(): void {
    const today = new Date().toISOString().substring(0, 10);
    const nextYear = new Date();
    nextYear.setFullYear(nextYear.getFullYear() + 1);
    const endStr = nextYear.toISOString().substring(0, 10);

    this.createForm = this.fb.group({
      policyId: ['', [Validators.required]],
      paymentFrequency: ['MONTHLY' as PaymentFrequency, [Validators.required]],
      startDate: [today, [Validators.required]],
      endDate: [endStr, [Validators.required]],
      basePremium: [1200, [Validators.required, Validators.min(1)]],
      riderPremium: [0, [Validators.min(0)]],
      riskLoading: [0, [Validators.min(0)]],
      discount: [0, [Validators.min(0)]],
      tax: [100, [Validators.min(0)]]
    });
  }

  searchPolicySchedules(): void {
    const raw = (this.policyInput || '').trim();
    if (!raw) {
      this.notificationService.warning('Please enter a Policy Number or Policy ID');
      return;
    }

    this.isLoading = true;
    this.availableSchedules = [];
    this.selectedScheduleId = '';
    this.currentSchedule = null;
    this.resolvedPolicy = null;

    const isUuid = /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/.test(raw);

    if (isUuid) {
      this.fetchScheduleForPolicyId(raw);
      this.policyService.getPolicy(raw).subscribe({
        next: (p) => (this.resolvedPolicy = p),
        error: () => {}
      });
    } else {
      // Find policy by Policy Number
      this.policyService.getAllPolicies().subscribe({
        next: (policies) => {
          const match = (policies || []).find(
            (p) =>
              (p.policyNumber && p.policyNumber.toLowerCase() === raw.toLowerCase()) ||
              (p.policyId && p.policyId.toLowerCase() === raw.toLowerCase())
          );
          if (match) {
            this.resolvedPolicy = match;
            this.fetchScheduleForPolicyId(match.policyId);
          } else {
            this.fetchScheduleForPolicyId(raw);
          }
        },
        error: () => {
          this.fetchScheduleForPolicyId(raw);
        }
      });
    }
  }

  private fetchScheduleForPolicyId(policyId: string): void {
    this.premiumService.getPremiumByPolicy(policyId).subscribe({
      next: (res) => {
        this.isLoading = false;
        this.availableSchedules = [res];
        this.selectedScheduleId = res.scheduleId;
        this.currentSchedule = res;
        this.activeMode = 'inspect';
        this.recentSchedules = this.premiumService.getRecentSchedules();
        this.notificationService.success(
          `Schedule found! Schedule ID: #${res.scheduleId}`
        );
      },
      error: () => {
        this.isLoading = false;
        this.currentSchedule = null;
        this.availableSchedules = [];
        this.selectedScheduleId = '';
        this.notificationService.warning(
          `No premium schedule found for policy: "${this.policyInput}". You can generate one manually.`
        );
      }
    });
  }

  onSelectSchedule(scheduleId: string): void {
    if (!scheduleId) return;
    this.selectedScheduleId = scheduleId;
    const found = this.availableSchedules.find((s) => s.scheduleId === scheduleId);
    if (found) {
      this.currentSchedule = found;
    } else {
      this.isLoading = true;
      this.premiumService.getPremiumScheduleById(scheduleId).subscribe({
        next: (res) => {
          this.isLoading = false;
          this.currentSchedule = res;
        },
        error: () => {
          this.isLoading = false;
        }
      });
    }
  }

  onCreateSchedule(): void {
    if (this.createForm.invalid || this.isSubmitting) {
      this.createForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    const val = this.createForm.value;

    const req: PremiumScheduleCreateRequest = {
      policyId: val.policyId.trim(),
      paymentFrequency: val.paymentFrequency,
      startDate: val.startDate,
      endDate: val.endDate,
      basePremium: Number(val.basePremium),
      riderPremium: Number(val.riderPremium || 0),
      riskLoading: Number(val.riskLoading || 0),
      discount: Number(val.discount || 0),
      tax: Number(val.tax || 0)
    };

    this.premiumService.createPremiumSchedule(req).subscribe({
      next: (sched) => {
        this.isSubmitting = false;
        this.availableSchedules = [sched];
        this.selectedScheduleId = sched.scheduleId;
        this.currentSchedule = sched;
        this.policyInput = sched.policyId;
        this.activeMode = 'inspect';
        this.recentSchedules = this.premiumService.getRecentSchedules();
        this.notificationService.success(
          `Premium Schedule created! Total: $${sched.totalPremium}, Installments: ${sched.numberOfInstallments}`
        );
      },
      error: () => {
        this.isSubmitting = false;
      }
    });
  }

  get computedTotalPreview(): number {
    const val = this.createForm.value;
    const base = Number(val.basePremium || 0);
    const rider = Number(val.riderPremium || 0);
    const risk = Number(val.riskLoading || 0);
    const disc = Number(val.discount || 0);
    const tax = Number(val.tax || 0);
    return Math.max(0, base + rider + risk - disc + tax);
  }

  selectRecent(sched: PremiumScheduleResponse): void {
    this.policyInput = sched.policyId;
    this.availableSchedules = [sched];
    this.selectedScheduleId = sched.scheduleId;
    this.currentSchedule = sched;
    this.activeMode = 'inspect';
  }

  goToPayInstallments(): void {
    if (this.currentSchedule) {
      this.router.navigate(['/premium/installments'], { queryParams: { policyId: this.currentSchedule.policyId } });
    }
  }

  goToRecalculate(): void {
    if (this.currentSchedule) {
      this.router.navigate(['/premium/recalculate'], { queryParams: { policyId: this.currentSchedule.policyId } });
    }
  }

  goToView(): void {
    if (this.currentSchedule) {
      this.router.navigate(['/premium/view', this.currentSchedule.policyId]);
    }
  }
}
