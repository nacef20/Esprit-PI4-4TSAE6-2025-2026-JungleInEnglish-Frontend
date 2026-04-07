import { Injectable, inject } from '@angular/core';
import { HttpRequest, HttpHandler, HttpEvent, HttpInterceptor, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { MessageService } from '../../services/message.service';

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  private readonly messageService = inject(MessageService);

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req).pipe(
      catchError((err: unknown) => {
        if (err instanceof HttpErrorResponse) {
          console.error(`[DEBUG] API ERROR: ${err.status} - ${err.url}`, err);
          
          if (err.status === 0) {
            this.messageService.error(`Cannot reach Gateway at ${err.url || '8222'}. Please verify that the Backend services (Gateway and Microservices) are running.`);
          } else if (err.status === 401 || err.status === 403) {
            this.messageService.error('Session expired or access denied.');
          } else {
            const message = this.getErrorMessage(err);
            this.messageService.error(message);
          }
        } else {
          this.messageService.error('An unexpected error occurred.');
        }
        return throwError(() => err);
      })
    );
  }

  private getErrorMessage(err: HttpErrorResponse): string {
    if (err.error?.message && typeof err.error.message === 'string') return err.error.message;
    if (err.error?.error && typeof err.error.error === 'string') return err.error.error;
    if (typeof err.error === 'string') return err.error;

    switch (err.status) {
      case 0: return 'Connection refused. The backend service might be offline.';
      case 400: return 'Invalid request.';
      case 404: return 'Resource not found.';
      case 500: return 'Server error. Please try again later.';
      default: return err.message || `Error ${err.status}.`;
    }
  }
}
