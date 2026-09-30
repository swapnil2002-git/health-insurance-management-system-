import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { NotificationApiService } from '../../../core/services/notification-api.service';
import { NotificationService } from '../../../core/services/notification.service';
import { NotificationResponse } from '../../../core/models/notification.model';

@Component({
  selector: 'app-notification-reference',
  templateUrl: './notification-reference.component.html',
  styleUrls: ['./notification-reference.component.scss']
})
export class NotificationReferenceComponent implements OnInit {
  searchForm!: FormGroup;
  notifications: NotificationResponse[] = [];
  isLoading = false;
  searchType: 'REFERENCE_ID' | 'RECIPIENT' = 'REFERENCE_ID';
  searchedTerm = '';

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private notificationApiService: NotificationApiService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.searchForm = this.fb.group({
      searchTerm: ['', [Validators.required]]
    });

    this.route.paramMap.subscribe((params) => {
      const refId = params.get('refId');
      if (refId) {
        this.searchType = 'REFERENCE_ID';
        this.searchForm.patchValue({ searchTerm: refId });
        this.fetchByReferenceId(refId);
      }
    });
  }

  onSearchTypeChange(type: 'REFERENCE_ID' | 'RECIPIENT'): void {
    this.searchType = type;
    this.notifications = [];
    this.searchedTerm = '';
  }

  onSearch(): void {
    if (this.searchForm.invalid) return;

    const term = this.searchForm.value.searchTerm.trim();
    this.searchedTerm = term;

    if (this.searchType === 'REFERENCE_ID') {
      this.fetchByReferenceId(term);
    } else {
      this.fetchByRecipient(term);
    }
  }

  fetchByReferenceId(refId: string): void {
    this.isLoading = true;
    this.notificationApiService.getNotificationsByReferenceId(refId).subscribe({
      next: (list) => {
        this.isLoading = false;
        this.notifications = list || [];
        if (this.notifications.length === 0) {
          this.notificationService.warning(`No notification records found for reference ID ${refId}`);
        }
      },
      error: (err) => {
        this.isLoading = false;
        this.notificationService.error(err.error?.message || 'Failed to fetch notifications by reference ID');
      }
    });
  }

  fetchByRecipient(recipient: string): void {
    this.isLoading = true;
    this.notificationApiService.getNotificationsByRecipient(recipient).subscribe({
      next: (list) => {
        this.isLoading = false;
        this.notifications = list || [];
        if (this.notifications.length === 0) {
          this.notificationService.warning(`No notification records found for recipient ${recipient}`);
        }
      },
      error: (err) => {
        this.isLoading = false;
        this.notificationService.error(err.error?.message || 'Failed to fetch notifications by recipient');
      }
    });
  }

  viewDetails(notificationId: string): void {
    this.router.navigate(['/notifications/view', notificationId]);
  }

  getChannelIcon(channel: string): string {
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
