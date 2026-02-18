import {
  HttpRequest,
  HttpHandlerFn,
  HttpErrorResponse,
  HttpEvent,
} from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, Observable, throwError } from 'rxjs';
import { MessageService } from '../services/message.service';

/**
 * Intercepteur HTTP : en cas d'erreur (4xx/5xx), affiche un message utilisateur
 * et propage l'erreur pour que les composants puissent réagir si besoin.
 */
export function errorInterceptor(
  req: HttpRequest<unknown>,
  next: HttpHandlerFn
): Observable<HttpEvent<unknown>> {
  const messageService = inject(MessageService);

  return next(req).pipe(
    catchError((err: unknown) => {
      if (err instanceof HttpErrorResponse) {
        const message = getErrorMessage(err);
        messageService.error(message);
      } else {
        messageService.error('Une erreur inattendue est survenue.');
      }
      return throwError(() => err);
    })
  );
}

function getErrorMessage(err: HttpErrorResponse): string {
  if (err.error?.message && typeof err.error.message === 'string') {
    return err.error.message;
  }
  if (err.error?.error && typeof err.error.error === 'string') {
    return err.error.error;
  }
  if (typeof err.error === 'string') {
    return err.error;
  }
  switch (err.status) {
    case 0:
      return 'Impossible de joindre le serveur. Vérifiez que le backend est démarré.';
    case 400:
      return 'Requête invalide.';
    case 401:
      return 'Session expirée ou non authentifié.';
    case 403:
      return 'Accès refusé.';
    case 404:
      return 'Ressource introuvable.';
    case 500:
      return 'Erreur serveur. Réessayez plus tard.';
    default:
      return err.message || `Erreur ${err.status}.`;
  }
}
