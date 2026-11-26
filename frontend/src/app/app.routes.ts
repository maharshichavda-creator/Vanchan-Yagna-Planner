import { Routes } from '@angular/router';
import { guestGuard, signedInGuard } from './core/guards';

export const routes: Routes = [
  {
    path: 'sign-in',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/sign-in/sign-in').then((m) => m.SignIn)
  },
  {
    path: '',
    pathMatch: 'full',
    canActivate: [signedInGuard],
    loadComponent: () => import('./features/home/home').then((m) => m.Home)
  },
  {
    path: 'vanchan',
    canActivate: [signedInGuard],
    loadComponent: () => import('./features/vanchan/vanchan').then((m) => m.Vanchan)
  },
  {
    path: 'group-vanchan',
    canActivate: [signedInGuard],
    loadComponent: () => import('./features/group-vanchan/group-vanchan').then((m) => m.GroupVanchanPage)
  },
  {
    path: 'shravan',
    canActivate: [signedInGuard],
    loadComponent: () => import('./features/shravan/shravan').then((m) => m.Shravan)
  },
  { path: '**', redirectTo: '' }
];
