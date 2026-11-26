import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { StatusReport } from '../../core/models';
import { SessionService } from '../../core/session.service';
import { YagnaApi, errorMessage } from '../../core/yagna-api';

@Component({
  selector: 'app-status-report',
  imports: [DatePipe],
  template: `
    <div class="report-head">
      <h1>Status Report</h1>
      <button type="button" (click)="load()" [disabled]="loading()">Refresh</button>
    </div>

    @if (error()) {
      <p class="error">{{ error() }}</p>
    }

    @if (loading() && !report()) {
      <p class="muted">Loading…</p>
    }

    @if (report(); as r) {
      <div class="widgets">
        <div class="widget">
          <span class="num">{{ r.summary.haribhakts }}</span>
          <span class="label">Haribhakts joined</span>
          <small class="muted">from {{ r.summary.mandals }} mandals</small>
        </div>
        <div class="widget">
          <span class="label">વાંચન યજ્ઞ</span>
          <span class="num">{{ r.summary.vanchan.total }}</span>
          <small>
            <span class="badge done">{{ r.summary.vanchan.completed }} completed</span>
            <span class="badge">{{ r.summary.vanchan.pending }} pending</span>
          </small>
        </div>
        <div class="widget">
          <span class="label">ગ્રુપ વાંચન યજ્ઞ</span>
          <span class="num">{{ r.summary.groupVanchan.total }}</span>
          <small>
            <span class="badge done">{{ r.summary.groupVanchan.completed }} completed</span>
            <span class="badge">{{ r.summary.groupVanchan.pending }} pending</span>
          </small>
        </div>
        <div class="widget">
          <span class="label">શ્રવણ યજ્ઞ</span>
          <span class="num">{{ r.summary.shravan.total }}</span>
          <small>
            <span class="badge done">{{ r.summary.shravan.completed }} completed</span>
            <span class="badge">{{ r.summary.shravan.pending }} pending</span>
          </small>
        </div>
      </div>

      <h2>Mandal-wise status</h2>
      @if (r.mandals.length === 0) {
        <p class="muted">No haribhakt has joined a vanchan or shravan yagna yet.</p>
      }
      @for (m of r.mandals; track m.mandal) {
        <details class="card mandal">
          <summary>
            <strong>{{ m.mandal }}</strong>
            <span class="chip">{{ m.haribhaktCount }} haribhakt</span>
            @if (m.vanchan.total) {
              <span class="chip">વાંચન {{ m.vanchan.completed }}/{{ m.vanchan.total }}</span>
            }
            @if (m.shravan.total) {
              <span class="chip">શ્રવણ {{ m.shravan.completed }}/{{ m.shravan.total }}</span>
            }
          </summary>
          @for (h of m.haribhakts; track h.id) {
            <div class="person">
              <div>
                <strong>{{ h.name }}</strong>
              </div>
              @if (h.vanchan.length) {
                <div class="yagna-title">વાંચન યજ્ઞ</div>
                <ul class="entries">
                  @for (v of h.vanchan; track $index) {
                    <li>
                      <span>
                        {{ v.date | date: 'dd MMM yyyy' }} · {{ v.bookTitle }}, pages {{ v.startPage }}–{{ v.endPage }}
                      </span>
                      <span class="badge" [class.done]="v.status === 'COMPLETED'">
                        {{ v.status === 'COMPLETED' ? 'Completed' : 'Pending' }}
                      </span>
                    </li>
                  }
                </ul>
              }
              @if (h.shravan.length) {
                <div class="yagna-title">શ્રવણ યજ્ઞ</div>
                <ul class="entries">
                  @for (s of h.shravan; track $index) {
                    <li>
                      <span>{{ s.date | date: 'dd MMM yyyy' }} · Track {{ s.trackNo }}: {{ s.trackTitle }}</span>
                      <span class="badge" [class.done]="s.status === 'COMPLETED'">
                        {{ s.status === 'COMPLETED' ? 'Completed' : 'Pending' }}
                      </span>
                    </li>
                  }
                </ul>
              }
            </div>
          }
        </details>
      }

      <h2>ગ્રુપ વાંચન યજ્ઞ</h2>
      @if (r.groups.length === 0) {
        <p class="muted">No group vanchan has been created yet.</p>
      }
      <div class="group-widgets">
        @for (g of r.groups; track g.id) {
          <div class="card group">
            <div class="group-head">
              <strong>{{ g.groupName }} <span class="badge pct">{{ g.percentComplete }}% read</span></strong>
              <span class="badge" [class.done]="g.status === 'COMPLETED'">
                {{ g.status === 'COMPLETED' ? 'Completed' : 'Pending' }}
              </span>
            </div>
            <small class="muted">{{ g.date | date: 'dd MMM yyyy' }} · {{ g.pagesRead }} of {{ g.totalPages }} pages</small>
            <div class="chips">
              @for (member of g.members; track $index) {
                <span class="chip">{{ member.name }}@if (member.mandal) { · {{ member.mandal }} } · {{ member.pagesRead }} pages</span>
              }
            </div>
          </div>
        }
      </div>
      <p class="muted note">
        Group members are saved by name only; a mandal is shown when the name matches one registered haribhakt.
      </p>
    }
  `,
  styles: `
    .report-head {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    .widgets {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(170px, 1fr));
      gap: 0.75rem;
      margin-bottom: 1.25rem;
    }
    .widget {
      display: flex;
      flex-direction: column;
      gap: 0.25rem;
      background: #fff;
      border: 1px solid var(--border);
      border-top: 4px solid var(--accent);
      border-radius: 10px;
      padding: 0.9rem 1rem;
    }
    .widget .num {
      font-size: 1.9rem;
      font-weight: 700;
      color: var(--accent-dark);
    }
    .widget .label {
      color: var(--muted);
      font-size: 0.9rem;
    }
    .widget small {
      display: flex;
      flex-wrap: wrap;
      gap: 0.3rem;
    }
    .mandal summary {
      cursor: pointer;
      display: flex;
      flex-wrap: wrap;
      align-items: center;
      gap: 0.5rem;
    }
    .person {
      border-top: 1px solid var(--border);
      margin-top: 0.75rem;
      padding-top: 0.6rem;
    }
    .yagna-title {
      color: var(--accent-dark);
      font-weight: 600;
      margin-top: 0.4rem;
      font-size: 0.9rem;
    }
    .entries {
      list-style: none;
      margin: 0.2rem 0 0;
      padding: 0;
    }
    .entries li {
      display: flex;
      justify-content: space-between;
      flex-wrap: wrap;
      gap: 0.5rem;
      padding: 0.2rem 0;
      font-size: 0.92rem;
    }
    .group-widgets {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
      gap: 0.75rem;
    }
    .group {
      margin: 0;
    }
    .group-head {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 0.5rem;
    }
    .note {
      font-size: 0.85rem;
    }
  `
})
export class StatusReportPage implements OnInit {
  private readonly api = inject(YagnaApi);
  private readonly hariId = inject(SessionService).haribhakt()!.id;

  protected readonly report = signal<StatusReport | null>(null);
  protected readonly loading = signal(false);
  protected readonly error = signal('');

  ngOnInit(): void {
    this.load();
  }

  protected load(): void {
    this.loading.set(true);
    this.error.set('');
    this.api.statusReport(this.hariId).subscribe({
      next: (r) => {
        this.report.set(r);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(errorMessage(err));
        this.loading.set(false);
      }
    });
  }
}
