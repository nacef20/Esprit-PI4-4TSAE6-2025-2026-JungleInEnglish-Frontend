import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from './auth/auth.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit {
  title = 'school-management';

  constructor(private authService: AuthService, private router: Router) {}

  ngOnInit() {
    // Wait one tick so Angular Router finishes its initial navigation
    setTimeout(() => {
      if (!this.authService.isLoggedIn()) return;

      const path     = window.location.pathname;
      const search   = window.location.search;

      // Redirect only when on root OR when Keycloak just redirected back with auth params
      const isRoot            = path === '/' || path === '';
      const isKeycloakCallback = search.includes('state=') || search.includes('code=') || search.includes('session_state=');

      if (!isRoot && !isKeycloakCallback) return;

      const roles = this.authService.getRoles().map((r: string) => r.toUpperCase());

      if (roles.includes('CANDIDATE')) {
        this.router.navigate(['/jobs'], { replaceUrl: true });
      } else if (roles.some((r: string) => ['ADMIN', 'EMPLOYER', 'TEACHER', 'STUDENT'].includes(r))) {
        this.router.navigate(['/admin'], { replaceUrl: true });
      }
    }, 0);
  }
}
