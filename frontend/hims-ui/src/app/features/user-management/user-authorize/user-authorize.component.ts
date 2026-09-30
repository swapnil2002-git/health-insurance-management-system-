import { Component, OnInit } from '@angular/core';
import { UserManagementService } from '../../../core/services/user-management.service';
import { NotificationService } from '../../../core/services/notification.service';
import { UserSummaryResponse } from '../../../core/models/user.model';

@Component({
  selector: 'app-user-authorize',
  templateUrl: './user-authorize.component.html',
  styleUrls: ['./user-authorize.component.scss']
})
export class UserAuthorizeComponent implements OnInit {
  pendingUsers: UserSummaryResponse[] = [];
  selectedUser: UserSummaryResponse | null = null;
  selectedUserId: string = '';
  isLoading = false;
  isActionInProgress = false;

  constructor(
    private userManagementService: UserManagementService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.loadPendingUsers();
  }

  loadPendingUsers(): void {
    this.isLoading = true;
    this.userManagementService.getPendingUsers().subscribe({
      next: (users: UserSummaryResponse[]) => {
        this.isLoading = false;
        this.pendingUsers = users || [];
        // If current selection is still in list, preserve it; otherwise clear
        if (this.selectedUser) {
          const match = this.pendingUsers.find(u => u.userId === this.selectedUser?.userId);
          this.selectedUser = match || null;
          this.selectedUserId = match ? match.userId : '';
        }
      },
      error: () => {
        this.isLoading = false;
        // Error notification handled by ErrorInterceptor
      }
    });
  }

  onUserSelect(userId: string): void {
    this.selectedUserId = userId;
    this.selectedUser = this.pendingUsers.find(u => u.userId === userId) || null;
  }

  selectUser(user: UserSummaryResponse): void {
    this.selectedUserId = user.userId;
    this.selectedUser = user;
  }

  approveUser(user: UserSummaryResponse): void {
    if (!user || this.isActionInProgress) return;
    this.isActionInProgress = true;

    this.userManagementService.approveUser(user.username).subscribe({
      next: (res) => {
        this.isActionInProgress = false;
        this.notificationService.success(res.message || `User ${user.username} approved successfully!`);
        this.clearSelectionIfMatches(user.userId);
        this.loadPendingUsers();
      },
      error: () => {
        this.isActionInProgress = false;
        // Error notification handled by ErrorInterceptor
      }
    });
  }

  rejectUser(user: UserSummaryResponse): void {
    if (!user || this.isActionInProgress) return;
    this.isActionInProgress = true;

    this.userManagementService.rejectUser(user.username).subscribe({
      next: (res) => {
        this.isActionInProgress = false;
        this.notificationService.info(res.message || `User ${user.username} registration rejected.`);
        this.clearSelectionIfMatches(user.userId);
        this.loadPendingUsers();
      },
      error: () => {
        this.isActionInProgress = false;
        // Error notification handled by ErrorInterceptor
      }
    });
  }

  private clearSelectionIfMatches(userId: string): void {
    if (this.selectedUser?.userId === userId) {
      this.selectedUser = null;
      this.selectedUserId = '';
    }
  }
}
