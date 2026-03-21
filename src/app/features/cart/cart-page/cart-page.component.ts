import { Component, inject, signal, OnInit, computed } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CartService } from '../../../core/services/cart.service';
import { ProductService } from '../../../core/services/product.service';
import { AuthService } from '../../../core/services/auth.service';
import { MessageService } from '../../../core/services/message.service';
import type { Cart } from '../../../shared/models/cart.model';

@Component({
  selector: 'app-cart-page',
  standalone: true,
  imports: [RouterLink, DecimalPipe],
  template: `
    <div class="container">
      <div class="page-header">
        <h1>🛒 Mon Panier</h1>
      </div>

      @if (loading()) {
        <div class="loading-center"><div class="spinner"></div></div>
      } @else if (items().length === 0) {
        <div class="empty-state">
          <div class="empty-state__icon">🛒</div>
          <div class="empty-state__title">Votre panier est vide</div>
          <p class="text-muted mb-3">Explorez nos produits et ajoutez-en à votre panier</p>
          <a routerLink="/products" class="btn btn-primary">Voir les produits</a>
        </div>
      } @else {
        <div class="cart-layout">
          <div class="table-wrap">
            <table>
              <thead><tr><th>Produit</th><th>Prix</th><th>Quantité</th><th>Total</th><th></th></tr></thead>
              <tbody>
                @for (item of items(); track item.id) {
                  <tr>
                    <td>
                      <div class="flex gap-1" style="align-items: center;">
                        <img [src]="productService.getImageUrl(item.product.image!)" [alt]="item.product.title" class="cart-thumb">
                        <strong>{{ item.product.title }}</strong>
                      </div>
                    </td>
                    <td>{{ item.product.discountPrice | number:'1.2-2' }} TND</td>
                    <td>
                      <div class="qty-controls">
                        <button class="btn btn-sm btn-secondary" (click)="updateQty(item.id, 'de')" [disabled]="item.quantity <= 1">−</button>
                        <span class="qty-val">{{ item.quantity }}</span>
                        <button class="btn btn-sm btn-secondary" (click)="updateQty(item.id, 'in')">+</button>
                      </div>
                    </td>
                    <td class="price">{{ (item.product.discountPrice ?? item.product.price) * item.quantity | number:'1.2-2' }} TND</td>
                    <td><button class="btn btn-sm btn-danger" (click)="remove(item.id)">✕</button></td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
          <div class="cart-summary">
            <h3>Résumé</h3>
            <div class="summary-row"><span>Articles</span><span>{{ items().length }}</span></div>
            <div class="summary-row total"><span>Total</span><span class="price">{{ total() | number:'1.2-2' }} TND</span></div>
            @if (hasInsufficientStock()) {
              <button class="btn btn-secondary btn-lg" disabled style="width:100%;justify-content:center;margin-top:1rem;">❌ Stock insuffisant</button>
            } @else {
              <a routerLink="/checkout" class="btn btn-primary btn-lg" style="width:100%;justify-content:center;margin-top:1rem;">Passer la commande</a>
            }
          </div>
        </div>
      }
    </div>
  `,
  styles: `
    :host { display: block; padding-bottom: 3rem; }
    .cart-layout { display: grid; grid-template-columns: 1fr 320px; gap: 2rem; align-items: start; }
    .cart-summary { background: var(--color-surface); border: 1px solid var(--color-border); border-radius: var(--radius-lg); padding: 1.5rem; }
    .cart-summary h3 { font-size: 1.1rem; font-weight: 700; margin-bottom: 1rem; }
    .summary-row { display: flex; justify-content: space-between; padding: .5rem 0; color: var(--color-text-muted); font-size: .9rem; border-bottom: 1px solid var(--color-border); }
    .summary-row.total { font-weight: 700; color: var(--color-text); font-size: 1.1rem; border: none; margin-top: .5rem; }
    .qty-controls { display: flex; align-items: center; gap: .5rem; }
    .qty-val { font-weight: 600; min-width: 24px; text-align: center; }
    .cart-thumb { width: 48px; height: 48px; border-radius: var(--radius-sm); object-fit: cover; background: var(--color-surface-elevated); }
    @media (max-width: 768px) { .cart-layout { grid-template-columns: 1fr; } }
  `,
})
export class CartPageComponent implements OnInit {
  private readonly cartService = inject(CartService);
  protected readonly productService = inject(ProductService);
  private readonly auth = inject(AuthService);
  private readonly msg = inject(MessageService);

  readonly items = signal<Cart[]>([]);
  readonly loading = signal(true);
  readonly total = computed(() => this.items().reduce((sum, i) => sum + (i.product.discountPrice ?? i.product.price) * i.quantity, 0));
  readonly hasInsufficientStock = computed(() => this.items().some(i => i.quantity > i.product.stock));

  ngOnInit(): void { this.load(); }

  load(): void {
    if (!this.auth.isLoggedIn()) {
      this.loading.set(false);
      return;
    }
    this.cartService.getMyCart().subscribe({
      next: items => { this.items.set(items); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
  }

  updateQty(id: number, action: 'de' | 'in'): void {
    this.cartService.updateQuantity(id, action).subscribe(() => this.load());
  }

  remove(id: number): void {
    this.cartService.remove(id).subscribe(() => { this.msg.success('Article retiré'); this.load(); });
  }
}
