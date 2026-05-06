import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ProductService } from '../../../services/product.service';
import { CategoryService } from '../../../services/category.service';
import { MessageService } from '../../../services/message.service';
import type { Category } from '@shop/shared/models/category.model';

@Component({
  selector: 'app-product-form',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule],
  template: `
    <div class="container main-content-wrap">
      <div class="page-header" style="text-align: center; margin-bottom: 2rem;">
        <h1 class="gradient-text">{{ isEdit() ? '✏️ Edit Product' : '✨ Create New Product' }}</h1>
        <p class="subtitle">Fill out the details below to add it to your catalog.</p>
      </div>

      <form [formGroup]="form" (ngSubmit)="onSubmit()" class="form-card fade-in">
        <div class="form-row">
          <div class="form-group">
            <label for="title">Title <span class="required">*</span></label>
            <input id="title" class="form-control" formControlName="title" placeholder="e.g., Premium Leather Bag" />
            @if (form.get('title')?.invalid && form.get('title')?.touched) {
              <span class="form-error">Title is required</span>
            }
          </div>
          <div class="form-group">
            <label for="category">Category</label>
            <select id="category" class="form-control" formControlName="category">
              <option value="">Select a category...</option>
              @for (cat of categories(); track cat.id) {
                <option [value]="cat.name">{{ cat.name }}</option>
              }
            </select>
          </div>
        </div>

        <div class="form-group">
          <label for="description">Description</label>
          <textarea id="description" class="form-control" formControlName="description" rows="5" placeholder="Describe the product details, materials, and benefits..."></textarea>
        </div>

        <div class="form-group">
          <label for="image">Product Image <span class="required" *ngIf="!isEdit()">*</span></label>
          <input id="image" type="file" class="form-control file-input" (change)="onFileSelected($event)" accept="image/png,image/jpeg,image/jpg,image/webp" />
          @if (!isEdit() && !selectedFile()) {
            <span class="text-xs text-slate-400 mt-1 block">Please select an image (PNG, JPG, WEBP)</span>
          }
        </div>

        <div class="form-row" style="grid-template-columns: 1fr 1fr 1fr;">
          <div class="form-group">
            <label for="price">Price (TND) <span class="required">*</span></label>
            <input id="price" type="number" class="form-control" formControlName="price" min="0" placeholder="0.00" />
          </div>
          <div class="form-group">
            <label for="stock">Stock Quantity</label>
            <input id="stock" type="number" class="form-control" formControlName="stock" min="0" placeholder="0" />
          </div>
          <div class="form-group">
            <label for="discount">Discount (%)</label>
            <input id="discount" type="number" class="form-control" formControlName="discount" min="0" max="100" placeholder="0" />
          </div>
        </div>

        <div class="form-group checkbox-group">
          <label class="toggle-label">
            <input type="checkbox" formControlName="isActive" class="toggle-input" /> 
            <span class="toggle-text">Make Product Active</span>
          </label>
        </div>

        <div class="form-actions mt-4">
          <button type="submit" class="btn-submit" [disabled]="form.invalid">{{ isEdit() ? 'Save Changes' : 'Create Product' }}</button>
          <button type="button" class="btn-cancel" (click)="goBack()">Cancel</button>
        </div>
      </form>
    </div>
  `,
  styles: `
    :host { display: block; padding-top: 2rem; padding-bottom: 4rem; font-family: 'Inter', sans-serif; }
    
    .container { max-width: 900px; margin: 0 auto; padding: 0 1.5rem; }

    .gradient-text {
      background: linear-gradient(135deg, #0f172a 0%, #0ea37a 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      font-weight: 900;
      font-size: 2.2rem;
      letter-spacing: -0.02em;
    }

    .subtitle { color: #64748b; font-size: 1.1rem; margin-top: 0.5rem; }

    .form-card { 
      background: rgba(255, 255, 255, 0.95); 
      border: 1px solid rgba(226, 232, 240, 0.8); 
      border-radius: 20px; 
      padding: 3rem; 
      margin: 0 auto;
      box-shadow: 0 20px 40px -10px rgba(0,0,0,0.05); 
      backdrop-filter: blur(20px);
    }

    .form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 1.5rem; }
    @media (max-width: 650px) { .form-row { grid-template-columns: 1fr !important; } }
    
    .form-group { display: flex; flex-direction: column; margin-bottom: 1.5rem; }
    
    label { font-weight: 700; color: #334155; margin-bottom: 0.6rem; font-size: 0.95rem; }
    .required { color: #ef4444; }

    .form-control { 
      padding: 0.8rem 1rem; 
      border: 1px solid #cbd5e1; 
      border-radius: 10px; 
      background: #f8fafc;
      transition: all 0.2s ease; 
      font-family: inherit; 
      font-size: 1rem;
      color: #0f172a;
    }

    .form-control:focus { 
      outline: none; 
      border-color: #0ea37a; 
      box-shadow: 0 0 0 3px rgba(14, 163, 122, 0.15); 
      background: white; 
    }

    .form-error { color: #ef4444; font-size: 0.85rem; margin-top: 0.4rem; font-weight: 500; }

    /* Custom File Input Styling */
    .file-input { padding: 0.5rem; }
    .file-input::file-selector-button {
      background: #e2e8f0; 
      border: none; 
      padding: 0.6rem 1.2rem; 
      border-radius: 6px; 
      color: #334155; 
      font-weight: 700; 
      cursor: pointer; 
      transition: all 0.2s; 
      margin-right: 1.5rem;
    }
    .file-input::file-selector-button:hover { background: #cbd5e1; }

    /* Custom Checkbox toggle style */
    .checkbox-group {
      padding: 1rem;
      background: #f8fafc;
      border-radius: 10px;
      border: 1px solid #e2e8f0;
      margin-bottom: 2rem;
    }
    .toggle-label { display: flex; align-items: center; gap: 0.75rem; cursor: pointer; margin: 0; }
    .toggle-input { width: 1.2rem; height: 1.2rem; accent-color: #0ea37a; cursor: pointer; }
    .toggle-text { font-size: 1rem; font-weight: 600; color: #0f172a; }

    /* Action Buttons */
    .form-actions { display: flex; gap: 1rem; justify-content: flex-end; border-top: 1px dashed rgba(0,0,0,0.1); padding-top: 1.5rem; }
    
    .btn-submit { 
      background: linear-gradient(135deg, #0ea37a 0%, #059669 100%); 
      color: white; 
      border: none; 
      padding: 0.8rem 2.5rem; 
      border-radius: 10px; 
      font-weight: 700; 
      font-size: 1rem; 
      box-shadow: 0 4px 12px rgba(14, 163, 122, 0.3); 
      transition: all 0.2s ease; 
      cursor: pointer; 
    }
    .btn-submit:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 6px 16px rgba(14, 163, 122, 0.4); }
    .btn-submit:disabled { opacity: 0.6; cursor: not-allowed; }
    
    .btn-cancel { 
      background: white; 
      color: #475569; 
      border: 1px solid #cbd5e1; 
      padding: 0.8rem 2rem; 
      border-radius: 10px; 
      font-weight: 600; 
      font-size: 1rem; 
      transition: all 0.2s ease; 
      cursor: pointer; 
    }
    .btn-cancel:hover { background: #f1f5f9; color: #0f172a; border-color: #94a3b8; }

    .fade-in { animation: fadeIn 0.5s ease-out; }
    @keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
  `,
})
export class ProductFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly productService = inject(ProductService);
  private readonly categoryService = inject(CategoryService);
  private readonly msg = inject(MessageService);

  readonly isEdit = signal(false);
  readonly categories = signal<Category[]>([]);
  readonly selectedFile = signal<File | null>(null);
  private editId = 0;

  readonly form = this.fb.nonNullable.group({
    title: ['', Validators.required],
    description: [''],
    category: [''],
    price: [0, [Validators.required, Validators.min(0)]],
    stock: [0, Validators.min(0)],
    discount: [0, [Validators.min(0), Validators.max(100)]],
    isActive: [true],
  });

  ngOnInit(): void {
    this.categoryService.getActive().subscribe({
      next: (c) => {
        this.categories.set(c);
        if (c.length === 0) {
          console.warn('No active categories found');
        }
      },
      error: (err) => {
        console.error('Error fetching categories:', err);
        this.msg.error('Unable to load categories');
      }
    });

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEdit.set(true);
      this.editId = +id;
      this.productService.getById(this.editId).subscribe({
        next: (p) => this.form.patchValue(p),
        error: (err) => {
          console.error('Error fetching product:', err);
          this.msg.error('Unable to load product data');
        }
      });
    }
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const file = input.files[0];
      const allowedTypes = ['image/png', 'image/jpeg', 'image/jpg', 'image/webp'];
      if (!allowedTypes.includes(file.type)) {
        this.msg.error('Unsupported file format. Use PNG, JPG or WEBP.');
        input.value = '';
        return;
      }
      this.selectedFile.set(file);
    }
  }

  onSubmit(): void {
    if (this.form.invalid) return;

    if (!this.isEdit() && !this.selectedFile()) {
      this.msg.error('An image is required for a new product');
      return;
    }

    const data = this.form.getRawValue();
    const obs = this.isEdit()
      ? this.productService.update(this.editId, data, this.selectedFile() || undefined)
      : this.productService.create(data, this.selectedFile()!);

    obs.subscribe({
      next: () => {
        this.msg.success(this.isEdit() ? 'Product updated' : 'Product created');
        this.router.navigate([this.getRedirectPath()]);
      },
      error: (err) => {
        // Display the specific backend validation message if available
        const backendMsg = err?.error?.message || err?.error || null;
        if (typeof backendMsg === 'string' && backendMsg.length > 0) {
          this.msg.error(backendMsg);
        } else {
          this.msg.error('An error occurred while saving');
        }
        console.error(err);
      }
    });
  }

  private getRedirectPath(): string {
    return this.router.url.includes('/admin/shop') ? '/admin/shop/products' : '/shop/products';
  }

  goBack(): void { this.router.navigate([this.getRedirectPath()]); }
}
