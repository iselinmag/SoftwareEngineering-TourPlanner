import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);

  // login and register show their own error messages, so let them handle it
  if (req.url.includes('/api/auth')) {
    return next(req);
  }

  return next(req).pipe(
    catchError((err: HttpErrorResponse) => {
      // the backend sends a message like { "error": "This tour is not yours" }
      // we read it if it is there, otherwise fall back to a generic line
      const backendMessage = err.error?.error;

      if (err.status === 403) {
        // forbidden, you tried to touch something that is not yours
        alert(backendMessage || 'You can only change your own tours and logs.');
      } else if (err.status === 404) {
        // not found, the thing you asked for does not exist
        alert(backendMessage || 'That item could not be found.');
      } else if (err.status === 401) {
        // token missing or expired, send them back to login
        localStorage.removeItem('token');
        router.navigate(['/login']);
      }

      // pass the error along so any other code can still react to it
      return throwError(() => err);
    })
  );
};
