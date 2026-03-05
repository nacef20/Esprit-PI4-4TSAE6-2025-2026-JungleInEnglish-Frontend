import { inject, PLATFORM_ID } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { isPlatformBrowser } from '@angular/common';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = (route, state) => {
    const authService = inject(AuthService);
    const router = inject(Router);
    const isBrowser = isPlatformBrowser(inject(PLATFORM_ID));

    if (!isBrowser) return true;

    if (authService.isAuthenticated()) {
        return true;
    }

    return router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
};
