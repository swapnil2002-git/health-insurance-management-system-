import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { NotificationApiService } from '../../../core/services/notification-api.service';
import { NotificationService } from '../../../core/services/notification.service';
import {
  NotificationChannel,
  NotificationRequest,
  NotificationResponse,
  NotificationTemplateResponse
} from '../../../core/models/notification.model';

@Component({
  selector: 'app-notification-send',
  templateUrl: './notification-send.component.html',
  styleUrls: ['./notification-send.component.scss']
})
export class NotificationSendComponent implements OnInit {
  sendForm!: FormGroup;
  isSubmitting = false;
  dispatchedNotification: NotificationResponse | null = null;
  templates: NotificationTemplateResponse[] = [];
  dispatchMode: 'TEMPLATE' | 'CUSTOM' = 'TEMPLATE';

  channels: { value: NotificationChannel; label: string; icon: string }[] = [
    { value: 'EMAIL', label: 'Email', icon: 'email' },
    { value: 'SMS', label: 'SMS Text Message', icon: 'sms' },
    { value: 'IN_APP', label: 'In-App Notification', icon: 'notifications_active' }
  ];

  templateDataJson = '{\n  "policyNumber": "POL-2026-9041",\n  "amount": "450.00",\n  "claimNumber": "CLM-2026-1102",\n  "approvedAmount": "1350.00",\n  "gatewayReference": "TXN-778811"\n}';
  jsonError: string | null = null;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private notificationApiService: NotificationApiService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadTemplates();
  }

  private initForm(): void {
    this.sendForm = this.fb.group({
      recipient: ['customer@example.com', [Validators.required]],
      channel: ['EMAIL', [Validators.required]],
      eventType: ['POLICY_ISSUED'],
      subject: [''],
      content: [''],
      referenceId: [''],
      eventId: ['EVT-' + Math.floor(Math.random() * 900000 + 100000)]
    });
  }

  loadTemplates(): void {
    this.notificationApiService.getAllTemplates().subscribe({
      next: (data) => {
        this.templates = (data || []).filter((t) => t.status === 'ACTIVE');
      }
    });
  }

  onModeChange(mode: 'TEMPLATE' | 'CUSTOM'): void {
    this.dispatchMode = mode;
    if (mode === 'CUSTOM') {
      this.sendForm.get('eventType')?.clearValidators();
      this.sendForm.get('content')?.setValidators([Validators.required]);
    } else {
      this.sendForm.get('content')?.clearValidators();
    }
    this.sendForm.get('eventType')?.updateValueAndValidity();
    this.sendForm.get('content')?.updateValueAndValidity();
  }

  onTemplateSelect(templateId: string): void {
    const tmpl = this.templates.find((t) => t.templateId === templateId);
    if (tmpl) {
      this.sendForm.patchValue({
        eventType: tmpl.eventType,
        channel: tmpl.channel,
        subject: tmpl.subjectTemplate || ''
      });
    }
  }

  onSubmit(): void {
    if (this.sendForm.invalid) {
      this.sendForm.markAllAsTouched();
      return;
    }

    let parsedTemplateData = {};
    if (this.dispatchMode === 'TEMPLATE') {
      try {
        this.jsonError = null;
        if (this.templateDataJson.trim()) {
          parsedTemplateData = JSON.parse(this.templateDataJson);
        }
      } catch (e: any) {
        this.jsonError = 'Invalid JSON: ' + e.message;
        return;
      }
    }

    this.isSubmitting = true;
    const formVal = this.sendForm.value;

    const req: NotificationRequest = {
      recipient: formVal.recipient.trim(),
      channel: formVal.channel,
      eventType: this.dispatchMode === 'TEMPLATE' ? formVal.eventType : undefined,
      subject: formVal.subject?.trim() || undefined,
      content: this.dispatchMode === 'CUSTOM' ? formVal.content?.trim() : undefined,
      referenceId: formVal.referenceId?.trim() || undefined,
      eventId: formVal.eventId?.trim() || undefined,
      templateData: this.dispatchMode === 'TEMPLATE' ? parsedTemplateData : undefined
    };

    this.notificationApiService.sendNotification(req).subscribe({
      next: (res) => {
        this.isSubmitting = false;
        this.dispatchedNotification = res;
        this.notificationService.success(`Notification dispatched successfully (ID: ${res.notificationId.slice(0, 8)}...)`);
      },
      error: (err) => {
        this.isSubmitting = false;
        this.notificationService.error(err.error?.message || 'Failed to dispatch notification');
      }
    });
  }

  viewDispatchedDetails(): void {
    if (this.dispatchedNotification) {
      this.router.navigate(['/notifications/view', this.dispatchedNotification.notificationId]);
    }
  }

  sendAnother(): void {
    this.dispatchedNotification = null;
    this.sendForm.patchValue({
      eventId: 'EVT-' + Math.floor(Math.random() * 900000 + 100000)
    });
  }
}
