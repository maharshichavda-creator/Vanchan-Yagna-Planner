import { Routes } from '@angular/router';
import { adminGuard, guestGuard, signedInGuard } from './core/guards';

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
  {
    path: 'status-report',
    canActivate: [signedInGuard, adminGuard],
    loadComponent: () => import('./features/status-report/status-report').then((m) => m.StatusReportPage)
  },
  { path: '**', redirectTo: '' }
];
