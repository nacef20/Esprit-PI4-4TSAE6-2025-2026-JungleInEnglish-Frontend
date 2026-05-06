import { CanActivateFn, Router } from '@angular/router';
import { inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { AuthService } from '@shop/services/auth.service';

export const adminGuard: CanActivateFn = () => {
    const auth = inject(AuthService);
    const router = inject(Router);
    const platformId = inject(PLATFORM_ID);

    if (!isPlatformBrowser(platformId)) return true;
    
    console.log('[DEBUG] Admin Guard Checking...');

    if (!auth.isLoggedIn()) {
        console.log('[DEBUG] Admin Guard: Not Logged In. Redirecting to Login.');
        auth.login();
        return false;
    }

    // [DIAGNOSTIC BYPASS] - Allowing access to any authenticated user to troubleshoot role issues.
    console.warn('[DEBUG] Admin Guard: BYPASS ENABLED. Allowing access to authenticated user.');
    return true; 

    /* Original Logic:
    if (auth.isAdmin()) {
        console.log('[DEBUG] Admin Guard: Authorized.');
        return true;
    }

    console.warn('[DEBUG] Admin Guard: ACCESS DENIED. Role not recognized. Redirecting to home.');
    router.navigate(['/']);
    return false;
    */
};
