import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { SessionService } from './core/session.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <header class="topbar">
      <div class="topbar-main">
        <a routerLink="/" class="brand">
          <img class="logo" src="PSM.png" alt="PSM logo" />
          <span class="brand-text">
            <span class="temple">BAPS શ્રી સ્વામિનારાયણ મંદિર - પુણે</span>
            <span class="title">પ્રમુખસ્વામી મહારાજ જીવનચરિત્ર વાંચન અને શ્રવણ યજ્ઞ</span>
          </span>
        </a>
        @if (session.haribhakt(); as hari) {
          <div class="user">
            <span>{{ hari.name }} · {{ hari.mandal }}</span>
            <button type="button" class="link" (click)="signOut()">Sign out</button>
          </div>
        }
      </div>
      @if (session.isSignedIn()) {
        <nav class="menu">
          <a routerLink="/vanchan" routerLinkActive="active">વાંચન યજ્ઞ</a>
          <a routerLink="/group-vanchan" routerLinkActive="active">ગ્રુપ વાંચન યજ્ઞ</a>
          <a routerLink="/shravan" routerLinkActive="active">શ્રવણ યજ્ઞ</a>
          @if (session.isAdmin()) {
            <a routerLink="/status-report" routerLinkActive="active">Status Report</a>
          }
        </nav>
      }
    </header>
    <main class="container">
      <router-outlet />
    </main>
  `
})
export class App {
  protected readonly session = inject(SessionService);
  private readonly router = inject(Router);

  protected signOut(): void {
    this.session.signOut();
    this.router.navigateByUrl('/sign-in');
  }
}
