import { DatePipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { GroupName, GroupVanchan, VanchanBook } from '../../core/models';
import { CelebrationService } from '../../core/celebration.service';
import { YagnaApi, errorMessage, todayIso } from '../../core/yagna-api';

@Component({
  selector: 'app-group-vanchan',
  imports: [FormsModule, DatePipe],
  template: `
    <h1>ગ્રુપ વાંચન યજ્ઞ</h1>

    <section class="card">
      <h2>Create a vanchan group</h2>
      @if (ebookUrl(); as url) {
        <p class="ebook"><a [href]="url" target="_blank" rel="noopener noreferrer">eBook</a></p>
      }
      <form (ngSubmit)="create()">
        <div class="row">
          <label>
            Group name
            <select name="group" [(ngModel)]="groupNameId" required>
              <option [ngValue]="null" disabled>Select a group</option>
              @for (g of groupNames(); track g.id) {
                <option [ngValue]="g.id">{{ g.name }}</option>
              }
            </select>
          </label>
          <label>
            Date
            <input type="date" name="date" [(ngModel)]="date" required />
          </label>
        </div>

        <div class="book-assign">
          <div class="assign-head">Haribhakt for each book</div>
          @for (b of books(); track b.id) {
            <label class="book-row">
              <span class="book-title">{{ b.title }} <small class="muted">· {{ b.totalPages }} pages</small></span>
              <input [name]="'book' + b.id" [ngModel]="assigned()[b.id] ?? ''" (ngModelChange)="setAssigned(b.id, $event)"
                     placeholder="Haribhakt name" maxlength="120" autocomplete="off" />
            </label>
          }
        </div>

        @if (!loading() && groupNames().length === 0) {
          <p class="muted">All group names have already been used.</p>
        }
        @if (error()) {
          <p class="error">{{ error() }}</p>
        }
        <button type="submit" class="primary" [disabled]="busy() || !groupNameId || !allAssigned()">Create group</button>
      </form>
    </section>

    <section class="card">
      <h2>Vanchan groups</h2>
      @if (loading()) {
        <p class="muted">Loading…</p>
      } @else if (groups().length === 0) {
        <p class="muted">No vanchan groups yet.</p>
      } @else {
        <ul class="list">
          @for (g of groups(); track g.id) {
            <li>
              <div>
                <button type="button" class="link group-link" (click)="openProgress(g)">{{ g.groupName }}</button>
                <span class="badge pct">{{ g.percentComplete }}% read</span>
                <small class="muted"> · {{ g.date | date: 'dd MMM yyyy' }}</small>
                <div class="muted pages-line">{{ g.pagesRead }} of {{ g.totalPages }} pages read</div>
                <div class="chips">
                  @for (m of g.members; track $index) {
                    <span class="chip">
                      @if (m.bookTitle) {
                        <b>{{ shortBook(m.bookTitle) }}</b> ·
                      }
                      {{ m.name }}
                    </span>
                  }
                </div>
              </div>
              <div class="actions">
                <span class="badge" [class.done]="g.status === 'COMPLETED'">
                  {{ g.status === 'COMPLETED' ? 'Completed' : 'In progress' }}
                </span>
              </div>
            </li>
          }
        </ul>
      }
    </section>

    @if (selected(); as g) {
      <div class="modal-backdrop" (click)="closeProgress()">
        <div class="modal" role="dialog" aria-modal="true" (click)="$event.stopPropagation()">
          <h2>{{ g.groupName }}</h2>
          <p class="muted">Enter the number of pages read by each haribhakt in their book.</p>
          <form (ngSubmit)="saveProgress()" novalidate>
            @for (m of g.members; track $index) {
              <label class="member-row">
                <span>
                  @if (m.bookTitle) {
                    <b>{{ shortBook(m.bookTitle) }}</b><br />
                  }
                  {{ m.name }}
                  @if (m.bookPages) {
                    <small class="muted"> · up to {{ m.bookPages }} pages</small>
                  }
                </span>
                <input type="number" min="0" step="1" inputmode="numeric" [name]="'pages' + $index"
                       [max]="m.bookPages ?? g.totalPages" [ngModel]="pages()[$index]" (ngModelChange)="setPages($index, $event)" />
              </label>
            }
            <div class="progress-summary">
              <strong>{{ totalRead() }}</strong> of {{ g.totalPages }} pages read ({{ livePercent() }}%)
              <div class="progress-bar"><div [style.width.%]="livePercent()"></div></div>
            </div>
            @if (modalError()) {
              <p class="error">{{ modalError() }}</p>
            }
            <div class="actions modal-actions">
              <button type="button" (click)="closeProgress()">Cancel</button>
              <button type="submit" class="primary" [disabled]="busy() || tooMany()">Save</button>
            </div>
          </form>
        </div>
      </div>
    }
  `
})
export class GroupVanchanPage implements OnInit {
  private readonly api = inject(YagnaApi);
  private readonly celebration = inject(CelebrationService);

  protected readonly groupNames = signal<GroupName[]>([]);
  protected readonly groups = signal<GroupVanchan[]>([]);
  protected readonly books = signal<VanchanBook[]>([]);
  // Book id -> name of the haribhakt who reads it in the new group.
  protected readonly assigned = signal<Record<number, string>>({});
  protected readonly allAssigned = computed(() => {
    const names = this.assigned();
    return this.books().length > 0 && this.books().every((b) => (names[b.id] ?? '').trim().length > 0);
  });
  protected readonly ebookUrl = signal<string | null>(null);
  protected readonly selected = signal<GroupVanchan | null>(null);
  protected readonly pages = signal<number[]>([]);
  protected readonly modalError = signal('');
  protected readonly totalRead = computed(() => this.pages().reduce((sum, n) => sum + n, 0));
  // True when the total, or any haribhakt's pages, is more than the books hold.
  protected readonly tooMany = computed(() => {
    const group = this.selected();
    return (
      !!group &&
      (this.totalRead() > group.totalPages ||
        group.members.some((m, i) => m.bookPages !== null && (this.pages()[i] ?? 0) > m.bookPages))
    );
  });
  protected readonly livePercent = computed(() => {
    const total = this.selected()?.totalPages ?? 0;
    return total > 0 ? Math.min(100, Math.round((this.totalRead() * 1000) / total) / 10) : 0;
  });
  protected readonly loading = signal(true);
  protected readonly busy = signal(false);
  protected readonly error = signal('');

  protected groupNameId: number | null = null;
  protected date = todayIso();

  ngOnInit(): void {
    this.api.settings().subscribe((s) => this.ebookUrl.set(s.ebookUrl));
    this.api.groupBooks().subscribe({
      next: (list) => this.books.set(list),
      error: (err) => this.error.set(errorMessage(err))
    });
    this.loadGroupNames();
    this.loadGroups();
  }

  private loadGroupNames(): void {
    this.api.groupNames().subscribe((names) => {
      this.groupNames.set(names);
      if (this.groupNameId !== null && !names.some((n) => n.id === this.groupNameId)) {
        this.groupNameId = null;
      }
    });
  }

  protected setAssigned(bookId: number, name: string): void {
    this.assigned.update((current) => ({ ...current, [bookId]: name }));
  }

  /** "પ્રમુખસ્વામી મહારાજ જીવનચરિત્ર ભાગ - ૧" -> "ભાગ - ૧"; any other title is shown as it is. */
  protected shortBook(title: string): string {
    return title.match(/ભાગ.*$/)?.[0] ?? title;
  }

  protected create(): void {
    if (this.groupNameId === null || !this.allAssigned()) {
      return;
    }
    const assignments = this.books().map((b) => ({ bookId: b.id, haribhaktName: (this.assigned()[b.id] ?? '').trim() }));
    this.busy.set(true);
    this.error.set('');
    this.api.createGroupVanchan(this.groupNameId, this.date, assignments).subscribe({
      next: () => {
        this.busy.set(false);
        this.groupNameId = null;
        this.assigned.set({});
        this.loadGroupNames();
        this.loadGroups();
      },
      error: (err) => {
        this.error.set(errorMessage(err));
        this.busy.set(false);
        this.loadGroupNames();
      }
    });
  }

  protected openProgress(group: GroupVanchan): void {
    this.modalError.set('');
    this.pages.set(group.members.map((m) => m.pagesRead));
    this.selected.set(group);
  }

  protected closeProgress(): void {
    this.selected.set(null);
  }

  protected setPages(index: number, value: unknown): void {
    const pages = Math.max(0, Math.floor(Number(value) || 0));
    this.pages.update((list) => list.map((p, i) => (i === index ? pages : p)));
  }

  protected saveProgress(): void {
    const group = this.selected();
    if (!group) {
      return;
    }
    this.busy.set(true);
    this.modalError.set('');
    this.api.updateGroupProgress(group.id, this.pages()).subscribe({
      next: (updated) => {
        this.busy.set(false);
        this.selected.set(null);
        // Reaching all pages completes the group automatically: celebrate that moment.
        if (updated.status === 'COMPLETED' && group.status !== 'COMPLETED') {
          this.celebration.petalShower();
        }
        this.loadGroups();
      },
      error: (err) => {
        this.modalError.set(errorMessage(err));
        this.busy.set(false);
      }
    });
  }

  private loadGroups(): void {
    this.api.groupVanchans().subscribe({
      next: (list) => {
        this.groups.set(list);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(errorMessage(err));
        this.loading.set(false);
      }
    });
  }
}
