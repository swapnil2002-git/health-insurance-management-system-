import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ProviderService } from '../../../core/services/provider.service';
import { NotificationService } from '../../../core/services/notification.service';
import { ProviderResponse, ProviderStatus } from '../../../core/models/provider.model';

@Component({
  selector: 'app-provider-list',
  templateUrl: './provider-list.component.html',
  styleUrls: ['./provider-list.component.scss']
})
export class ProviderListComponent implements OnInit {
  providers: ProviderResponse[] = [];
  filteredProviders: ProviderResponse[] = [];

  statusFilter: string = 'ALL';
  searchText: string = '';
  isLoading = false;

  constructor(
    private router: Router,
    private providerService: ProviderService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.loadProviders();
  }

  loadProviders(): void {
    this.isLoading = true;
    const paramStatus = this.statusFilter === 'ALL' ? undefined : (this.statusFilter as ProviderStatus);

    this.providerService.getAllProviders(paramStatus).subscribe({
      next: (list) => {
        this.providers = list || [];
        this.applyFilter();
        this.isLoading = false;
      },
      error: (err) => {
        this.isLoading = false;
        this.providers = [];
        this.filteredProviders = [];
        this.notificationService.error(err.error?.message || 'Failed to retrieve providers');
      }
    });
  }

  onFilterChange(status: string): void {
    this.statusFilter = status;
    this.loadProviders();
  }

  applyFilter(): void {
    if (!this.searchText.trim()) {
      this.filteredProviders = [...this.providers];
    } else {
      const q = this.searchText.toLowerCase().trim();
      this.filteredProviders = this.providers.filter(
        (p) =>
          p.providerName.toLowerCase().includes(q) ||
          p.providerType.toLowerCase().includes(q) ||
          (p.contactEmail && p.contactEmail.toLowerCase().includes(q)) ||
          (p.contactPhone && p.contactPhone.toLowerCase().includes(q)) ||
          p.providerId.toLowerCase().includes(q)
      );
    }
  }

  toggleStatus(p: ProviderResponse, event: Event): void {
    event.stopPropagation();
    const newStatus: ProviderStatus = p.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';

    this.providerService.updateProviderStatus(p.providerId, newStatus).subscribe({
      next: (updated) => {
        p.status = updated.status;
        this.notificationService.success(`Status for ${p.providerName} updated to ${newStatus}`);
      },
      error: (err) => {
        this.notificationService.error(err.error?.message || 'Failed to update status');
      }
    });
  }

  get totalCount(): number {
    return this.providers.length;
  }

  get activeCount(): number {
    return this.providers.filter((p) => p.status === 'ACTIVE').length;
  }

  get inactiveCount(): number {
    return this.providers.filter((p) => p.status === 'INACTIVE').length;
  }

  get hospitalCount(): number {
    return this.providers.filter((p) => p.providerType === 'HOSPITAL').length;
  }

  viewProvider(id: string): void {
    this.router.navigate(['/providers/view', id]);
  }

  editProvider(id: string, event: Event): void {
    event.stopPropagation();
    this.router.navigate(['/providers/edit', id]);
  }

  manageAddress(id: string, event: Event): void {
    event.stopPropagation();
    this.router.navigate(['/providers/address'], {
      queryParams: { providerId: id }
    });
  }

  manageMapping(id: string, event: Event): void {
    event.stopPropagation();
    this.router.navigate(['/providers/networks/mapping'], {
      queryParams: { providerId: id }
    });
  }
}
