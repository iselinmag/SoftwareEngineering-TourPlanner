import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('token');
  if (token) {
    // clone the request and add the ticket to the header
    const withToken = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });
    return next(withToken);
  }
  return next(req);
};