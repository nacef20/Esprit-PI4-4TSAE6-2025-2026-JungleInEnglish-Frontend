import { Injectable, inject, signal, PLATFORM_ID } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { isPlatformBrowser } from '@angular/common';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface User {
    id: number;
    username: string;
    email: string;
    roles: string[];
    level?: string;
}

export interface AuthResponse {
    token: string;
    tokenType: string;
    id: number;
    username: string;
    email: string;
    roles: string[];
    level?: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
    private readonly http = inject(HttpClient);
    private readonly platformId = inject(PLATFORM_ID);
    private readonly isBrowser = isPlatformBrowser(this.platformId);
    private readonly baseUrl = `${environment.todoApiUrl}/api/auth`;

    private currentUserSignal = signal<User | null>(this.getUserFromStorage());
    readonly currentUser = this.currentUserSignal.asReadonly();

    login(credentials: any): Observable<AuthResponse> {
        return this.http.post<AuthResponse>(`${this.baseUrl}/signin`, credentials).pipe(
            tap(response => {
                console.log('[AuthService] Login response:', response);
                this.saveUserToStorage(response);
                this.currentUserSignal.set({
                    id: response.id,
                    username: response.username,
                    email: response.email,
                    roles: response.roles,
                    level: response.level
                });
            })
        );
    }

    signup(userData: any): Observable<any> {
        return this.http.post(`${this.baseUrl}/signup`, userData);
    }

    logout(): void {
        if (this.isBrowser) {
            localStorage.removeItem('user');
            localStorage.removeItem('token');
        }
        this.currentUserSignal.set(null);
    }

    getToken(): string | null {
        if (!this.isBrowser) {
            return null;
        }

        // 1. Try direct token key
        const token = localStorage.getItem('token');
        if (token) {
            console.log('[AuthService] getToken: Found token in localStorage (token key)');
            return token;
        }

        // 2. Try inside 'user' object (legacy/alternative structure)
        const userJson = localStorage.getItem('user');
        console.log('[AuthService] getToken: user key value:', userJson ? 'EXISTS' : 'NULL');

        if (userJson) {
            try {
                const user = JSON.parse(userJson);
                const extractedToken = user.token || user.accessToken;
                if (extractedToken) {
                    console.log('[AuthService] getToken: Extracted token from user object');
                    return extractedToken;
                }
            } catch (e) {
                console.error('[AuthService] getToken: Error parsing user object', e);
            }
        }

        console.warn('[AuthService] getToken: No token found anywhere');
        return null;
    }

    isTeacher(): boolean {
        const user = this.currentUser();
        return !!user?.roles.some(r => r.toUpperCase() === 'ROLE_TEACHER' || r.toUpperCase() === 'TEACHER');
    }

    isStudent(): boolean {
        const user = this.currentUser();
        return !!user?.roles.some(r => r.toUpperCase() === 'ROLE_STUDENT' || r.toUpperCase() === 'STUDENT');
    }

    isAuthenticated(): boolean {
        return !!this.currentUser();
    }

    private saveUserToStorage(response: AuthResponse): void {
        if (this.isBrowser) {
            localStorage.setItem('user', JSON.stringify(response));
            localStorage.setItem('token', response.token);
        }
    }

    private getUserFromStorage(): User | null {
        if (!this.isBrowser) return null;

        const userJson = localStorage.getItem('user');
        if (!userJson) return null;
        try {
            const user = JSON.parse(userJson);
            return user;
        } catch (e) {
            localStorage.removeItem('user');
            return null;
        }
    }
}
