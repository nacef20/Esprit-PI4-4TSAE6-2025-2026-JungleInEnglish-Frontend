import { Injectable, signal, computed, PLATFORM_ID, inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Router } from '@angular/router';
import Keycloak from 'keycloak-js';
import { environment } from '../../../environments/environment';

export interface UserContext {
  id: string;
  email: string;
  name: string;
  roles: string[];
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly router = inject(Router);
  private readonly platformId = inject(PLATFORM_ID);

  private keycloak: Keycloak | null = null;

  // State
  private readonly userCtx = signal<UserContext | null>(null);
  private readonly mockToken = signal<string | null>(null);

  // Computed
  readonly isLoggedIn = computed(() => !!this.userCtx());
  readonly currentUser = computed(() => this.userCtx());
  readonly isAdmin = computed(() => {
    const roles = this.userCtx()?.roles || [];
    return roles.some(r => r.toUpperCase() === 'ROLE_ADMIN' || r.toUpperCase() === 'ADMIN');
  });

  constructor() {
    if (isPlatformBrowser(this.platformId) && !environment.mockMode) {
      this.initKeycloak();
    } else if (environment.mockMode) {
      this.initMockMode();
    }
  }

  private async initKeycloak() {
    this.keycloak = new Keycloak({
      url: environment.keycloak.url,
      realm: environment.keycloak.realm,
      clientId: environment.keycloak.clientId
    });

    try {
      const authenticated = await this.keycloak.init({
        onLoad: 'check-sso',
        silentCheckSsoRedirectUri: window.location.origin + '/assets/silent-check-sso.html',
        checkLoginIframe: false
      });

      if (authenticated) {
        this.updateUserContext(this.keycloak);
      }
    } catch (error) {
      console.error('Keycloak init failed', error);
    }
  }

  private initMockMode() {
    // In mock mode, we check localStorage just to optionally remember a mock login during development
    if (isPlatformBrowser(this.platformId)) {
      try {
        const stored = localStorage.getItem('mockUserCtx');
        if (stored) {
          this.userCtx.set(JSON.parse(stored));
          this.mockToken.set('mock-jwt-token');
        }
      } catch (e) {
        console.error('Failed to parse mock stored user', e);
      }
    }
  }

  private updateUserContext(kc: Keycloak) {
    if (!kc.tokenParsed) return;

    let roles: string[] = [];
    const realmAccess = kc.tokenParsed['realm_access'] as any;
    if (realmAccess && Array.isArray(realmAccess.roles)) {
      roles = realmAccess.roles;
    }

    this.userCtx.set({
      id: kc.subject || '',
      email: kc.tokenParsed['email'] || '',
      name: kc.tokenParsed['preferred_username'] || '',
      roles: roles
    });
  }

  getToken(): string | null {
    if (environment.mockMode) return this.mockToken();
    return this.keycloak?.token || null;
  }

  async login(mockCredentials?: { email: string; role: string }) {
    if (environment.mockMode) {
      // Create a mock user context based on credentials
      const role = mockCredentials?.role || 'user';
      const ctx: UserContext = {
        id: 'mock-user-' + Math.floor(Math.random() * 10000),
        email: mockCredentials?.email || 'mock@example.com',
        name: 'Mock User',
        roles: role === 'admin' ? ['ROLE_ADMIN'] : ['ROLE_USER']
      };

      this.userCtx.set(ctx);
      this.mockToken.set('mock-jwt-token');

      if (isPlatformBrowser(this.platformId)) {
        localStorage.setItem('mockUserCtx', JSON.stringify(ctx));
      }
      return this.router.navigate(['/']);
    }

    // Keycloak login
    if (this.keycloak) {
      return this.keycloak.login({
        redirectUri: window.location.origin + '/'
      });
    }
    return Promise.resolve();
  }

  logout() {
    if (environment.mockMode) {
      this.userCtx.set(null);
      this.mockToken.set(null);
      if (isPlatformBrowser(this.platformId)) {
        localStorage.removeItem('mockUserCtx');
      }
      this.router.navigate(['/login']);
    } else {
      if (this.keycloak) {
        this.keycloak.logout({
          redirectUri: window.location.origin + '/'
        });
      }
    }
  }

  // Used by interceptor to automatically refresh tokens
  async updateToken(minValidity = 20): Promise<boolean> {
    if (environment.mockMode) return true;
    if (!this.keycloak || !this.keycloak.authenticated) return false;

    try {
      return await this.keycloak.updateToken(minValidity);
    } catch (error) {
      await this.keycloak.login();
      return false;
    }
  }
}
