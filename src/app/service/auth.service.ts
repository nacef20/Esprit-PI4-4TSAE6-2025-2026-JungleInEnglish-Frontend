import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { MockAuthService } from './mock-auth.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = environment.apiUrl;
  private tokenKey = 'jwtToken';
  private userKey = 'user';
  private authEndpoint = '/auth';
  private mockService: MockAuthService;

  constructor(private http: HttpClient) {
    this.mockService = new MockAuthService();
  }

  login(credentials: { email: string; password: string }): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/api/auth/login`, credentials).pipe(
      tap(response => {
        if (response && response.token) {
          localStorage.setItem(this.tokenKey, response.token);
          if (response.user) {
            const user = {
              id: response.user.id,
              name: response.user.name,
              email: response.user.email,
              role: response.user.role || 'CANDIDATE'
            };
            localStorage.setItem(this.userKey, JSON.stringify(user));
          }
        }
      }),
      catchError(error => {
        // Only use mock service if backend is completely unavailable
        if (error.status === 0) {
          return this.mockService.login(credentials).pipe(
            tap(response => {
              if (response && response.token) {
                localStorage.setItem(this.tokenKey, response.token);
                if (response.user) {
                  localStorage.setItem(this.userKey, JSON.stringify(response.user));
                }
              }
            })
          );
        }
        return this.handleError(error);
      })
    );
  }

  register(user: { name: string; email: string; password: string; role?: string }): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/api/auth/signup`, user).pipe(
      catchError(error => {
        if (error.status === 0) {
          return this.mockService.register(user).pipe(
            tap(response => {
              if (response && response.token) {
                localStorage.setItem(this.tokenKey, response.token);
                if (response.user) {
                  localStorage.setItem(this.userKey, JSON.stringify(response.user));
                }
              }
            })
          );
        }
        return this.handleError(error);
      })
    );
  }

  logout() {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.userKey);
  }

  getCurrentUser(): any {
    return JSON.parse(localStorage.getItem(this.userKey) || '{}');
  }

  refreshToken(): Observable<any> {
    const refreshToken = localStorage.getItem('refreshToken');
    if (!refreshToken) {
      return throwError(() => new Error('No refresh token available'));
    }
    
    return this.http.post<any>(`${this.apiUrl}/api/auth/refresh`, { refreshToken }).pipe(
      tap(response => {
        if (response && response.token) {
          localStorage.setItem(this.tokenKey, response.token);
        }
      }),
      catchError(this.handleError)
    );
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  private handleError(error: any): Observable<never> {
    let errorMessage = 'An error occurred';
    if (error.error instanceof ErrorEvent) {
      // Client-side error
      errorMessage = `Error: ${error.error.message}`;
    } else {
      // Server-side error: show user-friendly message for auth failures
      if (error.status === 401) {
        errorMessage = typeof error.error === 'string' && error.error.length > 0
          ? error.error
          : 'Invalid email or password. Please try again.';
      } else if (error.status === 400 && typeof error.error === 'string') {
        errorMessage = error.error;
      } else {
        errorMessage = `Error Code: ${error.status}\nMessage: ${error.message}`;
      }
    }
    return throwError(() => ({ ...error, message: errorMessage }));
  }
}