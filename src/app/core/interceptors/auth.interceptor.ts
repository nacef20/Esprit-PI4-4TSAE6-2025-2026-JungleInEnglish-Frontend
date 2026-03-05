import { HttpInterceptorFn } from '@angular/common/http';
import { inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
    const platformId = inject(PLATFORM_ID);
    const isBrowser = isPlatformBrowser(platformId);

    // Skip auth logic if not in browser context (SSR)
    if (!isBrowser) {
        return next(req);
    }

    const authService = inject(AuthService);
    const token = authService.getToken();

    if (token) {
        console.log(`[AuthInterceptor] 🛡️ Attaching token to ${req.url}`);
        const authReq = req.clone({
            setHeaders: {
                Authorization: `Bearer ${token}`
            }
        });
        return next(authReq);
    }

    console.warn(`[AuthInterceptor] ❌ No token found for ${req.url}. Is user logged in?`);
    return next(req);
};
