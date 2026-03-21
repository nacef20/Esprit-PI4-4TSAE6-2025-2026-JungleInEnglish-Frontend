import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/products', pathMatch: 'full' },

  // Auth
  { path: 'login', loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent) },
  { path: 'login', loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent) },

  // Public — Products
  { path: 'products', loadComponent: () => import('./features/products/product-list/product-list.component').then(m => m.ProductListComponent) },
  { path: 'products/create', canActivate: [authGuard], loadComponent: () => import('./features/products/product-form/product-form.component').then(m => m.ProductFormComponent) },
  { path: 'products/:id/edit', canActivate: [authGuard], loadComponent: () => import('./features/products/product-form/product-form.component').then(m => m.ProductFormComponent) },
  { path: 'products/:id', loadComponent: () => import('./features/products/product-detail/product-detail.component').then(m => m.ProductDetailComponent) },

  // Auth-protected
  { path: 'cart', canActivate: [authGuard], loadComponent: () => import('./features/cart/cart-page/cart-page.component').then(m => m.CartPageComponent) },
  { path: 'checkout', canActivate: [authGuard], loadComponent: () => import('./features/orders/checkout/checkout.component').then(m => m.CheckoutComponent) },
  { path: 'my-orders', canActivate: [authGuard], loadComponent: () => import('./features/orders/order-list/order-list.component').then(m => m.OrderListComponent) },
  { path: 'my-orders/:id', canActivate: [authGuard], loadComponent: () => import('./features/orders/order-detail/order-detail.component').then(m => m.OrderDetailComponent) },
  { path: 'my-orders/:id/edit', canActivate: [authGuard], loadComponent: () => import('./features/orders/order-form/order-form.component').then(m => m.OrderFormComponent) },

  // Categories (Open to all actors)
  { path: 'categories', loadComponent: () => import('./features/categories/category-list/category-list.component').then(m => m.CategoryListComponent) },
  { path: 'categories/create', loadComponent: () => import('./features/categories/category-form/category-form.component').then(m => m.CategoryFormComponent) },
  { path: 'categories/:id/edit', loadComponent: () => import('./features/categories/category-form/category-form.component').then(m => m.CategoryFormComponent) },

  // Admin
  {
    path: 'admin', canActivate: [authGuard, adminGuard], children: [
      { path: '', loadComponent: () => import('./features/admin/admin-dashboard/admin-dashboard.component').then(m => m.AdminDashboardComponent) },
      { path: 'products', loadComponent: () => import('./features/admin/admin-product-list/admin-product-list.component').then(m => m.AdminProductListComponent) },
      { path: 'orders', loadComponent: () => import('./features/orders/order-admin/order-admin.component').then(m => m.OrderAdminComponent) },
      { path: 'inventory', loadComponent: () => import('./features/admin/inventory-dashboard/inventory-dashboard.component').then(m => m.InventoryDashboardComponent) },
    ]
  },

  { path: '**', redirectTo: '/products' },
];
