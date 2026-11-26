import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { GroupName, GroupVanchan } from '../../core/models';
import { SessionService } from '../../core/session.service';
import { YagnaApi, errorMessage, todayIso } from '../../core/yagna-api';

@Component({
  selector: 'app-group-vanchan',
  imports: [FormsModule, DatePipe],
  template: `
    <h1>ગ્રુપ વાંચન યજ્ઞ</h1>

    <section class="card">
      <h2>Create a vanchan group</h2>
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

        <label>
          Haribhakt names
          <div class="row">
            <input name="member" [(ngModel)]="memberName" placeholder="Enter a name and press Add" maxlength="120"
                   (keydown.enter)="addMember($event)" />
            <button type="button" (click)="addMember()" [disabled]="!memberName.trim()">Add</button>
          </div>
        </label>
        <div class="chips">
          @for (m of members(); track $index) {
            <span class="chip">{{ m }} <button type="button" class="link" (click)="removeMember($index)" aria-label="Remove">×</button></span>
          }
        </div>

        @if (!loading() && groupNames().length === 0) {
          <p class="muted">All group names have already been used.</p>
        }
        @if (error()) {
          <p class="error">{{ error() }}</p>
        }
        <button type="submit" class="primary" [disabled]="busy() || !groupNameId || members().length === 0">Create group</button>
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
                <strong>{{ g.groupName }}</strong>
                <small class="muted"> · {{ g.date | date: 'dd MMM yyyy' }}</small>
                <div class="chips">
                  @for (m of g.members; track $index) {
                    <span class="chip">{{ m }}</span>
                  }
                </div>
              </div>
              <div class="actions">
                <span class="badge" [class.done]="g.status === 'COMPLETED'">
                  {{ g.status === 'COMPLETED' ? 'Completed' : 'In progress' }}
                </span>
                @if (g.status === 'BOOKED') {
                  <button type="button" (click)="complete(g)" [disabled]="busy()">Mark completed</button>
                }
              </div>
            </li>
          }
        </ul>
      }
    </section>
  `
})
export class GroupVanchanPage implements OnInit {
  private readonly api = inject(YagnaApi);
  protected readonly session = inject(SessionService);

  protected readonly groupNames = signal<GroupName[]>([]);
  protected readonly groups = signal<GroupVanchan[]>([]);
  protected readonly members = signal<string[]>([]);
  protected readonly loading = signal(true);
  protected readonly busy = signal(false);
  protected readonly error = signal('');

  protected groupNameId: number | null = null;
  protected date = todayIso();
  protected memberName = '';

  ngOnInit(): void {
    const me = this.session.haribhakt();
    if (me) {
      this.members.set([me.name]);
    }
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

  protected addMember(event?: Event): void {
    event?.preventDefault();
    const name = this.memberName.trim();
    if (name && !this.members().includes(name)) {
      this.members.update((list) => [...list, name]);
    }
    this.memberName = '';
  }

  protected removeMember(index: number): void {
    this.members.update((list) => list.filter((_, i) => i !== index));
  }

  protected create(): void {
    if (this.groupNameId === null) {
      return;
    }
    this.busy.set(true);
    this.error.set('');
    this.api.createGroupVanchan(this.groupNameId, this.date, this.members()).subscribe({
      next: () => {
        this.busy.set(false);
        this.groupNameId = null;
        this.members.set(this.session.haribhakt() ? [this.session.haribhakt()!.name] : []);
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

  protected complete(group: GroupVanchan): void {
    this.busy.set(true);
    this.error.set('');
    this.api.completeGroupVanchan(group.id).subscribe({
      next: () => {
        this.busy.set(false);
        this.loadGroups();
      },
      error: (err) => {
        this.error.set(errorMessage(err));
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
