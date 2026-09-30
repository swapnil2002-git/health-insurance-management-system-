import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { UserManagementService } from '../../../core/services/user-management.service';
import { NotificationService } from '../../../core/services/notification.service';
import { SYSTEM_ROLES, UserSummaryResponse } from '../../../core/models/user.model';

@Component({
  selector: 'app-user-role',
  templateUrl: './user-role.component.html',
  styleUrls: ['./user-role.component.scss']
})
export class UserRoleComponent implements OnInit {
  roleForm!: FormGroup;
  roles = SYSTEM_ROLES;
  isLoading = false;
  isUpdating = false;
  isRejecting = false;
  pendingUsers: UserSummaryResponse[] = [];
  targetUsername = '';
  selectedUser: UserSummaryResponse | null = null;
  lastUpdatedMessage = '';

  constructor(
    private fb: FormBuilder,
    private userManagementService: UserManagementService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadPendingUsers();
  }

  private initForm(): void {
    this.roleForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      newRole: ['', [Validators.required]]
    });
  }

  loadPendingUsers(): void {
    this.isLoading = true;
    this.userManagementService.getPendingUsers().subscribe({
      next: (users) => {
        this.isLoading = false;
        this.pendingUsers = users || [];
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  selectPendingUser(user: UserSummaryResponse): void {
    this.selectedUser = user;
    this.targetUsername = user.username;
    this.roleForm.patchValue({
      username: user.username,
      newRole: user.role
    });
    this.lastUpdatedMessage = '';
  }

  onSearchUsername(): void {
    const usernameInput = this.roleForm.get('username')?.value?.trim();
    if (!usernameInput) {
      this.notificationService.error('Please enter a username or select a user.');
      return;
    }

    this.targetUsername = usernameInput;
    this.selectedUser = this.pendingUsers.find(
      u => u.username.toLowerCase() === usernameInput.toLowerCase() || u.userId === usernameInput
    ) || null;

    if (this.selectedUser) {
      this.roleForm.patchValue({
        username: this.selectedUser.username,
        newRole: this.selectedUser.role
      });
    }
  }

  onUpdateRole(): void {
    const username = this.roleForm.get('username')?.value?.trim();
    const newRole = this.roleForm.get('newRole')?.value;

    if (!username || !newRole) {
      this.roleForm.markAllAsTouched();
      return;
    }

    this.isUpdating = true;
    this.lastUpdatedMessage = '';

    this.userManagementService.assignRole(username, newRole).subscribe({
      next: (res) => {
        this.isUpdating = false;
        const msg = res.message || `Role updated to ${newRole} for user: ${username}`;
        this.lastUpdatedMessage = msg;
        this.notificationService.success(msg);
        this.loadPendingUsers();
      },
      error: () => {
        this.isUpdating = false;
        // Error notification handled by ErrorInterceptor
      }
    });
  }

  onRejectUser(): void {
    const username = this.roleForm.get('username')?.value?.trim();
    if (!username) {
      this.roleForm.get('username')?.markAsTouched();
      return;
    }

    this.isRejecting = true;
    this.userManagementService.rejectUser(username).subscribe({
      next: (res) => {
        this.isRejecting = false;
        const msg = res.message || `User ${username} registration rejected.`;
        this.notificationService.info(msg);
        this.resetForm();
        this.loadPendingUsers();
      },
      error: () => {
        this.isRejecting = false;
        // Error notification handled by ErrorInterceptor
      }
    });
  }

  resetForm(): void {
    this.roleForm.reset();
    this.targetUsername = '';
    this.selectedUser = null;
    this.lastUpdatedMessage = '';
  }
}
