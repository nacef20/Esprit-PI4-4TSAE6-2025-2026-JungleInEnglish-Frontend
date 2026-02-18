import { Component, inject, signal, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { DecimalPipe } from '@angular/common';
import { OrderService } from '../../../core/services/order.service';
import { MessageService } from '../../../core/services/message.service';
import type { ProductOrder } from '../../../shared/models/order.model';

@Component({
    selector: 'app-order-detail',
    standalone: true,
    imports: [DecimalPipe],
    template: `
    <div class="container container--narrow">
      <div class="page-header flex-between">
        <div>
          <h1>📄 Détails de la commande</h1>
          <p class="text-muted">#{{ order()?.orderId }}</p>
        </div>
        <button class="btn btn-secondary" (click)="goBack()">Retour</button>
      </div>

      @if (loading()) {
        <div class="loading-center"><div class="spinner"></div></div>
      } @else {
        @if (order(); as o) {
          <div class="detail-grid">
          <!-- Status & Info Card -->
          <div class="detail-card">
            <div class="flex-between mb-2">
              <span class="text-muted">Passée le {{ o.orderDate }}</span>
              <span class="badge" [class]="getStatusClass(o.status)">{{ o.status }}</span>
            </div>
            <div class="divider"></div>
            <div class="flex-between mt-2">
              <span class="text-muted">Mode de paiement:</span>
              <strong>{{ o.paymentType }}</strong>
            </div>
          </div>

          <!-- Product Card -->
          <div class="detail-card">
            <h3>Produit</h3>
            <div class="product-info flex gap-2">
              <div class="product-info__content">
                <div class="flex-between">
                  <strong>{{ o.product?.title }}</strong>
                  <span>{{ o.price | number:'1.2-2' }} TND x {{ o.quantity }}</span>
                </div>
                <div class="flex-between mt-1 total-row">
                  <span>Total</span>
                  <span class="total-price">{{ (o.price * o.quantity) | number:'1.2-2' }} TND</span>
                </div>
              </div>
            </div>
          </div>

          <!-- Shipping Address -->
          <div class="detail-card">
            <h3>Adresse de livraison</h3>
            <div class="address-box">
              <p><strong>{{ o.orderAddress?.firstName }} {{ o.orderAddress?.lastName }}</strong></p>
              <p>{{ o.orderAddress?.address }}</p>
              <p>{{ o.orderAddress?.city }}, {{ o.orderAddress?.state }} {{ o.orderAddress?.pincode }}</p>
              <p class="mt-1">📞 {{ o.orderAddress?.mobileNo }}</p>
              <p>✉️ {{ o.orderAddress?.email }}</p>
            </div>
          </div>

          <div class="flex gap-1 mt-2">
            <button class="btn btn-primary" (click)="editOrder(o)">✏️ Modifier</button>
          </div>
        </div>
      }
    }
  </div>
  `,
    styles: `
    :host { display: block; padding-bottom: 3rem; }
    .container--narrow { max-width: 600px; }
    .detail-grid { display: grid; gap: 1.5rem; }
    .detail-card { background: var(--color-surface); border: 1px solid var(--color-border); border-radius: var(--radius-lg); padding: 1.5rem; }
    .detail-card h3 { margin-bottom: 1rem; font-size: 1.1rem; color: var(--color-primary); border-bottom: 1px solid var(--color-border); padding-bottom: .5rem; }
    .flex-between { display: flex; justify-content: space-between; align-items: center; }
    .divider { height: 1px; background: var(--color-border); margin: 0.5rem 0; }
    .total-row { border-top: 1px dashed var(--color-border); pt-1; margin-top: 1rem; padding-top: .5rem; font-size: 1.2rem; }
    .total-price { color: var(--color-secondary); font-weight: bold; }
    .address-box p { margin-bottom: 0.25rem; }
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
            this.router.navigate(['/my-orders']);
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
                this.msg.error('Commande introuvable');
                this.router.navigate(['/my-orders']);
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

    editOrder(order: ProductOrder): void {
        this.router.navigate(['/my-orders', order.id, 'edit']);
    }

    goBack(): void {
        this.router.navigate(['/my-orders']);
    }
}
