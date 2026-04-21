import { Injectable } from '@angular/core';
import {
  HttpInterceptor,
  HttpRequest,
  HttpHandler,
  HttpEvent
} from '@angular/common/http';
import { Observable, from } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { AuthService } from '../auth/auth.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(private authService: AuthService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {

    // 🌍 PUBLIC ROUTES
    const publicUrls = [
      '/api/users/add',
      '/api/auth/login',
      '/api/auth/register',
      '/api/complaints',
      '/api/responses'
    ];

    const isPublic = publicUrls.some(url => req.url.includes(url));

    // ✅ BYPASS TOKEN
    if (isPublic) {
      return next.handle(req);
    }



    
    // 🔐 SECURE ROUTES
    return from(this.authService.getToken()).pipe(
      switchMap(token => {

        if (!token) {
          return next.handle(req);
        }

        const authReq = req.clone({
          setHeaders: {
            Authorization: `Bearer ${token}`
          }
        });

        return next.handle(authReq);
      })
    );
  }
}