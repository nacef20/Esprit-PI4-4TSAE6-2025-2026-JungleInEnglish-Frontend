import { Injectable, signal, computed, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService as GlobalAuthService } from '../../auth/auth.service';

export interface UserContext {
  id: string;
  email: string;
  name: string;
  roles: string[];
}

/**
 * Harmonized AuthService for the Shop module.
 * Wraps the global Keycloak-based AuthService to provide a consistent session
 * while maintaining the interface expected by shop components.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly globalAuth = inject(GlobalAuthService);
  private readonly router = inject(Router);

  // State (bridged from global)
  private readonly userCtx = signal<UserContext | null>(null);

  // Computed
  readonly isLoggedIn = computed(() => this.globalAuth.isLoggedIn());
  readonly currentUser = computed(() => {
    const info = this.globalAuth.getUserInfo();
    if (!info) return null;
    return {
      id: info.id || '',
      email: info.email || '',
      name: info.username || info.firstName || 'User',
      roles: info.roles
    } as UserContext;
  });

  readonly isAdmin = computed(() => this.globalAuth.isAdmin());

  constructor() {
    // Initial sync of user context if logged in
    const info = this.globalAuth.getUserInfo();
    if (info) {
      this.userCtx.set({
        id: info.id || '',
        email: info.email || '',
        name: info.username || info.firstName || 'User',
        roles: info.roles
      });
    }
  }

  getToken(): string | null {
    return this.globalAuth.getToken2() || null;
  }

  async login(mockCredentials?: { email: string; role: string }) {
    // Redirects to Keycloak via global service. 
    // mockCredentials are no longer needed as we use real Keycloak.
    this.globalAuth.login();
    return Promise.resolve();
  }

  logout() {
    // Standard Keycloak logout
    this.globalAuth.logout();
  }

  async updateToken(minValidity = 20): Promise<boolean> {
    // We bridge this to the global service's login status check.
    return Promise.resolve(this.globalAuth.isLoggedIn());
  }
}
