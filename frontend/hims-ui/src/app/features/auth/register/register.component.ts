import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';
import { NotificationService } from '../../../core/services/notification.service';
import { SYSTEM_ROLES } from '../../../core/models/user.model';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent implements OnInit {
  registerForm!: FormGroup;
  isLoading = false;
  hidePassword = true;
  roles = SYSTEM_ROLES;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private notificationService: NotificationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.registerForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      email: ['', [Validators.required, Validators.email]],
      role: ['CUSTOMER', [Validators.required]]
    });
  }

  onSubmit(): void {
    if (this.registerForm.invalid || this.isLoading) {
      this.registerForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    const request = this.registerForm.value;

    this.authService.register(request).subscribe({
      next: (message) => {
        this.isLoading = false;
        this.notificationService.success(message || 'Registration successful!');
        this.router.navigate(['/login']);
      },
      error: () => {
        this.isLoading = false;
        // Error notification handled by ErrorInterceptor
      }
    });
  }

  goToLogin(): void {
    this.router.navigate(['/login']);
  }
}
