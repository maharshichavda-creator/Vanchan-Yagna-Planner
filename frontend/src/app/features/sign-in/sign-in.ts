import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Mandal } from '../../core/models';
import { SessionService } from '../../core/session.service';
import { YagnaApi, errorMessage } from '../../core/yagna-api';

@Component({
  selector: 'app-sign-in',
  imports: [FormsModule],
  template: `
    <section class="card narrow">
      <img class="login-image" src="PSM.png" alt="Pramukh Swami Maharaj" />
      <form (ngSubmit)="submit()" novalidate>
        <label>
          <span>Name <span class="req">*</span></span>
          <input name="name" #nameCtl="ngModel" [(ngModel)]="name" required maxlength="120" autocomplete="name" />
          @if ((nameCtl.touched || attempted()) && !name.trim()) {
            <small class="error">Name is required</small>
          }
        </label>
        <label>
          <span>Mandal <span class="req">*</span></span>
          <select name="mandal" #mandalCtl="ngModel" [(ngModel)]="mandal" required>
            <option value="" disabled>Select Mandal</option>
            @for (m of mandals(); track m.id) {
              <option [value]="m.name">{{ m.name }}</option>
            }
          </select>
          @if ((mandalCtl.touched || attempted()) && !mandal.trim()) {
            <small class="error">Mandal is required</small>
          }
        </label>
        @if (error()) {
          <p class="error">{{ error() }}</p>
        }
        <button type="submit" class="primary" [disabled]="busy()">Continue</button>
      </form>
    </section>
  `
})
export class SignIn implements OnInit {
  private readonly api = inject(YagnaApi);
  private readonly session = inject(SessionService);
  private readonly router = inject(Router);

  protected name = '';
  protected mandal = '';
  protected readonly mandals = signal<Mandal[]>([]);
  protected readonly attempted = signal(false);
  protected readonly busy = signal(false);
  protected readonly error = signal('');

  ngOnInit(): void {
    this.api.mandals().subscribe({
      next: (list) => this.mandals.set(list),
      error: (err) => this.error.set(errorMessage(err))
    });
  }

  protected submit(): void {
    this.attempted.set(true);
    if (!this.name.trim() || !this.mandal.trim()) {
      return;
    }
    this.busy.set(true);
    this.error.set('');
    this.api.signIn(this.name.trim(), this.mandal.trim()).subscribe({
      next: (haribhakt) => {
        this.session.signIn(haribhakt);
        this.router.navigateByUrl('/');
      },
      error: (err) => {
        this.error.set(errorMessage(err));
        this.busy.set(false);
      }
    });
  }
}
