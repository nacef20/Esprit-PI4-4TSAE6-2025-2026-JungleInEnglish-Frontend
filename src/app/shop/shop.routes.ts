import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const SHOP_ROUTES: Routes = [
  { path: '', redirectTo: 'products', pathMatch: 'full' },

  // Public — Products
  { path: 'products', loadComponent: () => import('./features/products/product-list/product-list.component').then(m => m.ProductListComponent) },
  { path: 'products/create', canActivate: [authGuard], loadComponent: () => import('./features/products/product-form/product-form.component').then(m => m.ProductFormComponent) },
  { path: 'products/:id/edit', canActivate: [authGuard], loadComponent: () => import('./features/products/product-form/product-form.component').then(m => m.ProductFormComponent) },
  { path: 'products/:id', loadComponent: () => import('./features/products/product-detail/product-detail.component').then(m => m.ProductDetailComponent) },

  // Auth-protected
  { path: 'cart', canActivate: [authGuard], loadComponent: () => import('./features/cart/cart-page/cart-page.component').then(m => m.CartPageComponent) },
  { path: 'checkout', canActivate: [authGuard], loadComponent: () => import('./features/orders/checkout/checkout.component').then(m => m.CheckoutComponent) },
  { path: 'payment', canActivate: [authGuard], loadComponent: () => import('./features/orders/payment-page/payment-page.component').then(m => m.PaymentPageComponent) },
  { path: 'my-orders', canActivate: [authGuard], loadComponent: () => import('./features/orders/order-list/order-list.component').then(m => m.OrderListComponent) },
  { path: 'my-orders/:id', canActivate: [authGuard], loadComponent: () => import('./features/orders/order-detail/order-detail.component').then(m => m.OrderDetailComponent) },
  { path: 'my-orders/:id/edit', canActivate: [authGuard], loadComponent: () => import('./features/orders/order-form/order-form.component').then(m => m.OrderFormComponent) },

  // Categories
  { path: 'categories', loadComponent: () => import('./features/categories/category-list/category-list.component').then(m => m.CategoryListComponent) },
  { path: 'categories/create', canActivate: [authGuard], loadComponent: () => import('./features/categories/category-form/category-form.component').then(m => m.CategoryFormComponent) },
  { path: 'categories/:id/edit', canActivate: [authGuard], loadComponent: () => import('./features/categories/category-form/category-form.component').then(m => m.CategoryFormComponent) },
  
  // Auth
  { path: 'login', loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent) },
];
