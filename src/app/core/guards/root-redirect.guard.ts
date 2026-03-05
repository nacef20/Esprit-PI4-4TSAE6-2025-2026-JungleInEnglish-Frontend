import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const rootRedirectGuard: CanActivateFn = () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (!authService.isAuthenticated()) {
        return router.createUrlTree(['/login']);
    }

    if (authService.isTeacher()) {
        return router.createUrlTree(['/admin/todos']);
    }

    // Default for students or others
    return router.createUrlTree(['/home']);
};
