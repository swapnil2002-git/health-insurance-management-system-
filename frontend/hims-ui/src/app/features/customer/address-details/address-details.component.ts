import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormGroupDirective, Validators } from '@angular/forms';
import { CustomerService } from '../../../core/services/customer.service';
import { CustomerContextService } from '../../../core/services/customer-context.service';
import { NotificationService } from '../../../core/services/notification.service';
import { CustomerResponse, AddressResponse, ADDRESS_TYPES } from '../../../core/models/customer.model';

@Component({
  selector: 'app-address-details',
  templateUrl: './address-details.component.html',
  styleUrls: ['./address-details.component.scss']
})
export class AddressDetailsComponent implements OnInit {
  addressForm!: FormGroup;
  customer: CustomerResponse | null = null;
  addresses: AddressResponse[] = [];
  isLoading = false;
  isSubmitting = false;
  isEditing = false;
  editingAddressId: string | null = null;
  addressTypes = ADDRESS_TYPES;

  constructor(
    private fb: FormBuilder,
    private customerService: CustomerService,
    private customerContextService: CustomerContextService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.customerContextService.activeCustomer$.subscribe((c) => {
      this.customer = c;
      if (c) {
        this.loadAddresses(c.customerId);
      } else {
        this.addresses = [];
      }
    });
  }

  private initForm(): void {
    this.addressForm = this.fb.group({
      addressType: ['RESIDENTIAL', [Validators.required]],
      street1: ['', [Validators.required]],
      street2: [''],
      city: ['', [Validators.required]],
      state: ['', [Validators.required]],
      zipCode: ['', [Validators.required]],
      country: ['India', [Validators.required]]
    });
  }

  get addressTypeControl() { return this.addressForm.get('addressType'); }
  get street1Control() { return this.addressForm.get('street1'); }
  get cityControl() { return this.addressForm.get('city'); }
  get stateControl() { return this.addressForm.get('state'); }
  get zipControl() { return this.addressForm.get('zipCode'); }
  get countryControl() { return this.addressForm.get('country'); }

  loadAddresses(customerId: string): void {
    this.isLoading = true;
    this.customerService.getAddresses(customerId).subscribe({
      next: (list) => {
        this.isLoading = false;
        this.addresses = list || [];
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  startEdit(addr: AddressResponse): void {
    this.isEditing = true;
    this.editingAddressId = addr.customerAddressId;
    this.addressForm.patchValue({
      addressType: addr.addressType,
      street1: addr.street1,
      street2: addr.street2 || '',
      city: addr.city,
      state: addr.state,
      zipCode: addr.zipCode,
      country: addr.country
    });
  }

  cancelEdit(formDirective?: FormGroupDirective): void {
    this.isEditing = false;
    this.editingAddressId = null;
    if (formDirective) {
      formDirective.resetForm({
        addressType: 'RESIDENTIAL',
        country: 'India'
      });
    } else {
      this.addressForm.reset({
        addressType: 'RESIDENTIAL',
        country: 'India'
      });
    }
  }

  onSubmit(formDirective?: FormGroupDirective): void {
    if (!this.customer) {
      this.notificationService.error('Please select an active customer first.');
      return;
    }

    if (this.addressForm.invalid || this.isSubmitting) {
      this.addressForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    const req = this.addressForm.value;

    if (this.isEditing && this.editingAddressId) {
      this.customerService.updateAddress(this.customer.customerId, this.editingAddressId, req).subscribe({
        next: (updated) => {
          this.isSubmitting = false;
          this.notificationService.success(`Address (${updated.addressType}) updated successfully.`);
          this.cancelEdit(formDirective);
          if (this.customer) this.loadAddresses(this.customer.customerId);
        },
        error: () => {
          this.isSubmitting = false;
        }
      });
    } else {
      this.customerService.addAddress(this.customer.customerId, req).subscribe({
        next: (created) => {
          this.isSubmitting = false;
          this.notificationService.success(`Address (${created.addressType}) saved successfully.`);
          if (formDirective) {
            formDirective.resetForm({
              addressType: 'RESIDENTIAL',
              country: 'India'
            });
          } else {
            this.addressForm.reset({
              addressType: 'RESIDENTIAL',
              country: 'India'
            });
          }
          if (this.customer) this.loadAddresses(this.customer.customerId);
        },
        error: () => {
          this.isSubmitting = false;
        }
      });
    }
  }

  deleteAddress(addr: AddressResponse): void {
    if (!this.customer) return;
    const confirmed = confirm(`Are you sure you want to remove address: ${addr.street1}, ${addr.city}?`);
    if (!confirmed) return;

    this.customerService.removeAddress(this.customer.customerId, addr.customerAddressId).subscribe({
      next: () => {
        this.notificationService.info(`Address removed.`);
        if (this.customer) this.loadAddresses(this.customer.customerId);
      }
    });
  }
}
