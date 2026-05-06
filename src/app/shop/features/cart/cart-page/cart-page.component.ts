import { Component, inject, signal, OnInit, computed } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CartService } from '../../../services/cart.service';
import { ProductService } from '../../../services/product.service';
import { AuthService } from '@shop/services/auth.service';
import { MessageService } from '../../../services/message.service';
import type { Cart } from '@shop/shared/models/cart.model';

@Component({
  selector: 'app-cart-page',
  standalone: true,
  imports: [RouterLink, DecimalPipe],
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
                <a routerLink="/shop/my-orders" class="nav-link">My Orders</a>
                <a routerLink="/admin/shop" class="nav-link">Back Office</a>
            </div>
            <div class="navbar-right">
                @if (auth.isLoggedIn()) {
                    <a routerLink="/shop/cart" class="nav-link nav-link--cart nav-link--active">🛒 Cart</a>
                } @else {
                    <a routerLink="/auth/login" class="nav-link">Login</a>
                }
            </div>
        </div>
    </nav>

    <div class="page-hero">
        <div class="container hero-content">
            <div class="hero-text">
                <p class="hero-eyebrow">Checkout Process</p>
                <h1>My Shopping Cart</h1>
                <p class="hero-desc">You have {{ items().length }} item(s) in your cart</p>
            </div>
            <a routerLink="/shop/products" class="btn btn-hero-outline">← Continue Shopping</a>
        </div>
    </div>

    <div class="container main-content-wrap">
      @if (loading()) {
        <div class="loading-wrap">
            <div class="spinner"></div>
            <p>Gathering your items...</p>
        </div>
      } @else if (items().length === 0) {
        <div class="empty-state">
          <div class="empty-state__icon">🛒</div>
          <h3>Your cart is empty</h3>
          <p>It looks like you haven't added anything to your cart yet. Explore our catalogs to find amazing books!</p>
          <a routerLink="/shop/products" class="btn btn-primary mt-2">Start Exploring</a>
        </div>
      } @else {
        <div class="cart-layout">
          <div class="cart-items-card">
            <div class="table-responsive">
                <table class="premium-table">
                <thead>
                    <tr>
                        <th>Product</th>
                        <th>Price</th>
                        <th>Quantity</th>
                        <th class="text-right">Total</th>
                        <th></th>
                    </tr>
                </thead>
                <tbody>
                    @for (item of items(); track item.id) {
                    <tr>
                        <td>
                        <div class="product-cell">
                            <img [src]="productService.getImageUrl(item.product.image!)" [alt]="item.product.title" class="cart-thumb">
                            <div class="product-info">
                                <span class="product-name">{{ item.product.title }}</span>
                                <span class="product-stock" [class.low]="item.product.stock < 5">
                                    {{ item.product.stock > 0 ? (item.product.stock + ' in stock') : 'Out of stock' }}
                                </span>
                            </div>
                        </div>
                        </td>
                        <td class="unit-price">{{ item.product.discountPrice | number:'1.2-2' }} TND</td>
                        <td>
                        <div class="qty-stepper" [class.error]="item.quantity > item.product.stock">
                            <button class="stepper-btn" (click)="updateQty(item.id, 'de')" [disabled]="item.quantity <= 1">−</button>
                            <span class="stepper-val">{{ item.quantity }}</span>
                            <button class="stepper-btn" (click)="updateQty(item.id, 'in')" [disabled]="item.quantity >= item.product.stock">+</button>
                        </div>
                        </td>
                        <td class="price-total text-right">{{ (item.product.discountPrice ?? item.product.price) * item.quantity | number:'1.2-2' }} TND</td>
                        <td class="text-center">
                            <button class="btn-remove" (click)="remove(item.id)" title="Remove item">✕</button>
                        </td>
                    </tr>
                    }
                </tbody>
                </table>
            </div>
          </div>

          <div class="summary-sidebar">
            <div class="summary-card">
                <h3>Order Summary</h3>
                <div class="summary-details">
                    <div class="summary-row">
                        <span>Subtotal</span>
                        <span>{{ total() | number:'1.2-2' }} TND</span>
                    </div>
                    <div class="summary-row">
                        <span>Shipping</span>
                        <span class="free-badge">FREE</span>
                    </div>
                </div>
                <div class="summary-total">
                    <div class="summary-row">
                        <span>Total</span>
                        <span class="total-price">{{ total() | number:'1.2-2' }} TND</span>
                    </div>
                </div>

                @if (hasInsufficientStock()) {
                <div class="alert alert-danger">
                    ⚠️ Some items have insufficient stock. Please adjust quantities.
                </div>
                <button class="btn btn-primary btn-block btn-lg" disabled>Checkout Unavailable</button>
                } @else {
                <a routerLink="/shop/checkout" class="btn btn-primary btn-block btn-lg">Proceed to Checkout</a>
                }

                <div class="trust-badges">
                    <div class="trust-item">🛡️ Secure Payment</div>
                    <div class="trust-item">🚚 Fast Delivery</div>
                </div>
            </div>
          </div>
        </div>
      }
    </div>
  `,
  styles: `
    :host { 
        display: block; 
        min-height: 100vh;
        background: #f8fafc;
        padding-bottom: 5rem;
        font-family: 'Inter', sans-serif;
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
    .nav-link:hover { color: #0ea37a; background: rgba(14, 163, 122, 0.03); }
    .nav-link--active { color: #0ea37a; border-bottom-color: #0ea37a; font-weight: 700; }
    .nav-link--cart { color: #0ea37a; font-weight: 600; }

    /* --- Page Hero --- */
    .page-hero {
        background: linear-gradient(135deg, #0f172a 0%, #134e35 100%);
        padding: 4rem 0;
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

    .hero-text h1 { font-size: 2.5rem; font-weight: 800; margin: 0 0 0.5rem; letter-spacing: -0.02em; }
    .hero-desc { color: rgba(255,255,255,0.8); font-size: 1.1rem; margin: 0; }

    .btn-hero-outline {
        padding: 0.75rem 1.75rem;
        border: 1px solid rgba(255,255,255,0.3);
        border-radius: 99px;
        color: white;
        text-decoration: none;
        font-weight: 600;
        font-size: 0.95rem;
        transition: all 0.2s;
    }
    .btn-hero-outline:hover { background: rgba(255,255,255,0.1); border-color: white; }

    /* --- Cart Layout --- */
    .main-content-wrap { margin-top: -3rem; position: relative; z-index: 5; }

    .cart-layout { display: grid; grid-template-columns: 1fr 380px; gap: 2rem; align-items: start; }

    .cart-items-card {
        background: white;
        border-radius: 24px;
        box-shadow: 0 10px 40px rgba(0,0,0,0.08);
        border: 1px solid rgba(226, 232, 240, 0.8);
        overflow: hidden;
    }

    .table-responsive { overflow-x: auto; }
    .premium-table { width: 100%; border-collapse: collapse; min-width: 600px; }
    .premium-table th {
        background: #f8fafc;
        padding: 1.25rem 1.5rem;
        font-size: 0.75rem;
        font-weight: 700;
        text-transform: uppercase;
        letter-spacing: 0.05em;
        color: #64748b;
        text-align: left;
    }
    .premium-table td { padding: 1.5rem; border-bottom: 1px solid #f1f5f9; vertical-align: middle; }

    .product-cell { display: flex; align-items: center; gap: 1.25rem; }
    .cart-thumb {
        width: 80px; height: 100px;
        object-fit: cover;
        border-radius: 12px;
        background: #f1f5f9;
        box-shadow: 0 4px 12px rgba(0,0,0,0.05);
    }
    .product-info { display: flex; flex-direction: column; gap: 0.25rem; }
    .product-name { font-weight: 700; color: #1e293b; font-size: 1.05rem; }
    .product-stock { font-size: 0.8rem; color: #10b981; font-weight: 600; }
    .product-stock.low { color: #f59e0b; }

    .unit-price { color: #64748b; font-weight: 500; font-size: 0.95rem; }
    .price-total { font-weight: 800; color: #0ea37a; font-size: 1.15rem; }

    /* --- Stepper --- */
    .qty-stepper {
        display: inline-flex;
        align-items: center;
        background: #f1f5f9;
        border: 1px solid #e2e8f0;
        border-radius: 12px;
        padding: 2px;
    }
    .qty-stepper.error { border-color: #ef4444; background: #fef2f2; }

    .stepper-btn {
        width: 32px; height: 32px;
        display: flex; align-items: center; justify-content: center;
        background: white; border: none; border-radius: 10px;
        font-size: 1.2rem; cursor: pointer; transition: all 0.2s;
        box-shadow: 0 2px 4px rgba(0,0,0,0.05);
    }
    .stepper-btn:hover:not(:disabled) { background: #f8fafc; color: #0ea37a; transform: scale(1.1); }
    .stepper-btn:disabled { opacity: 0.3; cursor: not-allowed; }
    .stepper-val { width: 36px; text-align: center; font-weight: 700; color: #1e293b; font-size: 0.95rem; }

    .btn-remove {
        background: #fef2f2; color: #ef4444; border: none;
        width: 34px; height: 34px; border-radius: 10px;
        cursor: pointer; transition: all 0.2s; font-size: 0.9rem;
    }
    .btn-remove:hover { background: #fee2e2; transform: scale(1.1) rotate(90deg); }

    /* --- Summary Card --- */
    .summary-card {
        background: white;
        padding: 2rem;
        border-radius: 24px;
        box-shadow: 0 20px 50px rgba(0,0,0,0.1);
        border: 1px solid rgba(226, 232, 240, 0.8);
        position: sticky;
        top: 80px;
    }
    .summary-card h3 { font-size: 1.4rem; font-weight: 800; color: #1e293b; margin-bottom: 1.75rem; border-bottom: 2px solid #f1f5f9; padding-bottom: 0.75rem; }
    .summary-details { margin-bottom: 1.5rem; }
    .summary-row { display: flex; justify-content: space-between; padding: 0.75rem 0; color: #64748b; font-weight: 500; }
    .free-badge { color: #10b981; font-weight: 800; font-size: 0.85rem; }
    
    .summary-total { border-top: 2px dashed #e2e8f0; margin-top: 0.5rem; padding-top: 1.25rem; margin-bottom: 2rem; }
    .total-price { color: #0ea37a; font-size: 1.6rem; font-weight: 900; }

    .btn-block { width: 100%; display: flex; justify-content: center; }
    .btn-lg { padding: 1rem; font-size: 1.05rem; font-weight: 700; border-radius: 16px; }
    .btn-primary { background: linear-gradient(135deg, #0ea37a 0%, #0b8a68 100%); color: white; border: none; box-shadow: 0 10px 20px rgba(14,163,122,0.2); transition: all 0.3s; cursor: pointer; text-decoration: none; }
    .btn-primary:hover:not(:disabled) { transform: translateY(-3px); box-shadow: 0 15px 30px rgba(14,163,122,0.3); filter: brightness(1.1); }

    .alert { padding: 1rem; border-radius: 12px; margin-bottom: 1.5rem; font-size: 0.85rem; font-weight: 600; line-height: 1.4; }
    .alert-danger { background: #fff1f2; color: #e11d48; border: 1px solid #fecdd3; }

    .trust-badges { margin-top: 1.75rem; display: flex; gap: 1rem; color: #94a3b8; font-size: 0.8rem; font-weight: 600; justify-content: center; }
    .trust-item { display: flex; align-items: center; gap: 0.25rem; }

    /* --- General Helpers --- */
    .text-right { text-align: right; }
    .text-center { text-align: center; }
    .loading-wrap { padding: 6rem 2rem; text-align: center; color: #64748b; }
    .spinner { 
        width: 44px; height: 44px; border: 4px solid #e2e8f0; border-top-color: #0ea37a; 
        border-radius: 50%; animation: spin 1s linear infinite; margin: 0 auto 1.5rem;
    }
    @keyframes spin { to { transform: rotate(360deg); } }

    .empty-state {
        background: white;
        padding: 6rem 2rem;
        border-radius: 24px;
        text-align: center;
        box-shadow: 0 20px 40px rgba(0,0,0,0.05);
        border: 1px solid #e2e8f0;
        max-width: 600px; margin: 4rem auto 0;
    }
    .empty-state__icon { font-size: 5rem; margin-bottom: 1.5rem; }
    .empty-state h3 { font-size: 1.75rem; font-weight: 800; color: #1e293b; margin-bottom: 0.75rem; }
    .empty-state p { color: #64748b; font-size: 1.1rem; line-height: 1.6; margin-bottom: 2rem; }

    @keyframes fadeIn { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }
    .cart-layout, .empty-state { animation: fadeIn 0.6s cubic-bezier(0.16, 1, 0.3, 1); }

    @media (max-width: 1024px) {
        .cart-layout { grid-template-columns: 1fr; }
        .summary-card { position: static; margin-top: 2rem; }
    }
  `,
})
export class CartPageComponent implements OnInit {
  private readonly cartService = inject(CartService);
  protected readonly productService = inject(ProductService);
  protected readonly auth = inject(AuthService);
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
    this.cartService.remove(id).subscribe(() => { this.msg.success('Item removed'); this.load(); });
  }
}
