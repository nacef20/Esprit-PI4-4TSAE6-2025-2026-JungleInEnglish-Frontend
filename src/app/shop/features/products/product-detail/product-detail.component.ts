import { Component, inject, signal, OnInit } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ProductService } from '../../../services/product.service';
import { CartService } from '../../../services/cart.service';
import { AuthService } from '@shop/services/auth.service';
import { MessageService } from '../../../services/message.service';
import type { Product } from '@shop/shared/models/product.model';

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
          <div class="page-header slide-down" style="margin-bottom: 2rem;">
            <a routerLink="/shop/products" class="btn btn-outline" style="border: 1px solid #cbd5e1; background: #f8fafc; color: #475569; padding: 0.5rem 1rem; border-radius: 8px;">
              <span style="margin-right: 5px;">←</span> Back to Catalog
            </a>
          </div>

          <div class="detail fade-in">
            <!-- Image Card -->
            <div class="detail__img glass-card">
              <img [src]="productService.getImageUrl(p.image!)" [alt]="p.title">
            </div>

            <!-- Info Section -->
            <div class="detail__info glass-card">
              <span class="badge badge-primary float-up" style="animation-delay: 0.1s;">{{ p.category }}</span>
              <h1 class="gradient-text mt-1 float-up" style="animation-delay: 0.2s;">{{ p.title }}</h1>
              
              <div class="flex gap-2 mt-2 float-up" style="align-items: center; padding: 1rem 0; border-bottom: 1px dashed rgba(0,0,0,0.1); animation-delay: 0.3s;">
                <span class="price-main">{{ p.discountPrice | number:'1.2-2' }} TND</span>
                @if (p.discount > 0) {
                  <span class="price--old">{{ p.price | number:'1.2-2' }} TND</span>
                  <span class="discount-badge discount-pulse">-{{ p.discount }}% OFF</span>
                }
              </div>

              <p class="detail__desc mt-2 float-up" style="animation-delay: 0.4s;">{{ p.description || 'No description available for this product.' }}</p>
              
              <div class="stock-status mt-2 float-up" style="animation-delay: 0.5s;">
                <span class="status-dot" [class.bg-success]="p.stock > 0" [class.bg-danger]="p.stock <= 0"></span>
                <span [class.text-success]="p.stock > 0" [class.text-danger]="p.stock <= 0" style="font-weight: 600;">
                  {{ p.stock > 0 ? 'In stock (' + p.stock + ' units available)' : 'Out of stock' }}
                </span>
              </div>

              <div class="action-bar float-up" style="margin-top: 2rem; animation-delay: 0.6s;">
                @if (auth.isLoggedIn()) {
                  @if (p.stock > 0) {
                    <button class="btn btn-primary action-btn buy-btn" (click)="addToCart(p)">
                      🛒 Add to Cart
                    </button>
                  } @else {
                    <button class="btn btn-secondary action-btn" disabled style="opacity: 0.6; width: 100%;">Out of stock</button>
                  }
                } @else {
                  <button class="btn btn-outline action-btn" routerLink="/auth/login" style="width: 100%;">
                    🔒 Login to Purchase
                  </button>
                }
              </div>
            </div>
          </div>
        }
      }
    </div>
  `,
  styles: `
    :host { 
      display: block; 
      padding: 2rem 0 4rem; 
      font-family: 'Inter', sans-serif;
    }
    
    .container { max-width: 1200px; margin: 0 auto; padding: 0 1.5rem; }

    .detail { 
      display: grid; 
      grid-template-columns: 1.1fr 1fr; 
      gap: 2.5rem; 
      margin-top: 1rem; 
      align-items: stretch;
    }

    /* Glassmorphic & Modern Cards */
    .glass-card { 
      background: rgba(255, 255, 255, 0.85);
      border: 1px solid rgba(255, 255, 255, 0.6);
      border-radius: 24px; 
      padding: 2.5rem;
      box-shadow: 0 20px 40px -10px rgba(0,0,0,0.05), inset 0 2px 4px rgba(255,255,255,0.8);
      backdrop-filter: blur(20px);
      transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
    }
    
    .detail__img {
      min-height: 400px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: radial-gradient(circle at center, #ffffff 0%, #f1f5f9 100%);
      overflow: hidden;
      padding: 2rem;
    }

    .detail__img:hover { transform: translateY(-5px); box-shadow: 0 30px 50px -15px rgba(0,0,0,0.1); }

    .detail__img img { 
      width: 100%; 
      height: 100%; 
      max-height: 450px;
      object-fit: contain; 
      filter: drop-shadow(0 15px 25px rgba(0,0,0,0.15));
      transition: transform 0.6s cubic-bezier(0.165, 0.84, 0.44, 1);
    }
    
    .detail__img:hover img { transform: scale(1.08); }

    .gradient-text {
      background: linear-gradient(135deg, #0f172a 0%, #0ea37a 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      font-weight: 900;
      font-size: 3rem;
      line-height: 1.2;
      margin-top: 0.5rem;
      margin-bottom: 0;
      text-transform: capitalize;
      letter-spacing: -0.02em;
    }

    .badge {
      padding: 8px 16px;
      border-radius: 9999px;
      font-weight: 800;
      font-size: 0.8rem;
      text-transform: uppercase;
      letter-spacing: 0.1em;
      display: inline-flex;
      background: #e0f2fe; color: #0284c7; border: 1px solid #bae6fd;
      box-shadow: 0 4px 6px -1px rgba(2, 132, 199, 0.1);
    }

    .price-main {
      font-size: 2.5rem;
      font-weight: 900;
      color: #0ea37a;
      letter-spacing: -0.02em;
    }

    .price--old { text-decoration: line-through; color: #94a3b8; font-size: 1.3rem; font-weight: 600; margin-left: 0.5rem; }
    
    .discount-badge {
      background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
      color: white;
      padding: 6px 12px;
      border-radius: 10px;
      font-weight: 800;
      font-size: 0.9rem;
      border: none;
      box-shadow: 0 4px 10px rgba(239, 68, 68, 0.3);
      margin-left: 0.5rem;
    }

    .detail__desc { 
      color: #475569; 
      line-height: 1.8; 
      font-size: 1.15rem;
      margin-top: 2rem;
      padding-top: 1.5rem;
      border-top: 1px solid rgba(0,0,0,0.04);
    }

    .stock-status {
      display: inline-flex;
      align-items: center;
      gap: 10px;
      padding: 10px 18px;
      background: rgba(255, 255, 255, 0.6);
      border-radius: 14px;
      margin-top: 1.5rem;
      backdrop-filter: blur(5px);
      box-shadow: 0 2px 10px rgba(0,0,0,0.02);
    }

    .status-dot { width: 10px; height: 10px; border-radius: 50%; }
    .bg-success { background: #10b981; box-shadow: 0 0 0 3px #d1fae5; }
    .bg-danger { background: #ef4444; box-shadow: 0 0 0 3px #fee2e2; }
    .text-success { color: #059669; }
    .text-danger { color: #dc2626; }

    .action-btn {
      width: 100%;
      padding: 1.2rem;
      font-size: 1.2rem;
      border-radius: 14px;
      font-weight: 700;
      justify-content: center;
      display: flex;
      align-items: center;
      gap: 10px;
      transition: all 0.3s ease;
      cursor: pointer;
    }
    
    .buy-btn {
      background: linear-gradient(135deg, #0ea37a 0%, #059669 100%);
      color: white;
      border: none;
      box-shadow: 0 8px 20px rgba(14, 163, 122, 0.3);
    }
    .buy-btn:hover {
      box-shadow: 0 12px 25px rgba(14, 163, 122, 0.4);
      transform: translateY(-3px);
    }

    .btn-outline:hover { background: #e2e8f0; }

    /* Animations */
    .fade-in { animation: fadeIn 0.6s ease-out; }
    @keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }

    .slide-down { animation: slideDown 0.4s ease-out; }
    @keyframes slideDown { from { opacity: 0; transform: translateY(-10px); } to { opacity: 1; transform: translateY(0); } }

    .float-up { animation: floatUp 0.6s ease-out backwards; }
    @keyframes floatUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }

    .discount-pulse { animation: pulse 2s infinite; }
    @keyframes pulse { 0% { transform: scale(1); } 50% { transform: scale(1.05); } 100% { transform: scale(1); } }

    @media (max-width: 900px) { 
      .detail { grid-template-columns: 1fr; gap: 2rem; } 
      .detail__img { height: 350px; }
      .gradient-text { font-size: 2rem; }
    }
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
    if (!this.auth.isLoggedIn()) return;
    this.cartService.add(p.id).subscribe({
      next: () => this.msg.success(`"${p.title}" added to cart`),
    });
  }
}
