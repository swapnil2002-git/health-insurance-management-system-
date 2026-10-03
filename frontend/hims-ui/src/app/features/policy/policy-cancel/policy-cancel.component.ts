import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { PolicyService } from '../../../core/services/policy.service';
import { NotificationService } from '../../../core/services/notification.service';
import { AuthService } from '../../../core/auth/auth.service';
import {
  PolicyResponse,
  PolicyCancellationResponse,
  PolicyCancellationRequest
} from '../../../core/models/policy.model';

@Component({
  selector: 'app-policy-cancel',
  templateUrl: './policy-cancel.component.html',
  styleUrls: ['./policy-cancel.component.scss']
})
export class PolicyCancelComponent implements OnInit {
  cancelForm!: FormGroup;
  policyIdInput: string = '';
  policy: PolicyResponse | null = null;
  cancellation: PolicyCancellationResponse | null = null;

  isLoading = false;
  isSubmitting = false;
  userRole = '';

  commonReasons: string[] = [
    'Relocating out of coverage jurisdiction / state',
    'Customer enrolled in employer-sponsored group health scheme',
    'Financial hardship / unaffordable premium obligations',
    'Dissatisfaction with hospital network or servicing provider',
    'Duplicated health insurance coverage policy'
  ];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private policyService: PolicyService,
    private notificationService: NotificationService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.userRole = this.authService.getRole() || '';
    this.initForm();

    this.route.queryParams.subscribe((params) => {
      if (params['policyId']) {
        this.policyIdInput = params['policyId'];
        this.fetchPolicy(this.policyIdInput);
      }
    });
  }

  private initForm(): void {
    const username = this.authService.getUsername() || 'Customer';
    this.cancelForm = this.fb.group({
      reason: ['', [Validators.required, Validators.minLength(5)]],
      requestedBy: [username, [Validators.required]]
    });
  }

  fetchPolicy(id: string): void {
    if (!id?.trim()) return;
    this.isLoading = true;
    this.policyService.getPolicy(id.trim()).subscribe({
      next: (res) => {
        this.isLoading = false;
        this.policy = res;
        this.loadCancellation(res.policyId);
      },
      error: () => {
        this.isLoading = false;
        this.policy = null;
        this.notificationService.error('Policy not found with ID: ' + id);
      }
    });
  }

  loadCancellation(policyId: string): void {
    this.policyService.getCancellation(policyId).subscribe({
      next: (data) => (this.cancellation = data),
      error: () => (this.cancellation = null)
    });
  }

  onSearch(): void {
    if (this.policyIdInput) {
      this.fetchPolicy(this.policyIdInput);
    }
  }

  selectReason(r: string): void {
    this.cancelForm.patchValue({ reason: r });
  }

  onSubmitCancellation(): void {
    if (this.cancelForm.invalid || !this.policy || this.isSubmitting) {
      this.cancelForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    const formVal = this.cancelForm.value;

    const req: PolicyCancellationRequest = {
      reason: formVal.reason,
      requestedBy: formVal.requestedBy
    };

    this.policyService.requestCancellation(this.policy.policyId, req).subscribe({
      next: (cRes) => {
        this.isSubmitting = false;
        this.cancellation = cRes;
        this.notificationService.success(
          `Cancellation request submitted! Status: ${cRes.status}. Estimated refund: $${cRes.refundAmount || 0}`
        );
        this.fetchPolicy(this.policy!.policyId);
      },
      error: (err) => {
        this.isSubmitting = false;
        const msg = err.error?.message || 'Failed to submit cancellation request';
        this.notificationService.error(msg);
      }
    });
  }

  onApproveCancellation(): void {
    if (!this.policy) return;
    const adminUser = this.authService.getUsername() || 'admin';
    this.policyService.approveCancellation(this.policy.policyId, {
      approvedBy: adminUser,
      refundAmount: this.cancellation?.refundAmount || 250,
      notes: 'Prorated cancellation refund approved'
    }).subscribe({
      next: (res) => {
        this.cancellation = res;
        this.notificationService.success(
          `Cancellation APPROVED. Policy status transitioned to CANCELLED. Refund: $${res.refundAmount || 0}`
        );
        this.fetchPolicy(this.policy!.policyId);
      },
      error: (err) => {
        const msg = err.error?.message || 'Failed to approve cancellation';
        this.notificationService.error(msg);
      }
    });
  }

  onRejectCancellation(): void {
    if (!this.policy) return;
    this.policyService.rejectCancellation(this.policy.policyId, {
      rejectionReason: 'Cancellation within lock-in period is not eligible for refund',
      notes: 'Administrative rejection'
    }).subscribe({
      next: (res) => {
        this.cancellation = res;
        this.notificationService.info('Cancellation request rejected. Policy remains active.');
      }
    });
  }

  viewInPaymentLedger(): void {
    if (!this.policy) return;
    this.router.navigate(['/payments'], { queryParams: { policyId: this.policy.policyId } });
  }

  goToPolicies(): void {
    this.router.navigate(['/policies']);
  }
}
