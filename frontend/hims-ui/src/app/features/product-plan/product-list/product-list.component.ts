import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ProductPlanService } from '../../../core/services/product-plan.service';
import { NotificationService } from '../../../core/services/notification.service';
import { ProductResponse } from '../../../core/models/product-plan.model';

@Component({
  selector: 'app-product-list',
  templateUrl: './product-list.component.html',
  styleUrls: ['./product-list.component.scss']
})
export class ProductListComponent implements OnInit {
  productForm!: FormGroup;
  products: ProductResponse[] = [];
  filteredProducts: ProductResponse[] = [];
  searchTerm = '';
  isLoading = false;
  isSubmitting = false;
  isEditing = false;
  editingProductId: string | null = null;

  constructor(
    private fb: FormBuilder,
    private productPlanService: ProductPlanService,
    private notificationService: NotificationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadProducts();
  }

  private initForm(): void {
    this.productForm = this.fb.group({
      name: ['', [Validators.required]],
      description: ['']
    });
  }

  get nameControl() { return this.productForm.get('name'); }
  get descriptionControl() { return this.productForm.get('description'); }

  loadProducts(): void {
    this.isLoading = true;
    this.productPlanService.getProducts().subscribe({
      next: (list) => {
        this.isLoading = false;
        this.products = list || [];
        this.applyFilter();
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  applyFilter(): void {
    const term = this.searchTerm.trim().toLowerCase();
    if (!term) {
      this.filteredProducts = [...this.products];
    } else {
      this.filteredProducts = this.products.filter(
        (p) =>
          p.name.toLowerCase().includes(term) ||
          (p.description && p.description.toLowerCase().includes(term)) ||
          p.productId.toLowerCase().includes(term)
      );
    }
  }

  startEdit(product: ProductResponse): void {
    this.isEditing = true;
    this.editingProductId = product.productId;
    this.productForm.patchValue({
      name: product.name,
      description: product.description || ''
    });
  }

  cancelEdit(): void {
    this.isEditing = false;
    this.editingProductId = null;
    this.productForm.reset();
  }

  onSubmit(): void {
    if (this.productForm.invalid || this.isSubmitting) {
      this.productForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    const req = this.productForm.value;

    if (this.isEditing && this.editingProductId) {
      this.productPlanService.updateProduct(this.editingProductId, req).subscribe({
        next: (updated) => {
          this.isSubmitting = false;
          this.notificationService.success(`Product "${updated.name}" updated successfully.`);
          this.cancelEdit();
          this.loadProducts();
        },
        error: () => {
          this.isSubmitting = false;
        }
      });
    } else {
      this.productPlanService.createProduct(req).subscribe({
        next: (created) => {
          this.isSubmitting = false;
          this.notificationService.success(`Product "${created.name}" created successfully.`);
          this.productForm.reset();
          this.loadProducts();
        },
        error: () => {
          this.isSubmitting = false;
        }
      });
    }
  }

  viewPlans(productId: string): void {
    this.router.navigate(['/plans'], { queryParams: { productId } });
  }
}
