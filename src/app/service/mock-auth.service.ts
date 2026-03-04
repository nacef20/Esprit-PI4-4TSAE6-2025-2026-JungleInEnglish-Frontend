import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, of, timer } from 'rxjs';
import { delay, switchMap } from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class MockAuthService {
  private users = new Map<number, {
    id: number;
    email: string;
    password: string;
    role: string;
    name: string;
  }>([
    [1, {
      id: 1,
      email: 'admin@ilyes.tn',
      password: 'admin123',
      role: 'ADMIN',
      name: 'Admin User'
    }],
    [2, {
      id: 2,
      email: 'user@ilyes.tn',
      password: 'user123',
      role: 'USER',
      name: 'Regular User'
    }]
  ]);

  private authenticatedUser = new BehaviorSubject<any>(null);
  private nextUserId = 3;

  login(credentials: { email: string; password: string }): Observable<any> {
    const user = Array.from(this.users.values()).find(u => 
      u.email === credentials.email && u.password === credentials.password
    );

    if (user) {
      const token = `mock-token-${Date.now()}`;
      this.authenticatedUser.next(user);
      return timer(1000).pipe(
        switchMap(() => of({
          token,
          user: {
            id: user.id,
            name: user.name,
            email: user.email,
            role: user.role
          }
        }))
      );
    }

    return timer(1000).pipe(
      switchMap(() => {
        throw new Error('Invalid credentials');
      })
    );
  }

  register(user: { name: string; email: string; password: string }): Observable<any> {
    const newUser = {
      id: this.nextUserId++,
      email: user.email,
      password: user.password,
      role: 'USER',
      name: user.name
    };
    
    this.users.set(newUser.id, newUser);
    const token = `mock-token-${Date.now()}`;
    this.authenticatedUser.next(newUser);
    
    return timer(1000).pipe(
      switchMap(() => of({
        token,
        user: {
          id: newUser.id,
          name: newUser.name,
          email: newUser.email,
          role: newUser.role
        }
      }))
    );
  }

  logout(): void {
    this.authenticatedUser.next(null);
  }

  getCurrentUser(): any {
    return this.authenticatedUser.value;
  }

  isAuthenticated(): boolean {
    return !!this.authenticatedUser.value;
  }

  getToken(): string | null {
    if (this.authenticatedUser.value) {
      return `mock-token-${Date.now()}`;
    }
    return null;
  }
}
