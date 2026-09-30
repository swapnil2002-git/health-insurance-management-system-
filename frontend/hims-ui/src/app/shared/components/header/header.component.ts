import { Component, EventEmitter, Output, OnInit } from '@angular/core';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit {
  @Output() toggleSidebar = new EventEmitter<void>();

  username = '';
  role = '';
  todayDate: Date = new Date();

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    this.username = this.authService.getUsername() || 'Staff Member';
    this.role = this.authService.getRole() || 'CUSTOMER';

    // Update time every minute
    setInterval(() => {
      this.todayDate = new Date();
    }, 60000);
  }

  onToggle(): void {
    this.toggleSidebar.emit();
  }

  logout(): void {
    this.authService.logout();
  }
}
