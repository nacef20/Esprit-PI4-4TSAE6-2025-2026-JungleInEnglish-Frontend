import { Component, inject, signal, OnInit } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { OrderService } from '../../../services/order.service';
import { MessageService } from '../../../services/message.service';
import { PaginationComponent } from '@shop/shared/components/pagination/pagination.component';
import type { ProductOrder } from '@shop/shared/models/order.model';

@Component({
  selector: 'app-order-admin',
  standalone: true,
  imports: [FormsModule, PaginationComponent, DecimalPipe],
  template: `
    <div class="admin-container">
      <div class="admin-header">
        <div>
          <h1>📦 Order Management</h1>
          <p>Monitor and process customer orders</p>
        </div>
      </div>

      <div class="admin-toolbar">
        <div class="admin-search">
          <input type="text" placeholder="Search by Order ID..." [ngModel]="searchId()" (ngModelChange)="searchId.set($event)" (keyup.enter)="onSearch()" />
        </div>
        <button class="admin-btn admin-btn--primary" (click)="onSearch()">Search Order</button>
      </div>

      @if (loading()) {
        <div class="admin-loading">
          <div class="spinner"></div>
          <p>Analyzing orders database...</p>
        </div>
      } @else {
        <div class="admin-table-wrap">
          <table>
            <thead>
              <tr>
                <th>Order ID</th>
                <th>Product</th>
                <th>Customer</th>
                <th>Order Date</th>
                <th>Revenue</th>
                <th>Status</th>
                <th style="text-align: right;">Actions</th>
              </tr>
            </thead>
            <tbody>
              @for (o of orders(); track o.id) {
                <tr>
                  <td><span class="admin-uuid" title="{{ o.orderId }}">#{{ o.orderId }}</span></td>
                  <td><div style="font-weight: 700; color: #0f172a;">{{ o.product.title }}</div></td>
                  <td>
                     <div style="font-size: 0.8rem; color: #64748b; text-transform: uppercase; font-weight: 700; letter-spacing: 0.05em;">Customer</div>
                     <div style="font-weight: 600; color: #334155;">{{ o.userId }}</div>
                  </td>
                  <td style="font-size: 0.85rem; color: #64748b; font-weight: 500;">{{ o.orderDate }}</td>
                  <td><div style="font-weight: 800; color: #10b981; font-size: 1.05rem;">{{ (o.price * o.quantity) | number:'1.2-2' }} TND</div></td>
                  <td>
                    <span class="admin-badge" [class]="getStatusClass(o.status)">
                      {{ o.status }}
                    </span>
                  </td>
                  <td>
                    <div style="display: flex; gap: 0.5rem; justify-content: flex-end; align-items: center;">
                       <select class="admin-select" (change)="updateStatus(o.id, $event)">
                        <option value="">Update Status...</option>
                        <option value="1">Pending</option>
                        <option value="2">In Progress</option>
                        <option value="3">Completed</option>
                        <option value="4">Cancelled</option>
                        <option value="5">Shipped</option>
                        <option value="6">Delivered</option>
                      </select>
                    </div>
                  </td>
                </tr>
              } @empty {
                <tr><td colspan="7" style="text-align: center; padding: 4rem; color: #94a3b8;">No orders found in the system.</td></tr>
              }
            </tbody>
          </table>
        </div>
        
        <div style="margin-top: 1.5rem;">
          <app-pagination [pageNo]="pageNo()" [totalPages]="totalPages()" [totalElements]="totalElements()" [first]="first()" [last]="last()" (pageChange)="onPageChange($event)" />
        </div>
      }
    </div>
  `,
  styles: `
    :host { display: block; }
    .admin-select {
      padding: 0.4rem 0.75rem;
      border: 1px solid #e2e8f0;
      border-radius: 8px;
      font-size: 0.8rem;
      font-weight: 600;
      background: white;
      color: #2D5757;
      cursor: pointer;
      &:focus { outline: none; border-color: #2D5757; }
    }
  `,
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
      next: () => { this.msg.success('Order status updated successfully'); this.load(); },
    });
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'Delivered': case 'Completed': return 'admin-badge--success';
      case 'Cancelled': return 'admin-badge--danger';
      case 'Shipped': case 'In Progress': return 'admin-badge--info';
      default: return 'admin-badge--warning';
    }
  }
}
