import { HttpInterceptorFn } from '@angular/common/http';

// this quietly attaches our login ticket to every request going out to the backend.
// think of it as a stamp added to each outgoing letter, so the backend knows who is asking.
// if we have no ticket yet (not logged in) the request just goes out plain.
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('token');
  if (token) {
    // make a copy of the request with the ticket added to the header, then send that copy
    const withToken = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });
    return next(withToken);
  }
  return next(req);
};