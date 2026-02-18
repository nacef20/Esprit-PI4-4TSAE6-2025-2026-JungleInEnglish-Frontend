import { Component, inject, signal, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AdminService } from '../../../core/services/admin.service';

@Component({
    selector: 'app-admin-dashboard',
    standalone: true,
    imports: [RouterLink],
    template: `
    <div class="container">
      <div class="page-header">
        <h1>🏠 Tableau de bord</h1>
        <p>Vue d'ensemble de votre boutique</p>
      </div>

      @if (loading()) {
        <div class="loading-center"><div class="spinner"></div></div>
      } @else {
        <div class="stat-grid">
          <div class="stat-card">
            <span class="stat-card__label">Produits</span>
            <span class="stat-card__value">{{ stats().totalProducts }}</span>
          </div>
          <div class="stat-card">
            <span class="stat-card__label">Catégories</span>
            <span class="stat-card__value">{{ stats().totalCategories }}</span>
          </div>
          <div class="stat-card">
            <span class="stat-card__label">Commandes</span>
            <span class="stat-card__value">{{ stats().totalOrders }}</span>
          </div>
          <div class="stat-card">
            <span class="stat-card__label">Utilisateurs</span>
            <span class="stat-card__value">{{ stats().totalUsers }}</span>
          </div>
        </div>

        <h2 class="mt-4 mb-2" style="font-size:1.2rem;">Accès rapide</h2>
        <div class="admin-grid">
          <a routerLink="/admin/products" class="quick-link card">
            <div class="card__body">
              <div style="font-size:2rem;margin-bottom:.5rem;">📦</div>
              <div class="card__title">Produits</div>
              <p class="card__text">Gérer les produits</p>
            </div>
          </a>
          <a routerLink="/admin/categories" class="quick-link card">
            <div class="card__body">
              <div style="font-size:2rem;margin-bottom:.5rem;">📂</div>
              <div class="card__title">Catégories</div>
              <p class="card__text">Gérer les catégories</p>
            </div>
          </a>
          <a routerLink="/admin/orders" class="quick-link card">
            <div class="card__body">
              <div style="font-size:2rem;margin-bottom:.5rem;">🚚</div>
              <div class="card__title">Commandes</div>
              <p class="card__text">Suivre les commandes</p>
            </div>
          </a>
          <a routerLink="/admin/users" class="quick-link card">
            <div class="card__body">
              <div style="font-size:2rem;margin-bottom:.5rem;">👥</div>
              <div class="card__title">Utilisateurs</div>
              <p class="card__text">Gérer les comptes</p>
            </div>
          </a>
        </div>
      }
    </div>
  `,
    styles: `
    :host { display: block; padding-bottom: 3rem; }
    .quick-link { text-decoration: none; color: inherit; }
  `,
})
export class AdminDashboardComponent implements OnInit {
    private readonly adminService = inject(AdminService);
    readonly stats = signal({ totalProducts: 0, totalCategories: 0, totalOrders: 0, totalUsers: 0 });
    readonly loading = signal(true);

    ngOnInit(): void {
        this.adminService.getDashboard().subscribe({
            next: s => { this.stats.set(s); this.loading.set(false); },
            error: () => this.loading.set(false),
        });
    }
}
