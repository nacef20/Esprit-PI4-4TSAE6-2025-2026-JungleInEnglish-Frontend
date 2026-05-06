import { Injectable, inject } from '@angular/core';
import { HttpRequest, HttpHandler, HttpEvent, HttpInterceptor } from '@angular/common/http';
import { from, Observable } from 'rxjs';
import { mergeMap } from 'rxjs/operators';
import { AuthService } from '../../services/auth.service';
import { environment } from '@env/environment';

@Injectable()
export class JwtInterceptor implements HttpInterceptor {
  private readonly authService = inject(AuthService);

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // Exclude external calls
    if (!req.url.startsWith(environment.apiBaseUrl)) {
      return next.handle(req);
    }

    // 1. MOCK MODE Headers
    if (environment.mockMode) {
      const user = this.authService.currentUser();
      if (user) {
        const cloned = req.clone({
          setHeaders: {
            'X-Mock-User-Id': user.id,
            'X-Mock-User-Email': user.email,
            'X-Mock-User-Role': user.roles[0] || 'ROLE_USER'
          }
        });
        return next.handle(cloned);
      }
      return next.handle(req);
    }

    // 2. PRODUCTION / KEYCLOAK MODE
    return from(this.authService.updateToken(30)).pipe(
      mergeMap(() => {
        const token = this.authService.getToken();
        if (token) {
          const cloned = req.clone({
            setHeaders: {
              Authorization: `Bearer ${token}`
            }
          });
          return next.handle(cloned);
        }
        return next.handle(req);
      })
    );
  }
}
