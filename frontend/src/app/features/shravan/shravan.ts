import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ShravanBooking, ShravanSummary } from '../../core/models';
import { CelebrationService } from '../../core/celebration.service';
import { SessionService } from '../../core/session.service';
import { YagnaApi, errorMessage } from '../../core/yagna-api';

@Component({
  selector: 'app-shravan',
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
      <div class="card-head">
        <h2>Book a shravan slot</h2>
        @if (ebookUrl(); as url) {
          <span class="ebook"><a [href]="url" target="_blank" rel="noopener noreferrer">eBook</a></span>
        }
      </div>
      <div class="row">
        <button type="button" class="primary" (click)="book()" [disabled]="busy() || loading() || hasOpenBooking()">
          Book slot
        </button>
      </div>
      @if (hasOpenBooking()) {
        <p class="muted">Mark your current slot as completed to book a new one.</p>
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
        <p class="muted">You have not booked any shravan yet.</p>
      } @else {
        <ul class="list">
          @for (b of bookings(); track b.id) {
            <li>
              <div>
                <div class="big">{{ b.track.title }}</div>
                @if (b.track.audioUrl) {
                  <audio controls preload="none" [src]="b.track.audioUrl"></audio>
                }
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
export class Shravan implements OnInit {
  private readonly api = inject(YagnaApi);
  private readonly celebration = inject(CelebrationService);
  private readonly hariId = inject(SessionService).haribhakt()!.id;

  protected readonly summary = signal<ShravanSummary | null>(null);
  protected readonly bookings = signal<ShravanBooking[]>([]);
  protected readonly ebookUrl = signal<string | null>(null);
  protected readonly hasOpenBooking = computed(() => this.bookings().some((b) => b.status === 'BOOKED'));
  protected readonly loading = signal(true);
  protected readonly busy = signal(false);
  protected readonly error = signal('');

  ngOnInit(): void {
    this.api.settings().subscribe((s) => this.ebookUrl.set(s.ebookUrl));
    this.refresh();
  }

  protected book(): void {
    this.run(this.api.bookShravan(this.hariId));
  }

  protected complete(booking: ShravanBooking): void {
    this.run(this.api.completeShravan(booking.id, this.hariId), true);
  }

  private run(request: ReturnType<YagnaApi['bookShravan']>, celebrate = false): void {
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
