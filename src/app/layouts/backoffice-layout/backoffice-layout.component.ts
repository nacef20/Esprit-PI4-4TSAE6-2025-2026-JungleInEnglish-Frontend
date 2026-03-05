import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-backoffice-layout',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, RouterOutlet],
  template: `
    <div class="layout">
      <nav class="nav">
        <div class="nav-brand">Backoffice</div>
        <div class="nav-links">
          <a
            routerLink="/admin"
            routerLinkActive="active"
            [routerLinkActiveOptions]="{ exact: true }"
          >
            Dashboard
          </a>
          <a routerLink="/admin/todos" routerLinkActive="active">Todos</a>
          <a routerLink="/admin/events" routerLinkActive="active">Events</a>
        </div>
        
        <div class="user-info">
          <span>{{ currentUser()?.username }}</span>
          <button (click)="logout()" class="logout-btn">Logout</button>
        </div>

        <a routerLink="/" class="nav-back">← Frontoffice</a>
      </nav>
      <main class="main">
        <router-outlet />
      </main>
    </div>
  `,
  styles: `
    .layout {
      min-height: 100vh;
      display: flex;
      flex-direction: column;
      background: #f8fafc;
    }

    .nav {
      display: flex;
      align-items: center;
      gap: 1.5rem;
      padding: 0 1.5rem;
      height: 56px;
      background: linear-gradient(135deg, #15803d 0%, #166534 100%);
      color: #fff;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.12);
    }

    .nav-brand {
      font-weight: 700;
      font-size: 1.1rem;
      letter-spacing: -0.02em;
    }

    .nav-links {
      display: flex;
      align-items: center;
      gap: 0.25rem;
    }

    .nav a {
      color: rgba(255, 255, 255, 0.95);
      text-decoration: none;
      padding: 0.5rem 0.75rem;
      border-radius: 8px;
      font-size: 0.9rem;
      font-weight: 500;
      transition: background 0.15s, color 0.15s;
    }

    .nav a:hover {
      background: rgba(255, 255, 255, 0.15);
      color: #fff;
    }

    .nav a.active {
      background: rgba(255, 255, 255, 0.2);
      color: #fff;
    }

    .user-info {
      margin-left: auto;
      display: flex;
      align-items: center;
      gap: 1rem;
      font-size: 0.85rem;
    }

    .logout-btn {
      background: rgba(255, 255, 255, 0.1);
      border: 1px solid rgba(255, 255, 255, 0.2);
      color: #fff;
      padding: 0.25rem 0.75rem;
      border-radius: 6px;
      cursor: pointer;
      transition: all 0.2s;
    }

    .logout-btn:hover {
      background: rgba(255, 255, 255, 0.2);
    }

    .nav-back {
      background: rgba(255, 255, 255, 0.12);
    }

    .nav-back:hover {
      background: rgba(255, 255, 255, 0.22);
    }

    .main {
      flex: 1;
      padding: 1.5rem;
      max-width: 1200px;
      width: 100%;
      margin: 0 auto;
      box-sizing: border-box;
    }
  `,
})
export class BackofficeLayoutComponent {
  private authService = inject(AuthService);
  private router = inject(Router);

  currentUser = this.authService.currentUser;

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}

