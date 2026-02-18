import { Component, inject, signal, OnInit } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { Router } from '@angular/router';
import { OrderService } from '../../../core/services/order.service';
import { AuthService } from '../../../core/services/auth.service';
import { MessageService } from '../../../core/services/message.service';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import type { ProductOrder } from '../../../shared/models/order.model';

@Component({
  selector: 'app-order-list',
  standalone: true,
  imports: [DecimalPipe, ConfirmDialogComponent],
  template: `
    <div class="container">
      <div class="page-header">
        <h1>📦 Mes Commandes</h1>
      </div>

      @if (loading()) {
        <div class="loading-center"><div class="spinner"></div></div>
      } @else if (orders().length === 0) {
        <div class="empty-state">
          <div class="empty-state__icon">📦</div>
          <div class="empty-state__title">Aucune commande</div>
          <p class="text-muted">Vous n'avez pas encore passé de commande</p>
        </div>
      } @else {
        <div class="table-wrap">
          <table>
            <thead><tr><th>Produit</th><th>Date</th><th>Quantité</th><th>Prix</th><th>Statut</th><th>Actions</th></tr></thead>
            <tbody>
              @for (o of orders(); track o.id) {
                <tr>
                  <td>{{ o.product?.title }}</td>
                  <td>{{ o.orderDate }}</td>
                  <td>{{ o.quantity }}</td>
                  <td class="price">{{ o.price | number:'1.2-2' }} TND</td>
                  <td><span class="badge" [class]="getStatusClass(o.status)">{{ o.status }}</span></td>
                  <td class="flex gap-1">
                    <button class="btn btn-sm btn-info" (click)="viewOrder(o)" title="Voir détails">👁️</button>
                    <button class="btn btn-sm btn-secondary" (click)="editOrder(o)" title="Modifier">✏️</button>
                    <button class="btn btn-sm btn-danger" (click)="confirmDelete(o)" title="Supprimer">🗑️</button>
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      }

      @if (deletingOrder(); as order) {
        <app-confirm-dialog title="Supprimer la commande" [message]="'Supprimer la commande ' + order.orderId + ' ?'" (confirm)="doDelete(order)" (cancel)="deletingOrder.set(null)" />
      }
    </div>
  `,
  styles: `:host { display: block; padding-bottom: 3rem; } code { font-size: .8rem; background: var(--color-surface-elevated); padding: .15rem .4rem; border-radius: var(--radius-sm); }`,
})
export class OrderListComponent implements OnInit {
  private readonly orderService = inject(OrderService);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly msg = inject(MessageService);

  readonly orders = signal<ProductOrder[]>([]);
  readonly loading = signal(true);
  readonly deletingOrder = signal<ProductOrder | null>(null);

  ngOnInit(): void {
    this.refresh();
  }

  refresh(): void {
    const user = this.auth.currentUser();
    if (!user) { this.loading.set(false); return; }
    this.loading.set(true);
    this.orderService.getByUser(user.id).subscribe({
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
    this.router.navigate(['/my-orders', order.id]);
  }

  editOrder(order: ProductOrder): void {
    this.router.navigate(['/my-orders', order.id, 'edit']);
  }

  confirmDelete(order: ProductOrder): void {
    this.deletingOrder.set(order);
  }

  doDelete(order: ProductOrder): void {
    this.orderService.delete(order.id).subscribe({
      next: () => {
        this.msg.success('Commande supprimée');
        this.deletingOrder.set(null);
        this.refresh();
      },
      error: () => this.msg.error('Erreur lors de la suppression'),
    });
  }
}
