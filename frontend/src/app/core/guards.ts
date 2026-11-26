import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { SessionService } from './session.service';

export const signedInGuard: CanActivateFn = () => {
  const router = inject(Router);
  return inject(SessionService).isSignedIn() ? true : router.createUrlTree(['/sign-in']);
};

export const adminGuard: CanActivateFn = () => {
  const router = inject(Router);
  return inject(SessionService).isAdmin() ? true : router.createUrlTree(['/']);
};

export const guestGuard: CanActivateFn = () => {
  const router = inject(Router);
  return inject(SessionService).isSignedIn() ? router.createUrlTree(['/']) : true;
};
