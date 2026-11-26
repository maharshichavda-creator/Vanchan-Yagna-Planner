import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ShravanBooking, ShravanSummary } from '../../core/models';
import { SessionService } from '../../core/session.service';
import { YagnaApi, errorMessage, todayIso } from '../../core/yagna-api';

@Component({
  selector: 'app-shravan',
  imports: [FormsModule, DatePipe],
  template: `
    <h1>શ્રવણ યજ્ઞ</h1>

    @if (summary(); as s) {
      <p class="muted">
        Each booking gives you one audio track to listen to, in sequence.
        @if (s.remainingTracks > 0) {
          {{ s.remainingTracks }} of {{ s.totalTracks }} tracks are still available.
        } @else {
          All audio tracks have been allocated.
        }
      </p>
    }

    <section class="card">
      <h2>Book a shravan slot</h2>
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
        <p class="muted">You have not booked any shravan yet.</p>
      } @else {
        <ul class="list">
          @for (b of bookings(); track b.id) {
            <li>
              <div>
                <strong>{{ b.date | date: 'dd MMM yyyy' }}</strong>
                <div class="big">{{ b.track.title }}</div>
                <small class="muted">Track {{ b.track.sequenceNo }}</small>
                @if (b.track.audioUrl) {
                  <audio controls preload="none" [src]="b.track.audioUrl"></audio>
                }
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
export class Shravan implements OnInit {
  private readonly api = inject(YagnaApi);
  private readonly hariId = inject(SessionService).haribhakt()!.id;

  protected readonly today = todayIso();
  protected date = this.today;
  protected readonly summary = signal<ShravanSummary | null>(null);
  protected readonly bookings = signal<ShravanBooking[]>([]);
  protected readonly loading = signal(true);
  protected readonly busy = signal(false);
  protected readonly error = signal('');

  ngOnInit(): void {
    this.refresh();
  }

  protected book(): void {
    this.run(this.api.bookShravan(this.hariId, this.date));
  }

  protected complete(booking: ShravanBooking): void {
    this.run(this.api.completeShravan(booking.id, this.hariId));
  }

  private run(request: ReturnType<YagnaApi['bookShravan']>): void {
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
    this.api.shravanSummary().subscribe((s) => this.summary.set(s));
    this.api.shravanBookings(this.hariId).subscribe({
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
