import { inject, PLATFORM_ID } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { isPlatformBrowser } from '@angular/common';
import { AuthService } from '../services/auth.service';

export const roleGuard: CanActivateFn = (route, state) => {
    const authService = inject(AuthService);
    const router = inject(Router);
    const isBrowser = isPlatformBrowser(inject(PLATFORM_ID));

    if (!isBrowser) return true;

    const user = authService.currentUser();

    // Check if user has teacher role (case insensitive, prefix insensitive)
    const isTeacher = user?.roles?.some(r =>
        r.toUpperCase() === 'ROLE_TEACHER' || r.toUpperCase() === 'TEACHER'
    );

    if (authService.isAuthenticated() && isTeacher) {
        return true;
    }

    return router.createUrlTree(['/home']);
};
