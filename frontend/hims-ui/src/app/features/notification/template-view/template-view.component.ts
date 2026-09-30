import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { NotificationApiService } from '../../../core/services/notification-api.service';
import { NotificationService } from '../../../core/services/notification.service';
import { NotificationTemplateResponse } from '../../../core/models/notification.model';

@Component({
  selector: 'app-template-view',
  templateUrl: './template-view.component.html',
  styleUrls: ['./template-view.component.scss']
})
export class NotificationTemplateViewComponent implements OnInit {
  searchForm!: FormGroup;
  template: NotificationTemplateResponse | null = null;
  isLoading = false;

  // Live variable tester
  testDataJson = '{\n  "policyNumber": "POL-2026-001",\n  "customerName": "Jane Doe",\n  "amount": "$500.00",\n  "claimNumber": "CLM-9912",\n  "approvedAmount": "$1,200.00"\n}';
  jsonError: string | null = null;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private notificationApiService: NotificationApiService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.searchForm = this.fb.group({
      templateId: ['', [Validators.required]]
    });

    this.route.paramMap.subscribe((params) => {
      const id = params.get('id');
      if (id) {
        this.searchForm.patchValue({ templateId: id });
        this.fetchTemplate(id);
      }
    });
  }

  onSearch(): void {
    if (this.searchForm.invalid) return;
    const id = this.searchForm.value.templateId.trim();
    this.router.navigate(['/notifications/templates/view', id]);
  }

  fetchTemplate(id: string): void {
    this.isLoading = true;
    this.template = null;
    this.notificationApiService.getTemplateById(id).subscribe({
      next: (data) => {
        this.isLoading = false;
        this.template = data;
      },
      error: (err) => {
        this.isLoading = false;
        this.notificationService.error(err.error?.message || `Template ${id} not found`);
      }
    });
  }

  getRenderedPreview(): { subject: string; body: string } {
    if (!this.template) return { subject: '', body: '' };

    let subject = this.template.subjectTemplate || '';
    let body = this.template.bodyTemplate || '';

    try {
      this.jsonError = null;
      const parsed = JSON.parse(this.testDataJson);
      for (const key of Object.keys(parsed)) {
        const placeholder = `{${key}}`;
        const val = String(parsed[key]);
        subject = subject.split(placeholder).join(val);
        body = body.split(placeholder).join(val);
      }
    } catch (e: any) {
      this.jsonError = 'Invalid JSON: ' + e.message;
    }

    return { subject, body };
  }

  editTemplate(): void {
    if (this.template) {
      this.router.navigate(['/notifications/templates/edit', this.template.templateId]);
    }
  }

  backToList(): void {
    this.router.navigate(['/notifications/templates']);
  }
}
