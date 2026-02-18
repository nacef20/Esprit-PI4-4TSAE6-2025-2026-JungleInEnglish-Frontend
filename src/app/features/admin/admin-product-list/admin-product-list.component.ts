import { Component, inject, signal, OnInit } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ProductService } from '../../../core/services/product.service';
import { MessageService } from '../../../core/services/message.service';
import { PaginationComponent } from '../../../shared/components/pagination/pagination.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import type { Product } from '../../../shared/models/product.model';

@Component({
  selector: 'app-admin-product-list',
  standalone: true,
  imports: [RouterLink, FormsModule, PaginationComponent, ConfirmDialogComponent, DecimalPipe],
  template: `
    <div class="container">
      <div class="page-header flex-between">
        <div>
          <h1>📦 Produits</h1>
          <p>Gérer les produits du catalogue</p>
        </div>
        <a routerLink="/products/create" class="btn btn-primary">+ Nouveau produit</a>
      </div>

      <div class="toolbar">
        <div class="search-bar">
          <input type="text" placeholder="Rechercher un produit..." [ngModel]="search()" (ngModelChange)="onSearch($event)" />
        </div>
      </div>

      @if (loading()) {
        <div class="loading-center"><div class="spinner"></div></div>
      } @else {
        <div class="table-wrap">
          <table>
            <thead><tr><th>#</th><th>Titre</th><th>Catégorie</th><th>Prix</th><th>Stock</th><th>Statut</th><th>Actions</th></tr></thead>
            <tbody>
              @for (p of products(); track p.id) {
                <tr>
                  <td>{{ p.id }}</td>
                  <td><strong>{{ p.title }}</strong></td>
                  <td><span class="badge badge-primary">{{ p.category }}</span></td>
                  <td>
                    <span class="price">{{ p.discountPrice | number:'1.2-2' }} TND</span>
                    @if (p.discount > 0) { <span class="discount-badge">-{{ p.discount }}%</span> }
                  </td>
                  <td [class.text-danger]="p.stock <= 0">{{ p.stock }}</td>
                  <td><span class="badge" [class.badge-success]="p.isActive" [class.badge-danger]="!p.isActive">{{ p.isActive ? 'Actif' : 'Inactif' }}</span></td>
                  <td class="flex gap-1">
                    <a [routerLink]="['/products', p.id, 'edit']" class="btn btn-sm btn-secondary">✏️</a>
                    <button class="btn btn-sm btn-danger" (click)="deletingProduct.set(p)">🗑️</button>
                  </td>
                </tr>
              } @empty {
                <tr><td colspan="7" class="text-center text-muted" style="padding:2rem;">Aucun produit</td></tr>
              }
            </tbody>
          </table>
        </div>
        <app-pagination [pageNo]="pageNo()" [totalPages]="totalPages()" [totalElements]="totalElements()" [first]="first()" [last]="last()" (pageChange)="onPageChange($event)" />
      }

      @if (deletingProduct(); as p) {
        <app-confirm-dialog title="Supprimer le produit" [message]="'Supprimer « ' + p.title + ' » ?'" (confirm)="doDelete(p)" (cancel)="deletingProduct.set(null)" />
      }
    </div>
  `,
  styles: `:host { display: block; padding-bottom: 3rem; }`,
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
    this.productService.delete(p.id).subscribe({ next: () => { this.msg.success('Produit supprimé'); this.deletingProduct.set(null); this.load(); } });
  }
}
