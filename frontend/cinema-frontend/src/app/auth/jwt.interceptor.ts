import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from './auth.service';
import {
  HttpRequest,
  HttpHandlerFn,
  HttpEvent,
  HttpErrorResponse,
} from '@angular/common/http';
import { Observable, catchError, switchMap, throwError } from 'rxjs';

export const jwtInterceptor: HttpInterceptorFn = (
  req: HttpRequest<unknown>,
  next: HttpHandlerFn
): Observable<HttpEvent<unknown>> => {
  const auth = inject(AuthService);
  const accessToken = auth.getAccessToken();

  const authReq = accessToken
    ? req.clone({ setHeaders: { Authorization: `Bearer ${accessToken}` } })
    : req;

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      if (
        error.status === 401 &&
        req.url.includes('/api/') &&
        !req.url.includes('/login')
      ) {
        return auth.refresh().pipe(
          switchMap(() => {
            const refreshed = auth.getAccessToken();
            return next(
              req.clone({
                setHeaders: { Authorization: `Bearer ${refreshed}` },
              })
            );
          })
        );
      }
      return throwError(() => error);
    })
  );
};
