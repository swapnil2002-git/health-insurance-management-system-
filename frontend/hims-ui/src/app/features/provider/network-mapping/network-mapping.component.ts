import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ProviderService } from '../../../core/services/provider.service';
import { NotificationService } from '../../../core/services/notification.service';
import {
  ProviderResponse,
  ProviderNetworkResponse,
  ProviderNetworkMappingResponse,
  ProviderNetworkEligibilityResponse
} from '../../../core/models/provider.model';

@Component({
  selector: 'app-network-mapping',
  templateUrl: './network-mapping.component.html',
  styleUrls: ['./network-mapping.component.scss']
})
export class NetworkMappingComponent implements OnInit {
  mappingForm!: FormGroup;
  eligibilityForm!: FormGroup;

  providers: ProviderResponse[] = [];
  networks: ProviderNetworkResponse[] = [];

  isLoadingProviders = false;
  isLoadingNetworks = false;
  isSubmittingMapping = false;
  isCheckingEligibility = false;

  eligibilityResult: ProviderNetworkEligibilityResponse | null = null;
  activeProviderMappings: ProviderNetworkMappingResponse[] = [];
  isLoadingMappings = false;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private providerService: ProviderService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.initForms();
    this.loadProviders();
    this.loadNetworks();

    this.route.queryParams.subscribe((params) => {
      if (params['providerId']) {
        this.mappingForm.patchValue({ providerId: params['providerId'] });
        this.eligibilityForm.patchValue({ providerId: params['providerId'] });
        this.loadProviderMappings(params['providerId']);
      }
      if (params['networkId']) {
        this.mappingForm.patchValue({ networkId: params['networkId'] });
        this.eligibilityForm.patchValue({ networkId: params['networkId'] });
      }
    });
  }

  private initForms(): void {
    this.mappingForm = this.fb.group({
      providerId: ['', [Validators.required]],
      networkId: ['', [Validators.required]],
      active: [true]
    });

    this.eligibilityForm = this.fb.group({
      providerId: ['', [Validators.required]],
      networkId: ['', [Validators.required]]
    });
  }

  loadProviders(): void {
    this.isLoadingProviders = true;
    this.providerService.getAllProviders().subscribe({
      next: (list) => {
        this.providers = list || [];
        this.isLoadingProviders = false;
      },
      error: (err) => {
        this.isLoadingProviders = false;
        this.notificationService.error(err.error?.message || 'Failed to fetch providers');
      }
    });
  }

  loadNetworks(): void {
    this.isLoadingNetworks = true;
    this.providerService.getAllNetworks().subscribe({
      next: (list) => {
        this.networks = list || [];
        this.isLoadingNetworks = false;
      },
      error: (err) => {
        this.isLoadingNetworks = false;
        this.notificationService.error(err.error?.message || 'Failed to fetch networks');
      }
    });
  }

  onProviderChange(providerId: string): void {
    if (providerId) {
      this.loadProviderMappings(providerId);
    } else {
      this.activeProviderMappings = [];
    }
  }

  loadProviderMappings(providerId: string): void {
    this.isLoadingMappings = true;
    this.providerService.getNetworksForProvider(providerId).subscribe({
      next: (mappings) => {
        this.activeProviderMappings = mappings || [];
        this.isLoadingMappings = false;
      },
      error: () => {
        this.isLoadingMappings = false;
        this.activeProviderMappings = [];
      }
    });
  }

  onMapSubmit(): void {
    if (this.mappingForm.invalid) {
      this.mappingForm.markAllAsTouched();
      return;
    }

    this.isSubmittingMapping = true;
    const formVal = this.mappingForm.value;

    this.providerService
      .mapProviderToNetwork({
        providerId: formVal.providerId,
        networkId: formVal.networkId,
        active: formVal.active
      })
      .subscribe({
        next: (res) => {
          this.isSubmittingMapping = false;
          this.notificationService.success('Provider successfully enrolled in network');
          this.loadProviderMappings(formVal.providerId);
          // Also set up eligibility form
          this.eligibilityForm.patchValue({
            providerId: formVal.providerId,
            networkId: formVal.networkId
          });
        },
        error: (err) => {
          this.isSubmittingMapping = false;
          this.notificationService.error(err.error?.message || 'Failed to map provider to network');
        }
      });
  }

  onRemoveMapping(networkId: string, providerId: string): void {
    if (!confirm('Are you sure you want to remove this provider from the network?')) {
      return;
    }

    this.providerService.removeProviderFromNetwork(networkId, providerId).subscribe({
      next: () => {
        this.notificationService.success('Provider removed from network');
        this.loadProviderMappings(providerId);
      },
      error: (err) => {
        this.notificationService.error(err.error?.message || 'Failed to unmap provider');
      }
    });
  }

  checkEligibility(): void {
    if (this.eligibilityForm.invalid) {
      this.eligibilityForm.markAllAsTouched();
      return;
    }

    const { networkId, providerId } = this.eligibilityForm.value;
    this.isCheckingEligibility = true;
    this.eligibilityResult = null;

    this.providerService.verifyEligibility(networkId, providerId).subscribe({
      next: (result) => {
        this.eligibilityResult = result;
        this.isCheckingEligibility = false;
      },
      error: (err) => {
        this.isCheckingEligibility = false;
        this.notificationService.error(err.error?.message || 'Failed to verify in-network eligibility');
      }
    });
  }

  getProviderName(providerId: string): string {
    const p = this.providers.find((item) => item.providerId === providerId);
    return p ? p.providerName : providerId;
  }

  getNetworkName(networkId: string): string {
    const n = this.networks.find((item) => item.networkId === networkId);
    return n ? n.networkName : networkId;
  }
}
