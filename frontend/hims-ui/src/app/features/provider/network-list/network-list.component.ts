import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ProviderService } from '../../../core/services/provider.service';
import { NotificationService } from '../../../core/services/notification.service';
import {
  ProviderNetworkResponse,
  ProviderNetworkMappingResponse
} from '../../../core/models/provider.model';

@Component({
  selector: 'app-network-list',
  templateUrl: './network-list.component.html',
  styleUrls: ['./network-list.component.scss']
})
export class NetworkListComponent implements OnInit {
  networks: ProviderNetworkResponse[] = [];
  selectedNetwork: ProviderNetworkResponse | null = null;
  enrolledProviders: ProviderNetworkMappingResponse[] = [];

  isLoadingNetworks = false;
  isLoadingEnrolled = false;
  searchText = '';

  constructor(
    private router: Router,
    private providerService: ProviderService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.loadNetworks();
  }

  loadNetworks(): void {
    this.isLoadingNetworks = true;
    this.providerService.getAllNetworks().subscribe({
      next: (list) => {
        this.networks = list || [];
        this.isLoadingNetworks = false;
        if (this.networks.length > 0 && !this.selectedNetwork) {
          this.selectNetwork(this.networks[0]);
        }
      },
      error: (err) => {
        this.isLoadingNetworks = false;
        this.networks = [];
        this.notificationService.error(err.error?.message || 'Failed to retrieve networks');
      }
    });
  }

  selectNetwork(net: ProviderNetworkResponse): void {
    this.selectedNetwork = net;
    this.loadEnrolledProviders(net.networkId);
  }

  loadEnrolledProviders(networkId: string): void {
    this.isLoadingEnrolled = true;
    this.providerService.getProvidersInNetwork(networkId).subscribe({
      next: (mappings) => {
        this.enrolledProviders = mappings || [];
        this.isLoadingEnrolled = false;
      },
      error: (err) => {
        this.isLoadingEnrolled = false;
        this.enrolledProviders = [];
        this.notificationService.error(err.error?.message || 'Failed to retrieve enrolled providers');
      }
    });
  }

  get filteredNetworks(): ProviderNetworkResponse[] {
    if (!this.searchText.trim()) {
      return this.networks;
    }
    const q = this.searchText.toLowerCase().trim();
    return this.networks.filter(
      (n) =>
        n.networkName.toLowerCase().includes(q) ||
        (n.description && n.description.toLowerCase().includes(q)) ||
        n.status.toLowerCase().includes(q)
    );
  }

  onRemoveProvider(networkId: string, providerId: string, event: Event): void {
    event.stopPropagation();
    if (!confirm('Are you sure you want to remove this provider from the network?')) {
      return;
    }

    this.providerService.removeProviderFromNetwork(networkId, providerId).subscribe({
      next: () => {
        this.notificationService.success('Provider removed from network');
        this.loadEnrolledProviders(networkId);
      },
      error: (err) => {
        this.notificationService.error(err.error?.message || 'Failed to remove provider from network');
      }
    });
  }

  editNetwork(networkId: string, event?: Event): void {
    if (event) event.stopPropagation();
    this.router.navigate(['/providers/networks/edit', networkId]);
  }

  assignProvider(networkId: string): void {
    this.router.navigate(['/providers/networks/mapping'], {
      queryParams: { networkId }
    });
  }

  testEligibility(networkId: string, providerId?: string): void {
    const queryParams: any = { networkId };
    if (providerId) {
      queryParams.providerId = providerId;
    }
    this.router.navigate(['/providers/networks/mapping'], { queryParams });
  }

  viewProvider(providerId: string): void {
    this.router.navigate(['/providers/view', providerId]);
  }
}
