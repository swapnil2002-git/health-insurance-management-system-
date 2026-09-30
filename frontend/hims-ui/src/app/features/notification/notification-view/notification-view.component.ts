import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { NotificationApiService } from '../../../core/services/notification-api.service';
import { NotificationService } from '../../../core/services/notification.service';
import { NotificationResponse } from '../../../core/models/notification.model';

@Component({
  selector: 'app-notification-view',
  templateUrl: './notification-view.component.html',
  styleUrls: ['./notification-view.component.scss']
})
export class NotificationViewComponent implements OnInit {
  searchForm!: FormGroup;
  notification: NotificationResponse | null = null;
  isLoading = false;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private notificationApiService: NotificationApiService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.searchForm = this.fb.group({
      notificationId: ['', [Validators.required]]
    });

    this.route.paramMap.subscribe((params) => {
      const id = params.get('id');
      if (id) {
        this.searchForm.patchValue({ notificationId: id });
        this.fetchNotification(id);
      }
    });
  }

  onSearch(): void {
    if (this.searchForm.invalid) return;
    const id = this.searchForm.value.notificationId.trim();
    this.router.navigate(['/notifications/view', id]);
  }

  fetchNotification(id: string): void {
    this.isLoading = true;
    this.notification = null;
    this.notificationApiService.getNotificationById(id).subscribe({
      next: (data) => {
        this.isLoading = false;
        this.notification = data;
      },
      error: (err) => {
        this.isLoading = false;
        this.notificationService.error(err.error?.message || `Notification ${id} not found`);
      }
    });
  }

  viewRelated(): void {
    if (this.notification?.referenceId) {
      this.router.navigate(['/notifications/reference', this.notification.referenceId]);
    }
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
