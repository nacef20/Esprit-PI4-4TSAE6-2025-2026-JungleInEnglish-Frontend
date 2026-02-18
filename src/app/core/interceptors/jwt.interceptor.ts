import { HttpRequest, HttpHandlerFn } from '@angular/common/http';
import { inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

const AUTH_HEADER = 'Authorization';
const TOKEN_KEY = 'accessToken';

export function jwtInterceptor(
  req: HttpRequest<unknown>,
  next: HttpHandlerFn
) {
  const platformId = inject(PLATFORM_ID);
  const token = isPlatformBrowser(platformId) ? localStorage.getItem(TOKEN_KEY) : null;

  if (!token) return next(req);
  const cloned = req.clone({
    setHeaders: { [AUTH_HEADER]: `Bearer ${token}` },
  });
  return next(cloned);
}
