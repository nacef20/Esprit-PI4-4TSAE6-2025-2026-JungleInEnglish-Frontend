import { Component, inject, signal, OnInit } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { OrderService } from '../../../core/services/order.service';
import { MessageService } from '../../../core/services/message.service';
import { PaginationComponent } from '../../../shared/components/pagination/pagination.component';
import type { ProductOrder } from '../../../shared/models/order.model';

@Component({
  selector: 'app-order-admin',
  standalone: true,
  imports: [FormsModule, PaginationComponent, DecimalPipe],
  template: `
    <div class="container">
      <div class="page-header">
        <h1>📦 Gestion des commandes</h1>
      </div>

      <div class="toolbar">
        <div class="search-bar">
          <input type="text" placeholder="Rechercher par ID commande..." [ngModel]="searchId()" (ngModelChange)="searchId.set($event)" (keyup.enter)="onSearch()" />
        </div>
        <button class="btn btn-secondary" (click)="onSearch()">Rechercher</button>
      </div>

      @if (loading()) {
        <div class="loading-center"><div class="spinner"></div></div>
      } @else {
        <div class="table-wrap">
          <table>
            <thead><tr><th>ID</th><th>Produit</th><th>Client</th><th>Date</th><th>Prix</th><th>Statut</th><th>Action</th></tr></thead>
            <tbody>
              @for (o of orders(); track o.id) {
                <tr>
                  <td><code>{{ o.orderId }}</code></td>
                  <td>{{ o.product?.title }}</td>
                  <td>{{ o.user?.name ?? o.user?.email }}</td>
                  <td>{{ o.orderDate }}</td>
                  <td class="price">{{ o.price | number:'1.2-2' }} TND</td>
                  <td><span class="badge" [class]="getStatusClass(o.status)">{{ o.status }}</span></td>
                  <td>
                    <select class="form-control" style="width:auto;padding:.375rem .5rem;font-size:.8rem;" (change)="updateStatus(o.id, $event)">
                      <option value="">Changer...</option>
                      <option value="1">Pending</option>
                      <option value="2">In Progress</option>
                      <option value="3">Completed</option>
                      <option value="4">Cancelled</option>
                      <option value="5">Shipped</option>
                      <option value="6">Delivered</option>
                    </select>
                  </td>
                </tr>
              } @empty {
                <tr><td colspan="7" class="text-center text-muted" style="padding:2rem;">Aucune commande</td></tr>
              }
            </tbody>
          </table>
        </div>
        <app-pagination [pageNo]="pageNo()" [totalPages]="totalPages()" [totalElements]="totalElements()" [first]="first()" [last]="last()" (pageChange)="onPageChange($event)" />
      }
    </div>
  `,
  styles: `:host { display: block; padding-bottom: 3rem; } code { font-size: .8rem; background: var(--color-surface-elevated); padding: .15rem .4rem; border-radius: var(--radius-sm); }`,
})
export class OrderAdminComponent implements OnInit {
  private readonly orderService = inject(OrderService);
  private readonly msg = inject(MessageService);

  readonly orders = signal<ProductOrder[]>([]);
  readonly loading = signal(true);
  readonly searchId = signal('');
  readonly pageNo = signal(0);
  readonly totalPages = signal(0);
  readonly totalElements = signal(0);
  readonly first = signal(true);
  readonly last = signal(true);

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.orderService.getAll(this.pageNo()).subscribe({
      next: res => { this.orders.set(res.content); this.pageNo.set(res.pageNo); this.totalPages.set(res.totalPages); this.totalElements.set(res.totalElements); this.first.set(res.first); this.last.set(res.last); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
  }

  onSearch(): void {
    const id = this.searchId().trim();
    if (!id) { this.load(); return; }
    this.loading.set(true);
    this.orderService.searchByOrderId(id).subscribe({
      next: o => { this.orders.set([o]); this.totalPages.set(1); this.totalElements.set(1); this.first.set(true); this.last.set(true); this.loading.set(false); },
      error: () => { this.orders.set([]); this.loading.set(false); },
    });
  }

  onPageChange(p: number): void { this.pageNo.set(p); this.load(); }

  updateStatus(orderId: number, e: Event): void {
    const val = +(e.target as HTMLSelectElement).value;
    if (!val) return;
    this.orderService.updateStatus(orderId, val).subscribe({
      next: () => { this.msg.success('Statut mis à jour'); this.load(); },
    });
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'Delivered': return 'badge-success';
      case 'Cancelled': return 'badge-danger';
      case 'Shipped': case 'In Progress': return 'badge-info';
      default: return 'badge-warning';
    }
  }
}
