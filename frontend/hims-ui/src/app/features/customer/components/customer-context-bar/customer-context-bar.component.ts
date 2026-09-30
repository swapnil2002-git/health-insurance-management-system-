import { Component, OnInit } from '@angular/core';
import { CustomerContextService } from '../../../../core/services/customer-context.service';
import { CustomerService } from '../../../../core/services/customer.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { CustomerResponse } from '../../../../core/models/customer.model';

@Component({
  selector: 'app-customer-context-bar',
  templateUrl: './customer-context-bar.component.html',
  styleUrls: ['./customer-context-bar.component.scss']
})
export class CustomerContextBarComponent implements OnInit {
  customer: CustomerResponse | null = null;
  inputCustomerId = '';
  isLoading = false;
  isRefreshing = false;

  constructor(
    private customerContextService: CustomerContextService,
    private customerService: CustomerService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.customerContextService.activeCustomer$.subscribe((c) => {
      this.customer = c;
      if (c) {
        this.inputCustomerId = c.customerId;
      }
    });
  }

  loadCustomer(): void {
    const id = this.inputCustomerId.trim();
    if (!id) {
      this.notificationService.error('Please enter a Customer ID (UUID).');
      return;
    }

    this.isLoading = true;
    this.customerService.getCustomer(id).subscribe({
      next: (cust) => {
        this.isLoading = false;
        this.customerContextService.setActiveCustomer(cust);
        this.notificationService.success(`Loaded customer: ${cust.firstName} ${cust.lastName}`);
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  refreshCustomer(): void {
    if (!this.customer) return;

    this.isRefreshing = true;
    this.customerService.getCustomer(this.customer.customerId).subscribe({
      next: (cust) => {
        this.isRefreshing = false;
        this.customerContextService.setActiveCustomer(cust);
        this.notificationService.success(`Customer profile refreshed: ${cust.firstName} ${cust.lastName}`);
      },
      error: () => {
        this.isRefreshing = false;
      }
    });
  }

  clearCustomer(): void {
    this.customerContextService.setActiveCustomer(null);
    this.inputCustomerId = '';
    this.notificationService.info('Switched customer. Select or enter another customer ID.');
  }
}
