import { Component, inject, signal, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AdminService } from '../../../services/admin.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [RouterLink],
  template: `
    <div class="admin-container">
      <div class="admin-header">
        <div>
          <h1>🏠 Shop Dashboard</h1>
          <p>Overview of your store's performance and inventory</p>
        </div>
      </div>

      @if (loading()) {
        <div class="admin-loading">
          <div class="spinner"></div>
          <p>Loading dashboard data...</p>
        </div>
      } @else {
        <div class="admin-stats-grid">
          <div class="admin-stat-card">
            <div class="icon" style="background: linear-gradient(135deg, #2D5757 0%, #3d7070 100%); color: white;">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"/></svg>
            </div>
            <div class="content">
              <p>Total Products</p>
              <h3>{{ stats().totalProducts }}</h3>
            </div>
          </div>

          <div class="admin-stat-card">
            <div class="icon" style="background: linear-gradient(135deg, #4caf50 0%, #66bb6a 100%); color: white;">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/></svg>
            </div>
            <div class="content">
              <p>Categories</p>
              <h3>{{ stats().totalCategories }}</h3>
            </div>
          </div>

          <div class="admin-stat-card">
            <div class="icon" style="background: linear-gradient(135deg, #ff9800 0%, #ffb74d 100%); color: white;">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 17a2 2 0 11-4 0 2 2 0 014 0zM19 17a2 2 0 11-4 0 2 2 0 014 0z"/><path d="M13 16V6a1 1 0 00-1-1H4a1 1 0 00-1 1v10a1 1 0 001 1h1m8-1a1 1 0 011 1v2a1 1 0 01-1 1h-1m-4-7h8l2 3v5M7 9h1"/></svg>
            </div>
            <div class="content">
              <p>Total Orders</p>
              <h3>{{ stats().totalOrders }}</h3>
            </div>
          </div>
        </div>

        <h2 style="font-size: 1.25rem; color: #2D5757; font-weight: 700; margin: 0 0 1.5rem 0;">Quick Management Access</h2>
        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: 1.5rem;">
          <a routerLink="products" class="quick-link-card">
            <div class="card-icon">📦</div>
            <div class="card-title">Products</div>
            <p>Add, edit, and manage your products catalog</p>
          </a>
          <a routerLink="categories" class="quick-link-card">
            <div class="card-icon">📂</div>
            <div class="card-title">Categories</div>
            <p>Organize products into logical categories</p>
          </a>
          <a routerLink="orders" class="quick-link-card">
            <div class="card-icon">🚚</div>
            <div class="card-title">Orders</div>
            <p>Monitor and fulfill customer orders</p>
          </a>
          <a routerLink="inventory" class="quick-link-card quick-link-card--inventory">
            <div class="card-icon">📈</div>
            <div class="card-title">Inventory</div>
            <p>Track stock levels and restocking needs</p>
          </a>
        </div>
      }
    </div>
  `,
  styles: `
    :host { display: block; }
    
    .admin-stats-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
      gap: 1.5rem;
      margin-bottom: 2.5rem;
    }
    
    .admin-stat-card {
      background: white;
      border-radius: 12px;
      padding: 1.5rem;
      display: flex;
      align-items: center;
      gap: 1.25rem;
      box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.03);
      border: 1px solid #f1f5f9;
      transition: transform 0.2s, box-shadow 0.2s;
    }
    .admin-stat-card:hover {
      transform: translateY(-4px);
      box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.08), 0 4px 6px -2px rgba(0, 0, 0, 0.04);
    }
    
    .admin-stat-card .icon {
      width: 56px;
      height: 56px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
    }
    
    .admin-stat-card .content p {
      margin: 0;
      color: #64748b;
      font-size: 0.9rem;
      font-weight: 600;
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }
    
    .admin-stat-card .content h3 {
      margin: 0.25rem 0 0 0;
      color: #0f172a;
      font-size: 1.75rem;
      font-weight: 800;
    }
    
    .quick-link-card {
      background: white; border-radius: 12px; border: 1px solid #e2e8f0; padding: 1.5rem; text-align: center;
      text-decoration: none; color: inherit; transition: all 0.3s;
      &:hover { transform: translateY(-4px); box-shadow: 0 10px 20px rgba(0,0,0,0.05); border-color: #10b981; }
      .card-icon { font-size: 2.5rem; margin-bottom: 1rem; }
      .card-title { font-size: 1.15rem; font-weight: 700; color: #0f172a; margin-bottom: 0.5rem; }
      p { color: #64748b; font-size: 0.9rem; margin: 0; line-height: 1.4; }
      &--inventory:hover { border-color: #0ea5e9; }
    }
  `,
})
export class AdminDashboardComponent implements OnInit {
  private readonly adminService = inject(AdminService);
  readonly stats = signal({ totalProducts: 0, totalCategories: 0, totalOrders: 0 });
  readonly loading = signal(true);

  ngOnInit(): void {
    this.adminService.getDashboard().subscribe({
      next: s => { this.stats.set(s); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
  }
}
