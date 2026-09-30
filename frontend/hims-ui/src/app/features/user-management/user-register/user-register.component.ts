import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { UserManagementService } from '../../../core/services/user-management.service';
import { NotificationService } from '../../../core/services/notification.service';
import { SYSTEM_ROLES } from '../../../core/models/user.model';

@Component({
  selector: 'app-user-register',
  templateUrl: './user-register.component.html',
  styleUrls: ['./user-register.component.scss']
})
export class UserRegisterComponent implements OnInit {
  registerForm!: FormGroup;
  isLoading = false;
  hidePassword = true;
  roles = SYSTEM_ROLES;
  successMessage = '';

  constructor(
    private fb: FormBuilder,
    private userManagementService: UserManagementService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.initForm();
  }

  private initForm(): void {
    this.registerForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50), Validators.pattern(/^[a-zA-Z0-9._-]+$/)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(100)]],
      role: ['CUSTOMER', [Validators.required]]
    });
  }

  get usernameControl() {
    return this.registerForm.get('username');
  }

  get emailControl() {
    return this.registerForm.get('email');
  }

  get passwordControl() {
    return this.registerForm.get('password');
  }

  get roleControl() {
    return this.registerForm.get('role');
  }

  onSubmit(): void {
    if (this.registerForm.invalid || this.isLoading) {
      this.registerForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    this.successMessage = '';

    const req = this.registerForm.value;

    this.userManagementService.registerUser(req).subscribe({
      next: (responseMsg: string) => {
        this.isLoading = false;
        this.successMessage = responseMsg;
        this.notificationService.success(responseMsg);
        this.registerForm.reset({
          username: '',
          email: '',
          password: '',
          role: 'CUSTOMER'
        });
      },
      error: () => {
        this.isLoading = false;
        // Clean error notification message is displayed by ErrorInterceptor
      }
    });
  }

  resetForm(): void {
    this.registerForm.reset({
      username: '',
      email: '',
      password: '',
      role: 'CUSTOMER'
    });
    this.successMessage = '';
  }
}
