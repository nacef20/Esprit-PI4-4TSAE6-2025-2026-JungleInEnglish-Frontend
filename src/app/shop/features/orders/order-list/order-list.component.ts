import { Component, inject, signal, OnInit } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { OrderService } from '../../../services/order.service';
import { AuthService } from '@shop/services/auth.service';
import { MessageService } from '../../../services/message.service';
import { ConfirmDialogComponent } from '@shop/shared/components/confirm-dialog/confirm-dialog.component';
import type { ProductOrder } from '@shop/shared/models/order.model';

@Component({
  selector: 'app-order-list',
  standalone: true,
  imports: [DecimalPipe, ConfirmDialogComponent, RouterLink],
  template: `
    <nav class="storefront-navbar">
        <div class="navbar-container">
            <div class="navbar-brand">
                <img src="assets/logo.jpg" alt="Jungle In English" class="brand-logo-img" />
                <span class="brand-text">Jungle In English</span>
            </div>
            <div class="navbar-center">
                <a routerLink="/" class="nav-link">Home</a>
                <a routerLink="/shop/products" class="nav-link">Shop</a>
                <a routerLink="/shop/my-orders" class="nav-link nav-link--active">My Orders</a>
                <a routerLink="/admin/shop" class="nav-link">Back Office</a>
            </div>
            <div class="navbar-right">
                @if (auth.isLoggedIn()) {
                    <a routerLink="/shop/cart" class="nav-link nav-link--cart">🛒 Cart</a>
                } @else {
                    <a routerLink="/auth/login" class="nav-link">Login</a>
                }
            </div>
        </div>
    </nav>

    <div class="page-hero">
        <div class="container hero-content">
            <div class="hero-text">
                <p class="hero-eyebrow">Shopping History</p>
                <h1>My Orders</h1>
                <p class="hero-desc">Track and manage your recent purchases</p>
            </div>
            <a routerLink="/shop/products" class="btn btn-hero-outline">← Continue Shopping</a>
        </div>
    </div>

    <div class="container main-content">
      @if (loading()) {
        <div class="loading-wrap">
            <div class="spinner"></div>
            <p>Loading your orders...</p>
        </div>
      } @else if (orders().length === 0) {
        <div class="empty-state">
          <div class="empty-state__icon">🛍️</div>
          <h3>Your history is empty</h3>
          <p>It looks like you haven't placed any orders yet. Start exploring our premium collection!</p>
          <a routerLink="/shop/products" class="btn btn-primary mt-2">Start Shopping</a>
        </div>
      } @else {
        <div class="orders-card">
          <div class="table-responsive">
            <table class="premium-table">
              <thead>
                <tr>
                  <th>Product</th>
                  <th>Order Date</th>
                  <th>Quantity</th>
                  <th class="text-right">Total Price</th>
                  <th>Status</th>
                  <th class="text-center">Actions</th>
                </tr>
              </thead>
              <tbody>
                @for (o of orders(); track o.id) {
                  <tr>
                    <td>
                      <div class="product-cell">
                        <div class="product-info">
                          <span class="product-name">{{ o.product.title }}</span>
                        </div>
                      </div>
                    </td>
                    <td class="date-cell">{{ o.orderDate }}</td>
                    <td class="qty-cell"><span>{{ o.quantity }}</span></td>
                    <td class="price-cell text-right">{{ (o.price * o.quantity) | number:'1.2-2' }} TND</td>
                    <td>
                      <span class="status-badge" [class]="getStatusClass(o.status)">
                        {{ o.status }}
                      </span>
                    </td>
                    <td>
                      <div class="action-buttons">
                        <button class="btn-action view" (click)="viewOrder(o)" title="Order Details">👁️</button>
                        <button class="btn-action edit" (click)="editOrder(o)" title="Update Order">✏️</button>
                        <button class="btn-action delete" (click)="confirmDelete(o)" title="Cancel Order">🗑️</button>
                      </div>
                    </td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
        </div>
      }

      @if (deletingOrder(); as order) {
        <app-confirm-dialog 
          title="Cancel Order" 
          [message]="'Are you sure you want to cancel the order for ' + order.product.title + '?'" 
          (confirm)="doDelete(order)" 
          (cancel)="deletingOrder.set(null)" />
      }
    </div>
  `,
  styles: `
    :host { 
        display: block; 
        min-height: 100vh;
        background: #f8fafc;
        padding-bottom: 5rem;
    }

    /* --- Navbar Styles (Consistency) --- */
    .storefront-navbar {
        background: #ffffff;
        border-bottom: 1px solid #e2e8f0;
        position: sticky;
        top: 0;
        z-index: 50;
        padding: 0;
    }

    .navbar-container {
        max-width: 1400px;
        margin: 0 auto;
        padding: 0 3rem;
        display: flex;
        justify-content: space-between;
        align-items: center;
        height: 60px;
    }

    .navbar-brand { display: flex; align-items: center; gap: 1rem; text-decoration: none; }
    .brand-logo-img { height: 44px; width: 44px; object-fit: cover; border-radius: 8px; }
    .brand-text { font-size: 1.2rem; font-weight: 800; color: #0f172a; letter-spacing: -0.02em; }

    .navbar-center { display: flex; align-items: center; gap: 1rem; }
    .nav-link {
        display: inline-flex;
        align-items: center;
        padding: 0 1.5rem;
        height: 60px;
        font-size: 1rem;
        font-weight: 600;
        color: #475569;
        text-decoration: none;
        transition: all 0.2s ease;
        border-bottom: 3px solid transparent;
    }
    .nav-link:hover { 
        color: #0ea37a; 
        background: rgba(14, 163, 122, 0.03);
    }
    .nav-link--active {
        color: #0ea37a;
        border-bottom-color: #0ea37a;
        font-weight: 700;
    }
    .nav-link--cart { color: #0ea37a; font-weight: 600; }

    /* --- Page Hero --- */
    .page-hero {
        background: linear-gradient(135deg, #0f172a 0%, #134e35 100%);
        padding: 3rem 0;
        color: white;
    }

    .hero-content {
        display: flex;
        justify-content: space-between;
        align-items: center;
        flex-wrap: wrap;
        gap: 2rem;
    }

    .hero-eyebrow {
        font-size: 0.8rem;
        font-weight: 700;
        text-transform: uppercase;
        letter-spacing: 0.1em;
        color: rgba(255,255,255,0.6);
        margin-bottom: 0.5rem;
    }

    .hero-text h1 {
        font-size: 2.2rem;
        font-weight: 800;
        margin: 0 0 0.5rem;
        letter-spacing: -0.02em;
    }

    .hero-desc { color: rgba(255,255,255,0.8); font-size: 1rem; margin: 0; }

    .btn-hero-outline {
        padding: 0.75rem 1.5rem;
        border: 1px solid rgba(255,255,255,0.3);
        border-radius: 99px;
        color: white;
        text-decoration: none;
        font-weight: 600;
        font-size: 0.9rem;
        transition: all 0.2s;
    }
    .btn-hero-outline:hover { background: rgba(255,255,255,0.1); border-color: white; }

    /* --- Order Card & Table --- */
    .main-content { margin-top: -2rem; position: relative; z-index: 5; }

    .orders-card {
        background: white;
        border-radius: 20px;
        box-shadow: 0 10px 40px rgba(0,0,0,0.08);
        overflow: hidden;
        border: 1px solid rgba(226, 232, 240, 0.8);
    }

    .table-responsive { overflow-x: auto; }
    
    .premium-table { width: 100%; border-collapse: collapse; text-align: left; min-width: 800px; }
    
    .premium-table th {
        background: #f8fafc;
        padding: 1.25rem 1.5rem;
        font-size: 0.75rem;
        font-weight: 700;
        text-transform: uppercase;
        letter-spacing: 0.05em;
        color: #64748b;
        border-bottom: 1px solid #edf2f7;
    }
    
    .premium-table td {
        padding: 1.5rem;
        border-bottom: 1px solid #f1f5f9;
        vertical-align: middle;
    }

    .product-name { font-weight: 700; color: #1e293b; font-size: 1rem; }
    .date-cell { color: #64748b; font-size: 0.9rem; }
    .qty-cell span {
        background: #f1f5f9;
        padding: 0.25rem 0.75rem;
        border-radius: 8px;
        font-weight: 700;
        color: #475569;
    }
    .price-cell { font-weight: 800; color: #0ea37a; font-size: 1.1rem; }

    /* --- Badges --- */
    .status-badge {
        display: inline-flex;
        align-items: center;
        padding: 0.4rem 0.8rem;
        border-radius: 99px;
        font-size: 0.75rem;
        font-weight: 700;
        text-transform: uppercase;
        letter-spacing: 0.05em;
    }
    .badge-success { background: #dcfce7; color: #166534; }
    .badge-info { background: #e0f2fe; color: #0369a1; }
    .badge-danger { background: #fee2e2; color: #991b1b; }
    .badge-warning { background: #fef9c3; color: #854d0e; }

    /* --- Action Buttons --- */
    .action-buttons { display: flex; gap: 0.5rem; justify-content: center; }
    .btn-action {
        width: 36px;
        height: 36px;
        display: flex;
        align-items: center;
        justify-content: center;
        border: none;
        border-radius: 10px;
        cursor: pointer;
        transition: all 0.2s;
        font-size: 1rem;
    }
    .btn-action.view { background: #f1f5f9; color: #64748b; }
    .btn-action.view:hover { background: #e2e8f0; color: #0f172a; }
    .btn-action.edit { background: #f0fdf9; color: #0ea37a; border: 1px solid #d1faef; }
    .btn-action.edit:hover { background: #ccfbf1; transform: translateY(-2px); }
    .btn-action.delete { background: #fff1f2; color: #e11d48; border: 1px solid #fee2e2; }
    .btn-action.delete:hover { background: #ffe4e6; transform: translateY(-2px); }

    /* --- Helpers --- */
    .text-right { text-align: right; }
    .text-center { text-align: center; }
    .mt-2 { margin-top: 1rem; }

    .loading-wrap { padding: 5rem; text-align: center; color: #64748b; }
    .spinner { 
        width: 40px; height: 40px; border: 3px solid #e2e8f0; border-top-color: #0ea37a; 
        border-radius: 50%; animation: spin 1s linear infinite; margin: 0 auto 1.5rem;
    }
    @keyframes spin { to { transform: rotate(360deg); } }

    .empty-state {
        background: white;
        padding: 5rem 2rem;
        border-radius: 20px;
        text-align: center;
        box-shadow: 0 10px 40px rgba(0,0,0,0.05);
        border: 1px solid #e2e8f0;
    }
    .empty-state__icon { font-size: 4rem; margin-bottom: 1.5rem; opacity: 0.8; }
    .empty-state h3 { font-size: 1.5rem; font-weight: 800; color: #0f172a; margin-bottom: 0.5rem; }
    .empty-state p { color: #64748b; font-size: 1rem; max-width: 400px; margin: 0 auto; }

    @keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
    .main-content { animation: fadeIn 0.5s ease-out; }
  `,
})
export class OrderListComponent implements OnInit {
  private readonly orderService = inject(OrderService);
  readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly msg = inject(MessageService);

  readonly orders = signal<ProductOrder[]>([]);
  readonly loading = signal(true);
  readonly deletingOrder = signal<ProductOrder | null>(null);

  ngOnInit(): void {
    this.refresh();
  }

  refresh(): void {
    if (!this.auth.isLoggedIn()) {
      this.loading.set(false);
      return;
    }
    this.loading.set(true);
    this.orderService.getMyOrders().subscribe({
      next: o => { this.orders.set(o); this.loading.set(false); },
      error: () => this.loading.set(false),
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

  viewOrder(order: ProductOrder): void {
    this.router.navigate(['/shop/my-orders', order.id]);
  }

  editOrder(order: ProductOrder): void {
    this.router.navigate(['/shop/my-orders', order.id, 'edit']);
  }

  confirmDelete(order: ProductOrder): void {
    this.deletingOrder.set(order);
  }

  doDelete(order: ProductOrder): void {
    this.orderService.delete(order.id).subscribe({
      next: () => {
        this.msg.success('Order deleted');
        this.deletingOrder.set(null);
        this.refresh();
      },
      error: () => this.msg.error('Error deleting order'),
    });
  }
}
