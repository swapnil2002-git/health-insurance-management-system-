import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ProviderService } from '../../../core/services/provider.service';
import { NotificationService } from '../../../core/services/notification.service';
import {
  ProviderRequest,
  ProviderResponse,
  ProviderType,
  ProviderStatus
} from '../../../core/models/provider.model';

@Component({
  selector: 'app-provider-create',
  templateUrl: './provider-create.component.html',
  styleUrls: ['./provider-create.component.scss']
})
export class ProviderCreateComponent implements OnInit {
  providerForm!: FormGroup;
  isEditMode = false;
  providerId: string | null = null;

  isSubmitting = false;
  isLoading = false;
  savedProvider: ProviderResponse | null = null;

  providerTypes: { value: ProviderType; label: string; icon: string; description: string }[] = [
    { value: 'HOSPITAL', label: 'Hospital', icon: 'local_hospital', description: 'Multi-specialty / General Hospital' },
    { value: 'CLINIC', label: 'Clinic', icon: 'medical_services', description: 'Outpatient Clinic / Primary Care' },
    { value: 'DOCTOR', label: 'Doctor / Specialist', icon: 'person', description: 'Individual Physician Practice' },
    { value: 'DIAGNOSTIC_CENTER', label: 'Diagnostic Center', icon: 'biotech', description: 'Pathology & Radiology Labs' }
  ];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private providerService: ProviderService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.initForm();

    this.route.paramMap.subscribe((params) => {
      const id = params.get('id');
      if (id) {
        this.isEditMode = true;
        this.providerId = id;
        this.loadProvider(id);
      }
    });
  }

  private initForm(): void {
    this.providerForm = this.fb.group({
      providerName: ['', [Validators.required, Validators.minLength(3)]],
      providerType: ['HOSPITAL', [Validators.required]],
      contactEmail: ['', [Validators.email]],
      contactPhone: ['', [Validators.pattern(/^[\d\s\+\-\(\)]{7,20}$/)]],
      status: ['ACTIVE', [Validators.required]],
      // Optional initial address
      streetAddress: [''],
      city: [''],
      state: [''],
      postalCode: [''],
      country: ['USA']
    });
  }

  loadProvider(id: string): void {
    this.isLoading = true;
    this.providerService.getProviderById(id).subscribe({
      next: (res) => {
        this.isLoading = false;
        this.providerForm.patchValue({
          providerName: res.providerName,
          providerType: res.providerType,
          contactEmail: res.contactEmail || '',
          contactPhone: res.contactPhone || '',
          status: res.status
        });
      },
      error: (err) => {
        this.isLoading = false;
        this.notificationService.error(
          err.error?.message || `Failed to load provider details for ID: ${id}`
        );
      }
    });
  }

  selectType(type: ProviderType): void {
    this.providerForm.patchValue({ providerType: type });
  }

  onSubmit(): void {
    if (this.providerForm.invalid) {
      this.providerForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    const val = this.providerForm.value;

    const request: ProviderRequest = {
      providerName: val.providerName.trim(),
      providerType: val.providerType,
      contactEmail: val.contactEmail?.trim() || undefined,
      contactPhone: val.contactPhone?.trim() || undefined,
      status: val.status
    };

    // If initial address filled
    if (val.streetAddress && val.city && val.state && val.postalCode) {
      request.addresses = [
        {
          streetAddress: val.streetAddress.trim(),
          city: val.city.trim(),
          state: val.state.trim(),
          postalCode: val.postalCode.trim(),
          country: val.country?.trim() || 'USA',
          primary: true
        }
      ];
    }

    if (this.isEditMode && this.providerId) {
      this.providerService.updateProvider(this.providerId, request).subscribe({
        next: (res) => {
          this.isSubmitting = false;
          this.savedProvider = res;
          this.notificationService.success(`Provider "${res.providerName}" updated successfully!`);
        },
        error: (err) => {
          this.isSubmitting = false;
          this.notificationService.error(err.error?.message || 'Failed to update provider');
        }
      });
    } else {
      this.providerService.createProvider(request).subscribe({
        next: (res) => {
          this.isSubmitting = false;
          this.savedProvider = res;
          this.notificationService.success(`Provider "${res.providerName}" enrolled successfully!`);
        },
        error: (err) => {
          this.isSubmitting = false;
          this.notificationService.error(err.error?.message || 'Failed to create provider');
        }
      });
    }
  }

  goToAddress(): void {
    if (this.savedProvider) {
      this.router.navigate(['/providers/address'], {
        queryParams: { providerId: this.savedProvider.providerId }
      });
    }
  }

  goToMapping(): void {
    if (this.savedProvider) {
      this.router.navigate(['/providers/networks/mapping'], {
        queryParams: { providerId: this.savedProvider.providerId }
      });
    }
  }

  goToView(): void {
    if (this.savedProvider) {
      this.router.navigate(['/providers/view', this.savedProvider.providerId]);
    }
  }

  resetForm(): void {
    this.savedProvider = null;
    this.providerForm.reset({
      providerType: 'HOSPITAL',
      status: 'ACTIVE',
      country: 'USA'
    });
  }
}
