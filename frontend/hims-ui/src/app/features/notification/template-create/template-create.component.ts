import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { NotificationApiService } from '../../../core/services/notification-api.service';
import { NotificationService } from '../../../core/services/notification.service';
import { NotificationChannel, TemplateStatus } from '../../../core/models/notification.model';

@Component({
  selector: 'app-template-create',
  templateUrl: './template-create.component.html',
  styleUrls: ['./template-create.component.scss']
})
export class NotificationTemplateCreateComponent implements OnInit {
  templateForm!: FormGroup;
  isEditMode = false;
  templateId: string | null = null;
  isSubmitting = false;
  isLoading = false;

  channels: { value: NotificationChannel; label: string; icon: string }[] = [
    { value: 'EMAIL', label: 'Email', icon: 'email' },
    { value: 'SMS', label: 'SMS Text Message', icon: 'sms' },
    { value: 'IN_APP', label: 'In-App Alert', icon: 'notifications_active' }
  ];

  commonEventTypes: string[] = [
    'POLICY_ISSUED',
    'PREMIUM_DUE',
    'PREMIUM_PAID',
    'POLICY_RENEWED',
    'POLICY_CANCELLED',
    'CLAIM_SUBMITTED',
    'CLAIM_VALIDATED',
    'CLAIM_APPROVED',
    'CLAIM_REJECTED',
    'CLAIM_SETTLED',
    'USER_REGISTRATION',
    'PASSWORD_RESET'
  ];

  commonPlaceholders: { code: string; label: string; sample: string }[] = [
    { code: '{policyNumber}', label: 'Policy Number', sample: 'POL-2026-9041' },
    { code: '{customerName}', label: 'Customer Name', sample: 'John Doe' },
    { code: '{amount}', label: 'Amount', sample: '$450.00' },
    { code: '{effectiveDate}', label: 'Effective Date', sample: '2026-10-01' },
    { code: '{expiryDate}', label: 'Expiry Date', sample: '2027-10-01' },
    { code: '{claimNumber}', label: 'Claim Number', sample: 'CLM-2026-1102' },
    { code: '{approvedAmount}', label: 'Approved Amount', sample: '$1,350.00' },
    { code: '{gatewayReference}', label: 'Payment Ref', sample: 'TXN-984210' },
    { code: '{reason}', label: 'Reason', sample: 'Pre-existing condition exclusion' }
  ];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private notificationApiService: NotificationApiService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.route.paramMap.subscribe((params) => {
      const id = params.get('id');
      if (id) {
        this.isEditMode = true;
        this.templateId = id;
        this.loadTemplate(id);
      }
    });
  }

  private initForm(): void {
    this.templateForm = this.fb.group({
      eventType: ['', [Validators.required, Validators.maxLength(100)]],
      channel: ['EMAIL', [Validators.required]],
      subjectTemplate: ['', [Validators.maxLength(255)]],
      bodyTemplate: ['', [Validators.required, Validators.maxLength(4000)]],
      status: ['ACTIVE', [Validators.required]]
    });
  }

  loadTemplate(id: string): void {
    this.isLoading = true;
    this.notificationApiService.getTemplateById(id).subscribe({
      next: (t) => {
        this.isLoading = false;
        this.templateForm.patchValue({
          eventType: t.eventType,
          channel: t.channel,
          subjectTemplate: t.subjectTemplate || '',
          bodyTemplate: t.bodyTemplate,
          status: t.status
        });
      },
      error: (err) => {
        this.isLoading = false;
        this.notificationService.error(err.error?.message || 'Failed to load template');
        this.router.navigate(['/notifications/templates']);
      }
    });
  }

  insertPlaceholder(code: string, targetField: 'subjectTemplate' | 'bodyTemplate' = 'bodyTemplate'): void {
    const currentVal = this.templateForm.get(targetField)?.value || '';
    this.templateForm.get(targetField)?.setValue(currentVal + code);
  }

  selectEventType(evt: string): void {
    this.templateForm.get('eventType')?.setValue(evt);
  }

  getLiveRenderedPreview(): { subject: string; body: string } {
    let subject = this.templateForm.get('subjectTemplate')?.value || '';
    let body = this.templateForm.get('bodyTemplate')?.value || '';

    this.commonPlaceholders.forEach((p) => {
      subject = subject.split(p.code).join(p.sample);
      body = body.split(p.code).join(p.sample);
    });

    return { subject, body };
  }

  onSubmit(): void {
    if (this.templateForm.invalid) {
      this.templateForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    const req = {
      eventType: this.templateForm.value.eventType.trim(),
      channel: this.templateForm.value.channel,
      subjectTemplate: this.templateForm.value.subjectTemplate?.trim() || null,
      bodyTemplate: this.templateForm.value.bodyTemplate.trim(),
      status: this.templateForm.value.status as TemplateStatus
    };

    if (this.isEditMode && this.templateId) {
      this.notificationApiService.updateTemplate(this.templateId, req).subscribe({
        next: (res) => {
          this.isSubmitting = false;
          this.notificationService.success(`Template ${res.eventType} updated successfully`);
          this.router.navigate(['/notifications/templates']);
        },
        error: (err) => {
          this.isSubmitting = false;
          this.notificationService.error(err.error?.message || 'Failed to update template');
        }
      });
    } else {
      this.notificationApiService.createTemplate(req).subscribe({
        next: (res) => {
          this.isSubmitting = false;
          this.notificationService.success(`Template ${res.eventType} created successfully`);
          this.router.navigate(['/notifications/templates']);
        },
        error: (err) => {
          this.isSubmitting = false;
          this.notificationService.error(err.error?.message || 'Failed to create template');
        }
      });
    }
  }

  cancel(): void {
    this.router.navigate(['/notifications/templates']);
  }
}
