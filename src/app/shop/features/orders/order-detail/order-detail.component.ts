import { Component, inject, signal, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { DecimalPipe } from '@angular/common';
import { OrderService } from '../../../services/order.service';
import { MessageService } from '../../../services/message.service';
import type { ProductOrder } from '@shop/shared/models/order.model';

@Component({
  selector: 'app-order-detail',
  standalone: true,
  imports: [DecimalPipe],
  template: `
    <div class="container container--narrow">
      <div class="page-header flex-between slide-down">
        <div>
          <h1 class="gradient-text"><span class="emoji">📄</span> Order Details</h1>
          <p class="text-muted" style="margin-top: 0.25rem;">Review your order information below</p>
        </div>
        <button class="btn btn-outline" (click)="goBack()">
          <span style="margin-right: 5px;">←</span> Back to Orders
        </button>
      </div>

      @if (loading()) {
        <div class="loading-center"><div class="spinner"></div></div>
      } @else {
        @if (order(); as o) {
          <div class="detail-grid fade-in">
          
            <!-- Status & Info Card -->
            <div class="detail-card glass-card">
              <div class="flex-between mb-2">
                <div class="flex" style="gap: 10px; align-items: center;">
                  <div class="icon-box info-box">📅</div>
                  <div>
                    <span class="text-muted" style="display:block; font-size: 0.85rem;">Date Placed</span>
                    <strong>{{ o.orderDate }}</strong>
                  </div>
                </div>
                <span class="badge" [class]="getStatusClass(o.status)">
                  <span class="pulse-dot"></span> {{ o.status }}
                </span>
              </div>
              <div class="divider"></div>
              <div class="flex-between mt-2">
                <div class="flex" style="gap: 10px; align-items: center;">
                  <div class="icon-box warning-box">💳</div>
                  <div>
                    <span class="text-muted" style="display:block; font-size: 0.85rem;">Payment Method</span>
                    <strong>{{ getPaymentMethodName(o.paymentType) }}</strong>
                  </div>
                </div>
              </div>
            </div>

            <!-- Product Card -->
            <div class="detail-card glass-card">
              <h3 class="card-title">📦 Ordered Product</h3>
              <div class="product-info flex gap-2">
                <div class="product-info__content" style="width: 100%;">
                  <div class="flex-between" style="align-items: center;">
                    <strong class="product-name">{{ o.product.title }}</strong>
                    <span class="badge badge-neutral">{{ o.quantity }} Items</span>
                  </div>
                  <div class="flex-between mt-1">
                    <span class="text-muted">Unit Price</span>
                    <span>{{ o.price | number:'1.2-2' }} TND</span>
                  </div>
                  <div class="flex-between mt-1 total-row">
                    <span>Total Amount</span>
                    <span class="total-price">{{ (o.price * o.quantity) | number:'1.2-2' }} TND</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- Shipping Address -->
            <div class="detail-card glass-card">
              <h3 class="card-title">📍 Shipping Information</h3>
              <div class="address-box">
                <div class="flex-between" style="align-items: flex-start;">
                  <div>
                    <p class="customer-name">{{ o.orderAddress?.firstName }} {{ o.orderAddress?.lastName }}</p>
                    <p class="text-muted">{{ o.orderAddress?.address }}</p>
                    <p class="text-muted">{{ o.orderAddress?.city }}, {{ o.orderAddress?.state }} {{ o.orderAddress?.postalCode }}</p>
                  </div>
                  <div class="contact-badges">
                    <span class="contact-badge">📞 {{ o.orderAddress?.telephone }}</span>
                    <span class="contact-badge">✉️ {{ o.orderAddress?.email }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- Action Buttons -->
            <div class="action-bar float-up">
              <button class="btn btn-primary action-btn edit-btn" (click)="editOrder(o)">
                ✏️ Edit Details
              </button>
              <button class="btn btn-info action-btn track-btn" (click)="openMap(o.id)">
                🗺️ Track Live
              </button>
            </div>
            
          </div>
        }
      }
    </div>
  `,
  styles: `
    :host { 
      display: block; 
      padding-bottom: 4rem; 
      font-family: 'Inter', sans-serif;
    }
    
    .container--narrow { 
      max-width: 650px; 
      margin: 0 auto;
      padding-top: 1rem;
    }

    .page-header {
      margin-bottom: 2rem;
      border-bottom: 1px solid rgba(0,0,0,0.05);
      padding-bottom: 1rem;
    }

    .gradient-text {
      background: linear-gradient(135deg, #0ea37a 0%, #065f46 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      margin-bottom: 0;
      font-weight: 800;
      font-size: 2rem;
    }
    
    .emoji {
      -webkit-background-clip: unset;
      -webkit-text-fill-color: initial;
      background: none;
    }

    .detail-grid { display: grid; gap: 1.5rem; }

    /* Glassmorphic & Modern Cards */
    .glass-card { 
      background: rgba(255, 255, 255, 0.95);
      border: 1px solid rgba(226, 232, 240, 0.8);
      border-radius: 16px; 
      padding: 1.5rem 1.8rem;
      box-shadow: 0 4px 15px rgba(0,0,0,0.03);
      backdrop-filter: blur(10px);
      transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1), box-shadow 0.3s ease;
    }
    .glass-card:hover {
      transform: translateY(-3px);
      box-shadow: 0 12px 25px rgba(0,0,0,0.08);
    }

    .card-title { 
      margin-bottom: 1.25rem; 
      font-size: 1.15rem; 
      color: #334155; 
      font-weight: 700;
      display: flex;
      align-items: center;
      gap: 8px;
    }
    
    .divider { height: 1px; background: rgba(0,0,0,0.06); margin: 1rem 0; width: 100%; }
    
    /* Rows and Layouts */
    .flex-between { display: flex; justify-content: space-between; align-items: center; }
    .flex { display: flex; }
    
    .total-row { 
      border-top: 1px dashed rgba(0,0,0,0.1); 
      margin-top: 1.25rem; 
      padding-top: 1rem; 
      font-size: 1.1rem; 
      color: #475569;
    }
    .total-price { 
      color: #0ea37a; 
      font-weight: 900; 
      font-size: 1.4rem;
    }
    
    .product-name {
      font-size: 1.15rem;
      color: #1e293b;
    }

    .customer-name {
      font-weight: 700;
      color: #1e293b;
      font-size: 1.1rem;
      margin-bottom: 0.5rem;
    }

    /* Icon Boxes */
    .icon-box {
      width: 40px;
      height: 40px;
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 1.2rem;
    }
    .info-box { background: #e0f2fe; color: #0284c7; }
    .warning-box { background: #fef3c7; color: #d97706; }

    /* Badges */
    .badge {
      padding: 6px 14px;
      border-radius: 9999px;
      font-weight: 600;
      font-size: 0.8rem;
      text-transform: uppercase;
      letter-spacing: 0.05em;
      display: flex;
      align-items: center;
      gap: 6px;
      box-shadow: 0 2px 5px rgba(0,0,0,0.05);
    }
    .badge-success { background: #dcfce7; color: #166534; border: 1px solid #bbf7d0; }
    .badge-danger { background: #fee2e2; color: #991b1b; border: 1px solid #fecaca; }
    .badge-info { background: #e0f2fe; color: #075985; border: 1px solid #bae6fd; }
    .badge-warning { background: #fef9c3; color: #854d0e; border: 1px solid #fef08a; }
    .badge-neutral { background: #f1f5f9; color: #475569; border: 1px solid #e2e8f0; }

    .pulse-dot {
      width: 8px;
      height: 8px;
      background: currentColor;
      border-radius: 50%;
      animation: pulse 2s infinite;
    }

    /* Contact Badges */
    .contact-badges {
      display: flex;
      flex-direction: column;
      gap: 6px;
      align-items: flex-end;
    }
    .contact-badge {
      background: #f8fafc;
      border: 1px solid #cbd5e1;
      padding: 4px 10px;
      border-radius: 6px;
      font-size: 0.85rem;
      color: #475569;
    }

    /* Action Bar */
    .action-bar {
      display: flex;
      gap: 1rem;
      margin-top: 1rem;
    }
    .action-btn {
      flex: 1;
      padding: 0.8rem;
      font-size: 1.05rem;
      border-radius: 12px;
      font-weight: 600;
      justify-content: center;
      display: flex;
      align-items: center;
      gap: 8px;
      transition: all 0.3s ease;
    }
    .edit-btn {
      background: #f1f5f9;
      color: #334155;
      border: 1px solid #cbd5e1;
    }
    .edit-btn:hover { background: #e2e8f0; transform: translateY(-2px); }
    
    .track-btn {
      background: linear-gradient(135deg, #0ea37a 0%, #059669 100%);
      color: white;
      border: none;
      box-shadow: 0 4px 12px rgba(14, 163, 122, 0.3);
    }
    .track-btn:hover {
      box-shadow: 0 6px 15px rgba(14, 163, 122, 0.4);
      transform: translateY(-2px);
    }

    /* Animations */
    @keyframes pulse {
      0% { transform: scale(0.95); opacity: 0.5; }
      50% { transform: scale(1.1); opacity: 1; }
      100% { transform: scale(0.95); opacity: 0.5; }
    }
    .fade-in { animation: fadeIn 0.5s ease-out; }
    @keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }

    .slide-down { animation: slideDown 0.4s ease-out; }
    @keyframes slideDown { from { opacity: 0; transform: translateY(-10px); } to { opacity: 1; transform: translateY(0); } }

    .float-up { animation: floatUp 0.6s ease-out backwards 0.2s; }
    @keyframes floatUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }
  `,
})
export class OrderDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly orderService = inject(OrderService);
  private readonly msg = inject(MessageService);

  readonly order = signal<ProductOrder | null>(null);
  readonly loading = signal(true);

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadOrder(+id);
    } else {
      this.router.navigate(['/shop/my-orders']);
    }
  }

  loadOrder(id: number): void {
    this.loading.set(true);
    this.orderService.getById(id).subscribe({
      next: (o) => {
        this.order.set(o);
        this.loading.set(false);
      },
      error: () => {
        this.msg.error('Order not found');
        this.router.navigate(['/shop/my-orders']);
      },
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

  getPaymentMethodName(type: string | undefined): string {
    if (!type) return 'Unknown';
    if (type === 'COD') return 'Cash On Delivery';
    if (type === 'CARD' || type === 'ONLINE') return 'Online Payment';
    return type;
  }

  editOrder(order: ProductOrder): void {
    this.router.navigate(['/shop/my-orders', order.id, 'edit']);
  }

  openMap(orderId: number): void {
    window.open(`/assets/shop-images/order-tracking-example.html?orderId=${orderId}`, '_blank');
  }

  goBack(): void {
    this.router.navigate(['/shop/my-orders']);
  }
}
