import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { VanchanBooking, VanchanSummary } from '../../core/models';
import { SessionService } from '../../core/session.service';
import { YagnaApi, errorMessage, todayIso } from '../../core/yagna-api';

@Component({
  selector: 'app-vanchan',
  imports: [FormsModule, DatePipe],
  template: `
    <h1>વાંચન યજ્ઞ</h1>

    @if (summary(); as s) {
      <p class="muted">
        Each booking gives you up to {{ s.pagesPerBooking }} pages to read from one book, in sequence.
        @if (s.remainingPages > 0) {
          {{ s.remainingPages }} of {{ s.totalPages }} pages ({{ s.totalBooks }} books) are still available.
        } @else {
          All pages have been allocated.
        }
      </p>
    }

    <section class="card">
      <h2>Book a vanchan slot</h2>
      <form class="row" (ngSubmit)="book()">
        <label>
          Date
          <input type="date" name="date" [(ngModel)]="date" [min]="today" required />
        </label>
        <button type="submit" class="primary" [disabled]="busy() || !date">Book slot</button>
      </form>
      @if (error()) {
        <p class="error">{{ error() }}</p>
      }
    </section>

    <section class="card">
      <h2>My bookings</h2>
      @if (loading()) {
        <p class="muted">Loading…</p>
      } @else if (bookings().length === 0) {
        <p class="muted">You have not booked any vanchan yet.</p>
      } @else {
        <ul class="list">
          @for (b of bookings(); track b.id) {
            <li>
              <div>
                <strong>{{ b.date | date: 'dd MMM yyyy' }}</strong>
                <div class="book">{{ b.bookTitle }}</div>
                <div class="big">Page {{ b.startPage }} to {{ b.endPage }}</div>
                <small class="muted">{{ b.pageCount }} pages</small>
              </div>
              <div class="actions">
                <span class="badge" [class.done]="b.status === 'COMPLETED'">
                  {{ b.status === 'COMPLETED' ? 'Completed' : 'Booked' }}
                </span>
                @if (b.status === 'BOOKED') {
                  <button type="button" (click)="complete(b)" [disabled]="busy()">Mark completed</button>
                }
              </div>
            </li>
          }
        </ul>
      }
    </section>
  `
})
export class Vanchan implements OnInit {
  private readonly api = inject(YagnaApi);
  private readonly hariId = inject(SessionService).haribhakt()!.id;

  protected readonly today = todayIso();
  protected date = this.today;
  protected readonly summary = signal<VanchanSummary | null>(null);
  protected readonly bookings = signal<VanchanBooking[]>([]);
  protected readonly loading = signal(true);
  protected readonly busy = signal(false);
  protected readonly error = signal('');

  ngOnInit(): void {
    this.refresh();
  }

  protected book(): void {
    this.run(this.api.bookVanchan(this.hariId, this.date));
  }

  protected complete(booking: VanchanBooking): void {
    this.run(this.api.completeVanchan(booking.id, this.hariId));
  }

  private run(request: ReturnType<YagnaApi['bookVanchan']>): void {
    this.busy.set(true);
    this.error.set('');
    request.subscribe({
      next: () => {
        this.busy.set(false);
        this.refresh();
      },
      error: (err) => {
        this.error.set(errorMessage(err));
        this.busy.set(false);
      }
    });
  }

  private refresh(): void {
    this.api.vanchanSummary().subscribe((s) => this.summary.set(s));
    this.api.vanchanBookings(this.hariId).subscribe({
      next: (list) => {
        this.bookings.set(list);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(errorMessage(err));
        this.loading.set(false);
      }
    });
  }
}
