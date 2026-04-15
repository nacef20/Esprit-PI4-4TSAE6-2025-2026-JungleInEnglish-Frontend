import { Component, inject, signal, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CategoryService } from '../../../services/category.service';
import { MessageService } from '../../../services/message.service';
import { PaginationComponent } from '@shop/shared/components/pagination/pagination.component';
import { ConfirmDialogComponent } from '@shop/shared/components/confirm-dialog/confirm-dialog.component';
import type { Category } from '@shop/shared/models/category.model';

@Component({
  selector: 'app-category-list',
  standalone: true,
  imports: [RouterLink, PaginationComponent, ConfirmDialogComponent],
  template: `
    <div class="admin-container">
      <div class="admin-header">
        <div>
          <h1>📂 Categories</h1>
          <p>Manage product categories</p>
        </div>
        <a routerLink="create" class="admin-btn admin-btn--primary">
          <svg width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M12 4v16m8-8H4"/></svg>
          New Category
        </a>
      </div>

      @if (loading()) {
        <div class="admin-loading">
          <div class="spinner"></div>
        </div>
      } @else {
        <div class="admin-table-wrap">
          <table>
            <thead>
              <tr><th>#</th><th>Name</th><th>Image</th><th>Status</th><th style="text-align: right;">Actions</th></tr>
            </thead>
            <tbody>
              @for (cat of categories(); track cat.id) {
                <tr>
                  <td style="color: #64748b; font-size: 0.8rem; font-family: monospace;">#{{ cat.id }}</td>
                  <td><div style="font-weight: 700; color: #2D5757;">{{ cat.name }}</div></td>
                  <td style="font-size: 1.5rem;">{{ cat.imageName }}</td>
                  <td>
                    <span class="admin-badge" [class.admin-badge--success]="cat.isActive" [class.admin-badge--danger]="!cat.isActive">
                      {{ cat.isActive ? 'Active' : 'Inactive' }}
                    </span>
                  </td>
                  <td>
                    <div style="display: flex; gap: 0.5rem; justify-content: flex-end;">
                      <a [routerLink]="[cat.id, 'edit']" class="admin-btn admin-btn--secondary admin-btn--sm">✏️</a>
                      <button class="admin-btn admin-btn--danger admin-btn--sm" (click)="confirmDelete(cat)">🗑️</button>
                    </div>
                  </td>
                </tr>
              } @empty {
                <tr><td colspan="5" style="text-align: center; padding: 4rem; color: #94a3b8;">No categories found</td></tr>
              }
            </tbody>
          </table>
        </div>
        <app-pagination [pageNo]="pageNo()" [totalPages]="totalPages()" [totalElements]="totalElements()" [first]="first()" [last]="last()" (pageChange)="onPageChange($event)" />
      }

      @if (deletingCat(); as cat) {
        <app-confirm-dialog title="Delete Category" [message]="'Delete \u00ab ' + cat.name + ' \u00bb ?'" (confirm)="doDelete(cat)" (cancel)="deletingCat.set(null)" />
      }
    </div>
  `,
  styles: `
    :host { display: block; }
    .admin-btn {
      display: inline-flex; align-items: center; justify-content: center;
      padding: 0.625rem 1.25rem;
      border-radius: 8px;
      font-weight: 600;
      cursor: pointer;
      text-decoration: none;
      transition: all 0.2s;
    }
    .admin-btn--primary {
      background-color: #10b981;
      color: white;
      border: 1px solid #10b981;
    }
    .admin-btn--primary:hover {
      background-color: #059669;
    }
    .admin-btn svg { margin-right: 0.5rem; }
    
    .admin-badge {
      display: inline-block;
      padding: 0.25rem 0.75rem;
      border-radius: 999px;
      font-size: 0.8rem;
      font-weight: 600;
    }
    .admin-badge--success { background: #dcfce7; color: #15803d; }
    .admin-badge--danger { background: #fee2e2; color: #dc2626; }
    
    .admin-btn--sm {
      padding: 0.35rem 0.5rem;
      border-radius: 6px;
      font-size: 0.875rem;
    }
    .admin-btn--secondary { background: #f8fafc; color: #475569; border: 1px solid #e2e8f0; }
    .admin-btn--secondary:hover { background: #f1f5f9; }
    
    .admin-btn--danger { background: #fff1f2; color: #e11d48; border: 1px solid #fecdd3; }
    .admin-btn--danger:hover { background: #ffe4e6; }
  `,
})
export class CategoryListComponent implements OnInit {
  private readonly catService = inject(CategoryService);
  private readonly msg = inject(MessageService);

  readonly categories = signal<Category[]>([]);
  readonly loading = signal(true);
  readonly deletingCat = signal<Category | null>(null);
  readonly pageNo = signal(0);
  readonly totalPages = signal(0);
  readonly totalElements = signal(0);
  readonly first = signal(true);
  readonly last = signal(true);

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.catService.getAll(this.pageNo()).subscribe({
      next: res => { this.categories.set(res.content); this.pageNo.set(res.pageNo); this.totalPages.set(res.totalPages); this.totalElements.set(res.totalElements); this.first.set(res.first); this.last.set(res.last); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
  }

  onPageChange(p: number): void { this.pageNo.set(p); this.load(); }
  confirmDelete(cat: Category): void { this.deletingCat.set(cat); }
  doDelete(cat: Category): void {
    this.catService.delete(cat.id).subscribe({ next: () => { this.msg.success('Category deleted'); this.deletingCat.set(null); this.load(); } });
  }
}
