import { Component } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-frontoffice-layout',
  standalone: true,
  imports: [RouterLink, RouterOutlet],
  template: `
    <nav class="nav">
      <a routerLink="/todos" routerLinkActive="active">Todos</a>
      <a routerLink="/events" routerLinkActive="active">Events</a>
      <a routerLink="/admin" class="admin-link">Admin</a>
    </nav>
    <main class="main">
      <router-outlet />
    </main>
  `,
  styles: `
    .nav {
      display: flex;
      gap: 1rem;
      padding: 1rem;
      background: #1976d2;
      color: white;
    }
    .nav a {
      color: white;
      text-decoration: none;
    }
    .nav a:hover, .nav a.active {
      text-decoration: underline;
    }
    .admin-link {
      margin-left: auto;
    }
    .main {
      padding: 1.5rem;
    }
  `,
})
export class FrontofficeLayoutComponent {}
