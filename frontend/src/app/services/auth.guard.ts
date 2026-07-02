import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

// this is the bouncer on certain pages.
// before angular shows a protected page, it asks this: is the user logged in?
// if yes they walk right in, if no they get sent to the login page instead.
export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (auth.isLoggedIn()) {
    return true;             // let them in
  }
  router.navigate(['/login']);  // otherwise send to login
  return false;
};