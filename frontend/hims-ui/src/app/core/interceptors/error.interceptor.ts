import { Injectable } from '@angular/core';
import { HttpRequest, HttpHandler, HttpEvent, HttpInterceptor, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Router } from '@angular/router';
import { TokenStorageService } from '../auth/token-storage.service';
import { NotificationService } from '../services/notification.service';

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  constructor(
    private tokenStorage: TokenStorageService,
    private notificationService: NotificationService,
    private router: Router
  ) {}

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        const displayMessage = this.extractErrorMessage(error);

        switch (error.status) {
          case 401:
            this.notificationService.error(displayMessage || 'Session expired or invalid credentials.');
            this.tokenStorage.clear();
            this.router.navigate(['/login']);
            break;
          case 403:
            this.notificationService.error(displayMessage || 'Access Denied: You do not have permission to perform this action.');
            break;
          default:
            this.notificationService.error(displayMessage);
            break;
        }

        return throwError(() => error);
      })
    );
  }

  /**
   * Extracts clean, human-readable error messages from Spring Boot backend responses,
   * handling parsed JSON objects, stringified JSON bodies, or plain text messages.
   */
  private extractErrorMessage(error: HttpErrorResponse): string {
    let rawError = error.error;

    // Handle responses where responseType was 'text' and error is a stringified JSON
    if (typeof rawError === 'string') {
      const trimmed = rawError.trim();
      if ((trimmed.startsWith('{') && trimmed.endsWith('}')) || (trimmed.startsWith('[') && trimmed.endsWith(']'))) {
        try {
          rawError = JSON.parse(trimmed);
        } catch {
          // If JSON parse fails, rawError remains as string
        }
      }
    }

    if (rawError && typeof rawError === 'object') {
      if (rawError.message && typeof rawError.message === 'string') {
        return rawError.message;
      }
      if (rawError.error && typeof rawError.error === 'string') {
        return rawError.error;
      }
      if (rawError.details && typeof rawError.details === 'string') {
        return rawError.details;
      }
    } else if (typeof rawError === 'string' && rawError.trim().length > 0) {
      return rawError.trim();
    }

    if (error.status === 0) {
      return 'Unable to connect to server. Please check your network or backend service.';
    }

    if (error.message) {
      return error.message;
    }

    return 'An unexpected error occurred. Please try again.';
  }
}
