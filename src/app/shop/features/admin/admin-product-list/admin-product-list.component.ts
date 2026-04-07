import { Component, inject, signal, OnInit } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ProductService } from '../../../services/product.service';
import { MessageService } from '../../../services/message.service';
import { PaginationComponent } from '@shop/shared/components/pagination/pagination.component';
import { ConfirmDialogComponent } from '@shop/shared/components/confirm-dialog/confirm-dialog.component';
import type { Product } from '@shop/shared/models/product.model';

@Component({
  selector: 'app-admin-product-list',
  standalone: true,
  imports: [RouterLink, FormsModule, PaginationComponent, ConfirmDialogComponent, DecimalPipe],
  template: `
    <div class="admin-container">
      <div class="admin-header">
        <div>
          <h1>📦 Product Management</h1>
          <p>View, edit and manage your store's inventory</p>
        </div>
        <a routerLink="create" class="admin-btn admin-btn--primary">
          <svg width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M12 4v16m8-8H4"/></svg>
          New Product
        </a>
      </div>

      <div class="admin-toolbar">
        <div class="admin-search">
          <input type="text" placeholder="Search products by title..." [ngModel]="search()" (ngModelChange)="onSearch($event)" />
        </div>
      </div>

      @if (loading()) {
        <div class="admin-loading">
          <div class="spinner"></div>
          <p>Fetching product catalog...</p>
        </div>
      } @else {
        <div class="admin-table-wrap">
          <table>
            <thead>
              <tr>
                <th>#</th>
                <th>Product Info</th>
                <th>Category</th>
                <th>Pricing</th>
                <th>Stock</th>
                <th>Status</th>
                <th style="text-align: right;">Actions</th>
              </tr>
            </thead>
            <tbody>
              @for (p of products(); track p.id) {
                <tr>
                  <td style="color: #64748b; font-size: 0.8rem; font-family: monospace;">#{{ p.id }}</td>
                  <td>
                    <div style="font-weight: 700; color: #2D5757;">{{ p.title }}</div>
                  </td>
                  <td>
                    <span class="admin-badge admin-badge--primary">{{ p.category }}</span>
                  </td>
                  <td>
                    <div style="font-weight: 700; color: #2D5757;">{{ p.discountPrice | number:'1.2-2' }} TND</div>
                    @if (p.discount > 0) {
                      <div style="font-size: 0.75rem; color: #ef4444; text-decoration: line-through; opacity: 0.7;">{{ p.price | number:'1.2-2' }} TND</div>
                    }
                  </td>
                  <td>
                    <div [style.color]="p.stock <= 5 ? '#ef4444' : 'inherit'" style="font-weight: 600;">
                      {{ p.stock }}
                      @if (p.stock <= 5) { <span style="font-size: 0.7rem; margin-left: 2px;">⚠️</span> }
                    </div>
                  </td>
                  <td>
                    <span class="admin-badge" [class.admin-badge--success]="p.isActive" [class.admin-badge--danger]="!p.isActive">
                      {{ p.isActive ? 'Active' : 'Inactive' }}
                    </span>
                  </td>
                  <td>
                    <div style="display: flex; gap: 0.5rem; justify-content: flex-end;">
                      <a [routerLink]="[p.id, 'edit']" class="admin-btn admin-btn--secondary admin-btn--sm" title="Edit Product">✏️</a>
                      <button class="admin-btn admin-btn--danger admin-btn--sm" (click)="deletingProduct.set(p)" title="Delete Product">🗑️</button>
                    </div>
                  </td>
                </tr>
              } @empty {
                <tr><td colspan="7" style="text-align: center; padding: 4rem; color: #94a3b8;">No products found in the catalog.</td></tr>
              }
            </tbody>
          </table>
        </div>
        
        <div style="margin-top: 1.5rem;">
          <app-pagination [pageNo]="pageNo()" [totalPages]="totalPages()" [totalElements]="totalElements()" [first]="first()" [last]="last()" (pageChange)="onPageChange($event)" />
        </div>
      }

      @if (deletingProduct(); as p) {
        <app-confirm-dialog 
          title="Delete Product" 
          [message]="'Are you sure you want to delete \u00ab ' + p.title + ' \u00bb? This action cannot be undone.'" 
          (confirm)="doDelete(p)" 
          (cancel)="deletingProduct.set(null)" />
      }
    </div>
  `,
  styles: `
    :host { display: block; }
  `,
})
export class AdminProductListComponent implements OnInit {
  private readonly productService = inject(ProductService);
  private readonly msg = inject(MessageService);

  readonly products = signal<Product[]>([]);
  readonly loading = signal(true);
  readonly search = signal('');
  readonly deletingProduct = signal<Product | null>(null);
  readonly pageNo = signal(0);
  readonly totalPages = signal(0);
  readonly totalElements = signal(0);
  readonly first = signal(true);
  readonly last = signal(true);

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.productService.getAll(this.pageNo(), 10, this.search()).subscribe({
      next: res => { this.products.set(res.content); this.pageNo.set(res.pageNo); this.totalPages.set(res.totalPages); this.totalElements.set(res.totalElements); this.first.set(res.first); this.last.set(res.last); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
  }

  onSearch(term: string): void { this.search.set(term); this.pageNo.set(0); this.load(); }
  onPageChange(p: number): void { this.pageNo.set(p); this.load(); }
  doDelete(p: Product): void {
    this.productService.delete(p.id).subscribe({ next: () => { this.msg.success('Product deleted'); this.deletingProduct.set(null); this.load(); } });
  }
}
