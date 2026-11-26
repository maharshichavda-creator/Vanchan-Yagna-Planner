import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { VanchanBooking, VanchanSummary } from '../../core/models';
import { CelebrationService } from '../../core/celebration.service';
import { SessionService } from '../../core/session.service';
import { YagnaApi, errorMessage, gujaratiNumber, todayIso } from '../../core/yagna-api';

@Component({
  selector: 'app-vanchan',
  imports: [FormsModule],
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
      @if (ebookUrl(); as url) {
        <p class="ebook"><a [href]="url" target="_blank" rel="noopener noreferrer">eBook</a></p>
      }
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
                <strong class="pushp">વાંચન પુષ્પ - {{ pushpNumbers().get(b.id) }}</strong>
                <div class="book">{{ b.bookTitle }}</div>
                <div class="big"><strong>Page {{ b.startPage }} to {{ b.endPage }}</strong></div>
                <small class="muted">{{ b.pageCount }} pages</small>
              </div>
              <div class="actions">
                <span class="badge" [class.done]="b.status === 'COMPLETED'">
                  {{ b.status === 'COMPLETED' ? 'Completed' : 'In Progress' }}
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
  private readonly celebration = inject(CelebrationService);
  private readonly hariId = inject(SessionService).haribhakt()!.id;

  protected readonly today = todayIso();
  protected date = this.today;
  protected readonly summary = signal<VanchanSummary | null>(null);
  protected readonly bookings = signal<VanchanBooking[]>([]);
  // Booking id -> its "વાંચન પુષ્પ" number (Gujarati digits), counted from the first booking.
  protected readonly pushpNumbers = signal(new Map<number, string>());
  protected readonly ebookUrl = signal<string | null>(null);
  protected readonly loading = signal(true);
  protected readonly busy = signal(false);
  protected readonly error = signal('');

  ngOnInit(): void {
    this.api.settings().subscribe((s) => this.ebookUrl.set(s.ebookUrl));
    this.refresh();
  }

  protected book(): void {
    this.run(this.api.bookVanchan(this.hariId, this.date));
  }

  protected complete(booking: VanchanBooking): void {
    this.run(this.api.completeVanchan(booking.id, this.hariId), true);
  }

  private run(request: ReturnType<YagnaApi['bookVanchan']>, celebrate = false): void {
    this.busy.set(true);
    this.error.set('');
    request.subscribe({
      next: () => {
        this.busy.set(false);
        if (celebrate) {
          this.celebration.petalShower();
        }
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
        const numbers = new Map<number, string>();
        [...list].sort((a, b) => a.id - b.id).forEach((b, i) => numbers.set(b.id, gujaratiNumber(i + 1)));
        this.pushpNumbers.set(numbers);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(errorMessage(err));
        this.loading.set(false);
      }
    });
  }
}
