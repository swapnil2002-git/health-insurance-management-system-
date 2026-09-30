import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';
import { NavItem, NAVIGATION_ITEMS } from './sidebar.model';

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.scss']
})
export class SidebarComponent implements OnInit {
  navItems: NavItem[] = [];
  userRole = '';

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.userRole = this.authService.getRole() || 'CUSTOMER';
    this.filterNavigation();
  }

  filterNavigation(): void {
    this.navItems = NAVIGATION_ITEMS.filter((item) => {
      if (!item.roles || item.roles.length === 0) return true;
      return this.authService.hasRole(item.roles);
    }).map((item) => ({
      ...item,
      expanded: false
    }));
  }

  toggleExpand(item: NavItem): void {
    item.expanded = !item.expanded;
  }

  isSubmenuActive(item: NavItem): boolean {
    if (!item.submenus) return false;
    const currentUrl = this.router.url;
    return item.submenus.some((sub) => currentUrl.startsWith(sub.route));
  }
}
