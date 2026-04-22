import { CanActivateFn, Router } from '@angular/router';
import { inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { AuthService } from '@shop/services/auth.service';

export const authGuard: CanActivateFn = () => {
    const auth = inject(AuthService);
    const platformId = inject(PLATFORM_ID);
    
    if (!isPlatformBrowser(platformId)) return true;

    // [DIAGNOSTIC BYPASS] - Allowing any user to proceed to isolate redirection issues.
    console.log('[DEBUG] Auth Guard: BYPASS ENABLED.');
    return true;

    /* Original Logic:
    if (auth.isLoggedIn()) return true;
    
    console.log('[DEBUG] Auth Guard: Not Logged In. Triggering login.');
    auth.login();
    return false;
    */
};
