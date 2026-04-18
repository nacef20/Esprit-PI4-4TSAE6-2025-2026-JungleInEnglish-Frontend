import { Injectable } from '@angular/core';
import { getKeycloak } from './keycloak.config';
import { of } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
constructor() {}
  login(redirectUri?: string): void {
    getKeycloak().login({
      // Always redirect back to root so AppComponent can apply role-based routing
      redirectUri: redirectUri || window.location.origin + '/'
    });
  }

  logout(): void {
    getKeycloak().logout({
      redirectUri: window.location.origin
    });
  }

  isLoggedIn(): boolean {
    const kc = getKeycloak();
    const status = !!kc && !!kc.token && !kc.isTokenExpired();
    console.log('[DEBUG] Auth Status:', status);
    return status;
  }

  getRoles(): string[] {
    const roles = getKeycloak().tokenParsed?.realm_access?.roles || [];
    console.log('[DEBUG] User Roles:', roles);
    return roles;
  }


  getToken2(): string {
  return getKeycloak().token || '';
}

  getUserInfo() {
    const tokenParsed = getKeycloak().tokenParsed;
    if (tokenParsed) {
      return {
        id: tokenParsed['sub'],
        username: tokenParsed['preferred_username'],
        firstName: tokenParsed['given_name'],
        lastName: tokenParsed['family_name'],
        email: tokenParsed['email'],
        roles: tokenParsed['realm_access']?.roles || []
      };
    }
    return null;
  }

  isTeacher(): boolean {
    const roles = this.getRoles();
    return roles.some(r => r.toUpperCase() === 'TEACHER');
  }

  isStudent(): boolean {
    const roles = this.getRoles();
    return roles.some(r => r.toUpperCase() === 'STUDENT');
  }

  getMe() {
    return of(this.getUserInfo());
  }

  currentUser() {
    return this.getUserInfo();
  }

  async getToken(): Promise<string> {
    try {
      const keycloak = getKeycloak();

      if (!keycloak || !keycloak.token) {
        return '';
      }

      if (keycloak.isTokenExpired()) {
        await keycloak.updateToken(30);
      }

      return keycloak.token || '';
    } catch {
      return '';
    }
  }


  isAdmin(): boolean {
    const roles = this.getRoles();
    const result = roles.some(r => ['ADMIN', 'ADMIN_SHOP', 'ROLE_ADMIN', 'ADMINISTRATOR']
      .includes(r.toUpperCase()) || r.toLowerCase() === 'admin');
    console.log('[DEBUG] Is Admin Check:', result);
    return result;
  }
}
