import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ProviderService } from '../../../core/services/provider.service';
import { NotificationService } from '../../../core/services/notification.service';
import { NetworkStatus, ProviderNetworkRequest } from '../../../core/models/provider.model';

@Component({
  selector: 'app-network-create',
  templateUrl: './network-create.component.html',
  styleUrls: ['./network-create.component.scss']
})
export class NetworkCreateComponent implements OnInit {
  networkForm!: FormGroup;
  networkId?: string;
  isEditMode = false;
  isLoading = false;
  isSubmitting = false;

  statusOptions: NetworkStatus[] = ['ACTIVE', 'INACTIVE'];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private providerService: ProviderService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.networkId = this.route.snapshot.paramMap.get('id') || undefined;
    this.isEditMode = !!this.networkId;

    this.initForm();

    if (this.isEditMode && this.networkId) {
      this.loadNetwork(this.networkId);
    }
  }

  private initForm(): void {
    this.networkForm = this.fb.group({
      networkName: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
      description: ['', [Validators.maxLength(255)]],
      status: ['ACTIVE' as NetworkStatus, [Validators.required]]
    });
  }

  private loadNetwork(id: string): void {
    this.isLoading = true;
    this.providerService.getNetworkById(id).subscribe({
      next: (net) => {
        this.networkForm.patchValue({
          networkName: net.networkName,
          description: net.description || '',
          status: net.status
        });
        this.isLoading = false;
      },
      error: (err) => {
        this.isLoading = false;
        this.notificationService.error(err.error?.message || 'Failed to load network details');
        this.router.navigate(['/providers/networks']);
      }
    });
  }

  onSubmit(): void {
    if (this.networkForm.invalid) {
      this.networkForm.markAllAsTouched();
      return;
    }

    const payload: ProviderNetworkRequest = {
      networkName: this.networkForm.value.networkName.trim(),
      description: this.networkForm.value.description ? this.networkForm.value.description.trim() : undefined,
      status: this.networkForm.value.status
    };

    this.isSubmitting = true;

    if (this.isEditMode && this.networkId) {
      this.providerService.updateNetwork(this.networkId, payload).subscribe({
        next: (res) => {
          this.isSubmitting = false;
          this.notificationService.success(`Network "${res.networkName}" updated successfully`);
          this.router.navigate(['/providers/networks']);
        },
        error: (err) => {
          this.isSubmitting = false;
          this.notificationService.error(err.error?.message || 'Failed to update network');
        }
      });
    } else {
      this.providerService.createNetwork(payload).subscribe({
        next: (res) => {
          this.isSubmitting = false;
          this.notificationService.success(`Network "${res.networkName}" registered successfully`);
          this.router.navigate(['/providers/networks']);
        },
        error: (err) => {
          this.isSubmitting = false;
          this.notificationService.error(err.error?.message || 'Failed to register provider network');
        }
      });
    }
  }

  cancel(): void {
    this.router.navigate(['/providers/networks']);
  }
}
