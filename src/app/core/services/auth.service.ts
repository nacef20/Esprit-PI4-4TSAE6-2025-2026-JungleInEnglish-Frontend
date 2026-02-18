import { Injectable, inject, signal, computed, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs';
import { API_CONFIG } from '../constants/api.constants';
import type { User } from '../../shared/models/user.model';

const AUTH_URL = `${API_CONFIG.apiUrl}/auth`;
const TOKEN_KEY = 'accessToken';
const USER_KEY = 'user';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface SignupRequest {
  name: string;
  email: string;
  password: string;
}

export interface AuthResponse {
  accessToken: string;
  token?: string;
  user?: User;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly platformId = inject(PLATFORM_ID);

  private readonly token = signal<string | null>(this.getStoredToken());
  private readonly user = signal<User | null>(this.getStoredUser());

  readonly isLoggedIn = computed(() => !!this.token());
  readonly currentUser = computed(() => this.user());
  readonly isAdmin = computed(() => {
    const role = this.user()?.role?.toUpperCase();
    return role === 'ADMIN' || role === 'ROLE_ADMIN';
  });

  private getStoredToken(): string | null {
    if (!isPlatformBrowser(this.platformId)) return null;
    return localStorage.getItem(TOKEN_KEY);
  }

  private getStoredUser(): User | null {
    if (!isPlatformBrowser(this.platformId)) return null;
    try {
      const raw = localStorage.getItem(USER_KEY);
      return raw ? JSON.parse(raw) : null;
    } catch {
      return null;
    }
  }

  login(credentials: LoginRequest) {
    return this.http.post<AuthResponse>(`${AUTH_URL}/login`, credentials).pipe(
      tap((res) => this.handleAuthSuccess(res.accessToken ?? (res as { token?: string }).token, res.user))
    );
  }

  signup(data: SignupRequest) {
    return this.http.post<AuthResponse>(`${AUTH_URL}/register`, data).pipe(
      tap((res) => this.handleAuthSuccess(res.accessToken ?? (res as { token?: string }).token, res.user))
    );
  }

  private handleAuthSuccess(token: string | undefined, user?: User | null): void {
    if (!token) return;
    if (isPlatformBrowser(this.platformId)) {
      localStorage.setItem(TOKEN_KEY, token);
      if (user) localStorage.setItem(USER_KEY, JSON.stringify(user));
    }
    this.token.set(token);
    this.user.set(user ?? null);
  }

  logout(): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(USER_KEY);
    }
    this.token.set(null);
    this.user.set(null);
    this.router.navigate(['/login']);
  }
}
