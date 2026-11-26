import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { SessionService } from '../../core/session.service';

@Component({
  selector: 'app-home',
  imports: [RouterLink],
  template: `
    <h1>Select a Yagna</h1>
    <div class="tiles">
      <a class="tile" routerLink="/vanchan">
        <h2>વાંચન યજ્ઞ</h2>
        <p>Book a date and read your pages.</p>
      </a>
      <a class="tile" routerLink="/group-vanchan">
        <h2>ગ્રુપ વાંચન યજ્ઞ</h2>
        <p>Create a vanchan group and read all 12 books per group.</p>
      </a>
      <a class="tile" routerLink="/shravan">
        <h2>શ્રવણ યજ્ઞ</h2>
        <p>Book a date and listen to your audio track.</p>
      </a>
      @if (session.isAdmin()) {
        <a class="tile" routerLink="/status-report">
          <h2>Status Report</h2>
          <p>Mandal-wise progress of all yagnas.</p>
        </a>
      }
    </div>
  `
})
export class Home {
  protected readonly session = inject(SessionService);
}
