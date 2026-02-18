import { Component, inject, signal, OnInit } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ProductService } from '../../../core/services/product.service';
import { CartService } from '../../../core/services/cart.service';
import { AuthService } from '../../../core/services/auth.service';
import { MessageService } from '../../../core/services/message.service';
import type { Product } from '../../../shared/models/product.model';

@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [RouterLink, DecimalPipe],
  template: `
    <div class="container">
      @if (loading()) {
        <div class="loading-center"><div class="spinner"></div></div>
      } @else {
        @if (product(); as p) {
        <div class="detail">
          <div class="detail__img">
            <img [src]="productService.getImageUrl(p.image!)" [alt]="p.title">
          </div>
          <div class="detail__info">
            <a routerLink="/products" class="btn btn-sm btn-secondary mb-2">← Retour</a>
            <h1>{{ p.title }}</h1>
            <span class="badge badge-primary">{{ p.category }}</span>
            <p class="detail__desc mt-2">{{ p.description || 'Pas de description disponible.' }}</p>
            <div class="flex gap-2 mt-2" style="align-items: baseline;">
              <span class="price" style="font-size:1.5rem;">{{ p.discountPrice | number:'1.2-2' }} TND</span>
              @if (p.discount > 0) {
                <span class="price--old">{{ p.price | number:'1.2-2' }} TND</span>
                <span class="discount-badge">-{{ p.discount }}%</span>
              }
            </div>
            <p class="mt-1" [class.text-success]="p.stock > 0" [class.text-danger]="p.stock <= 0">
              {{ p.stock > 0 ? 'En stock (' + p.stock + ' disponibles)' : 'Rupture de stock' }}
            </p>
            @if (auth.isLoggedIn() && p.stock > 0) {
              <button class="btn btn-primary btn-lg mt-3" (click)="addToCart(p)">🛒 Ajouter au panier</button>
            }
          </div>
        </div>
        }
      }
    </div>
  `,
  styles: `
    :host { display: block; padding-bottom: 3rem; }
    .detail { display: grid; grid-template-columns: 1fr 1fr; gap: 3rem; margin-top: 2rem; }
    .detail__img { background: var(--color-surface); border-radius: var(--radius-lg); height: 400px; display: flex; align-items: center; justify-content: center; font-size: 6rem; border: 1px solid var(--color-border); overflow: hidden; }
    .detail__img img { width: 100%; height: 100%; object-fit: contain; }
    .detail__info h1 { font-size: 1.75rem; font-weight: 700; }
    .detail__desc { color: var(--color-text-muted); line-height: 1.7; }
    @media (max-width: 768px) { .detail { grid-template-columns: 1fr; } }
  `,
})
export class ProductDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  protected readonly productService = inject(ProductService);
  private readonly cartService = inject(CartService);
  readonly auth = inject(AuthService);
  private readonly msg = inject(MessageService);

  readonly product = signal<Product | null>(null);
  readonly loading = signal(true);

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.productService.getById(id).subscribe({
      next: p => { this.product.set(p); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
  }

  addToCart(p: Product): void {
    const user = this.auth.currentUser();
    if (!user) return;
    this.cartService.add(p.id, user.id).subscribe({
      next: () => this.msg.success(`"${p.title}" ajouté au panier`),
    });
  }
}
