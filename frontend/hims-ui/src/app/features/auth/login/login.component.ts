import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';
import { NotificationService } from '../../../core/services/notification.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {
  loginForm!: FormGroup;
  isLoading = false;
  returnUrl = '/dashboard';
  hidePassword = true;

  // Pre-configured test accounts seeded in identity-service
  demoAccounts = [
    { role: 'Admin', username: 'admin', pass: 'Admin@123' },
    { role: 'Underwriter', username: 'underwriter1', pass: 'Underwriter@123' },
    { role: 'Claims Officer', username: 'claims_officer1', pass: 'Claims@123' },
    { role: 'Agent', username: 'agent1', pass: 'Agent@123' },
    { role: 'Finance', username: 'finance1', pass: 'Finance@123' },
    { role: 'Provider', username: 'provider1', pass: 'Provider@123' }
  ];

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private notificationService: NotificationService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    if (this.authService.isAuthenticated()) {
      this.router.navigate(['/dashboard']);
    }

    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/dashboard';

    this.loginForm = this.fb.group({
      username: ['', [Validators.required]],
      password: ['', [Validators.required]]
    });
  }

  fillDemo(account: { username: string; pass: string }): void {
    this.loginForm.patchValue({
      username: account.username,
      password: account.pass
    });
  }

  onSubmit(): void {
    if (this.loginForm.invalid || this.isLoading) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    const { username, password } = this.loginForm.value;

    this.authService.login({ username, password }).subscribe({
      next: (res) => {
        this.isLoading = false;
        this.notificationService.success(`Welcome back, ${res.username}! Signed in as [${res.role}].`);
        this.router.navigateByUrl(this.returnUrl);
      },
      error: () => {
        this.isLoading = false;
        // Error notification handled by ErrorInterceptor
      }
    });
  }

  goToRegister(): void {
    this.router.navigate(['/register']);
  }
}
