import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { NotificationApiService } from '../../../core/services/notification-api.service';
import { NotificationService } from '../../../core/services/notification.service';
import { NotificationChannel, NotificationTemplateResponse } from '../../../core/models/notification.model';

@Component({
  selector: 'app-template-list',
  templateUrl: './template-list.component.html',
  styleUrls: ['./template-list.component.scss']
})
export class NotificationTemplateListComponent implements OnInit {
  templates: NotificationTemplateResponse[] = [];
  filteredTemplates: NotificationTemplateResponse[] = [];
  isLoading = false;
  selectedChannel: string = 'ALL';
  searchFilter: string = '';

  channels: { label: string; value: string; icon: string }[] = [
    { label: 'All Channels', value: 'ALL', icon: 'all_inclusive' },
    { label: 'Email', value: 'EMAIL', icon: 'email' },
    { label: 'SMS', value: 'SMS', icon: 'sms' },
    { label: 'In-App', value: 'IN_APP', icon: 'notifications_active' }
  ];

  constructor(
    private notificationApiService: NotificationApiService,
    private notificationService: NotificationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadTemplates();
  }

  loadTemplates(): void {
    this.isLoading = true;
    this.notificationApiService.getAllTemplates().subscribe({
      next: (data) => {
        this.isLoading = false;
        this.templates = data || [];
        this.applyFilters();
      },
      error: (err) => {
        this.isLoading = false;
        this.notificationService.error(err.error?.message || 'Failed to load notification templates');
      }
    });
  }

  applyFilters(): void {
    let result = [...this.templates];

    if (this.selectedChannel !== 'ALL') {
      result = result.filter((t) => t.channel === this.selectedChannel);
    }

    if (this.searchFilter.trim()) {
      const q = this.searchFilter.trim().toLowerCase();
      result = result.filter(
        (t) =>
          t.eventType.toLowerCase().includes(q) ||
          (t.subjectTemplate && t.subjectTemplate.toLowerCase().includes(q)) ||
          t.bodyTemplate.toLowerCase().includes(q)
      );
    }

    this.filteredTemplates = result;
  }

  onChannelChange(channel: string): void {
    this.selectedChannel = channel;
    this.applyFilters();
  }

  onSearchChange(): void {
    this.applyFilters();
  }

  viewTemplate(templateId: string): void {
    this.router.navigate(['/notifications/templates/view', templateId]);
  }

  editTemplate(templateId: string): void {
    this.router.navigate(['/notifications/templates/edit', templateId]);
  }

  deleteTemplate(template: NotificationTemplateResponse): void {
    if (confirm(`Are you sure you want to delete template for event "${template.eventType}" (${template.channel})?`)) {
      this.notificationApiService.deleteTemplate(template.templateId).subscribe({
        next: () => {
          this.notificationService.success(`Template ${template.eventType} deleted successfully`);
          this.loadTemplates();
        },
        error: (err) => {
          this.notificationService.error(err.error?.message || 'Failed to delete template');
        }
      });
    }
  }

  getChannelIcon(channel: NotificationChannel): string {
    switch (channel) {
      case 'EMAIL':
        return 'email';
      case 'SMS':
        return 'sms';
      case 'IN_APP':
        return 'notifications_active';
      default:
        return 'send';
    }
  }
}
