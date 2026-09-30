import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ProviderService } from '../../../core/services/provider.service';
import { NotificationService } from '../../../core/services/notification.service';
import {
  ProviderResponse,
  ProviderAddressResponse,
  ProviderAddressRequest
} from '../../../core/models/provider.model';

@Component({
  selector: 'app-provider-address',
  templateUrl: './provider-address.component.html',
  styleUrls: ['./provider-address.component.scss']
})
export class ProviderAddressComponent implements OnInit {
  addressForm!: FormGroup;
  providerIdInput: string = '';
  provider: ProviderResponse | null = null;
  addresses: ProviderAddressResponse[] = [];

  isLoading = false;
  isAdding = false;
  deletingId: string | null = null;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private providerService: ProviderService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.initForm();

    this.route.queryParams.subscribe((params) => {
      if (params['providerId']) {
        this.providerIdInput = params['providerId'];
        this.loadProvider(this.providerIdInput);
      }
    });
  }

  private initForm(): void {
    this.addressForm = this.fb.group({
      streetAddress: ['', [Validators.required, Validators.minLength(3)]],
      city: ['', [Validators.required]],
      state: ['', [Validators.required]],
      postalCode: ['', [Validators.required]],
      country: ['USA'],
      primary: [false]
    });
  }

  onSearch(): void {
    if (this.providerIdInput.trim()) {
      this.loadProvider(this.providerIdInput.trim());
    }
  }

  loadProvider(id: string): void {
    this.isLoading = true;
    this.providerService.getProviderById(id).subscribe({
      next: (res) => {
        this.provider = res;
        this.isLoading = false;
        this.loadAddresses(id);
      },
      error: (err) => {
        this.isLoading = false;
        this.provider = null;
        this.addresses = [];
        this.notificationService.error(
          err.error?.message || `Provider not found for ID: ${id}`
        );
      }
    });
  }

  loadAddresses(providerId: string): void {
    this.providerService.getAddresses(providerId).subscribe({
      next: (list) => {
        this.addresses = list || [];
      },
      error: () => {
        this.addresses = [];
      }
    });
  }

  onAddAddress(): void {
    if (!this.provider || this.addressForm.invalid) {
      this.addressForm.markAllAsTouched();
      return;
    }

    this.isAdding = true;
    const req: ProviderAddressRequest = {
      streetAddress: this.addressForm.value.streetAddress.trim(),
      city: this.addressForm.value.city.trim(),
      state: this.addressForm.value.state.trim(),
      postalCode: this.addressForm.value.postalCode.trim(),
      country: this.addressForm.value.country?.trim() || 'USA',
      primary: !!this.addressForm.value.primary
    };

    this.providerService.addAddress(this.provider.providerId, req).subscribe({
      next: () => {
        this.isAdding = false;
        this.notificationService.success('Address added successfully!');
        this.addressForm.reset({
          country: 'USA',
          primary: false
        });
        this.loadAddresses(this.provider!.providerId);
      },
      error: (err) => {
        this.isAdding = false;
        this.notificationService.error(err.error?.message || 'Failed to add address');
      }
    });
  }

  deleteAddress(addressId: string): void {
    if (!confirm('Are you sure you want to delete this address?')) return;

    this.deletingId = addressId;
    this.providerService.deleteAddress(addressId).subscribe({
      next: () => {
        this.deletingId = null;
        this.notificationService.success('Address deleted successfully!');
        if (this.provider) {
          this.loadAddresses(this.provider.providerId);
        }
      },
      error: (err) => {
        this.deletingId = null;
        this.notificationService.error(err.error?.message || 'Failed to delete address');
      }
    });
  }

  goToView(): void {
    if (this.provider) {
      this.router.navigate(['/providers/view', this.provider.providerId]);
    }
  }

  goToMapping(): void {
    if (this.provider) {
      this.router.navigate(['/providers/networks/mapping'], {
        queryParams: { providerId: this.provider.providerId }
      });
    }
  }
}
