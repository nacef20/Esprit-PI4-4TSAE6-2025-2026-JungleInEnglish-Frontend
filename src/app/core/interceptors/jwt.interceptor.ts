import { HttpRequest, HttpHandlerFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { from, Observable } from 'rxjs';
import { mergeMap } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';
import { environment } from '../../../environments/environment';

export function jwtInterceptor(req: HttpRequest<unknown>, next: HttpHandlerFn): Observable<any> {
  const authService = inject(AuthService);

  // Exclude external calls
  if (!req.url.startsWith(environment.apiBaseUrl)) {
    return next(req);
  }

  // 1. MOCK MODE Headers
  if (environment.mockMode) {
    const user = authService.currentUser();
    if (user) {
      const cloned = req.clone({
        setHeaders: {
          'X-Mock-User-Id': user.id,
          'X-Mock-User-Email': user.email,
          'X-Mock-User-Role': user.roles[0] || 'ROLE_USER'
        }
      });
      return next(cloned);
    }
    return next(req);
  }

  // 2. PRODUCTION / KEYCLOAK MODE
  // We wrap in from(Promise) because updateToken is async for Keycloak
  return from(authService.updateToken(30)).pipe(
    mergeMap(() => {
      const token = authService.getToken();
      if (token) {
        const cloned = req.clone({
          setHeaders: {
            Authorization: `Bearer ${token}`
          }
        });
        return next(cloned);
      }
      return next(req);
    })
  );
}
