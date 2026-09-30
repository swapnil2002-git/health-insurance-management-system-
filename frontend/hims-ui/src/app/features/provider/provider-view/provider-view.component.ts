import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ProviderService } from '../../../core/services/provider.service';
import { NotificationService } from '../../../core/services/notification.service';
import {
  ProviderResponse,
  ProviderNetworkMappingResponse,
  ProviderStatus
} from '../../../core/models/provider.model';

@Component({
  selector: 'app-provider-view',
  templateUrl: './provider-view.component.html',
  styleUrls: ['./provider-view.component.scss']
})
export class ProviderViewComponent implements OnInit {
  providerIdInput: string = '';
  provider: ProviderResponse | null = null;
  networks: ProviderNetworkMappingResponse[] = [];

  isLoading = false;
  isUpdatingStatus = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private providerService: ProviderService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe((params) => {
      const id = params.get('id');
      if (id) {
        this.providerIdInput = id;
        this.fetchProvider(id);
      } else {
        this.route.queryParams.subscribe((q) => {
          if (q['providerId']) {
            this.providerIdInput = q['providerId'];
            this.fetchProvider(this.providerIdInput);
          }
        });
      }
    });
  }

  onSearch(): void {
    if (this.providerIdInput.trim()) {
      this.fetchProvider(this.providerIdInput.trim());
    }
  }

  fetchProvider(id: string): void {
    this.isLoading = true;
    this.providerService.getProviderById(id).subscribe({
      next: (res) => {
        this.provider = res;
        this.isLoading = false;
        this.fetchNetworks(id);
      },
      error: (err) => {
        this.isLoading = false;
        this.provider = null;
        this.networks = [];
        this.notificationService.error(
          err.error?.message || `Provider not found for ID: ${id}`
        );
      }
    });
  }

  fetchNetworks(providerId: string): void {
    this.providerService.getNetworksForProvider(providerId).subscribe({
      next: (list) => {
        this.networks = list || [];
      },
      error: () => {
        this.networks = [];
      }
    });
  }

  toggleStatus(): void {
    if (!this.provider) return;

    const newStatus: ProviderStatus = this.provider.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
    this.isUpdatingStatus = true;

    this.providerService.updateProviderStatus(this.provider.providerId, newStatus).subscribe({
      next: (res) => {
        this.isUpdatingStatus = false;
        this.provider = res;
        this.notificationService.success(`Provider status updated to ${newStatus}!`);
      },
      error: (err) => {
        this.isUpdatingStatus = false;
        this.notificationService.error(err.error?.message || 'Failed to update provider status');
      }
    });
  }

  goToEdit(): void {
    if (this.provider) {
      this.router.navigate(['/providers/edit', this.provider.providerId]);
    }
  }

  goToAddress(): void {
    if (this.provider) {
      this.router.navigate(['/providers/address'], {
        queryParams: { providerId: this.provider.providerId }
      });
    }
  }

  goToMapping(): void {
    if (this.provider) {
      this.router.navigate(['/providers/networks/mapping'], {
        queryParams: { providerId: this.provider.providerId }
      });
    }
  }

  printProfile(): void {
    window.print();
  }
}
