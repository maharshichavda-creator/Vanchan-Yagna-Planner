import { Injectable, computed, signal } from '@angular/core';
import { Haribhakt } from './models';

const STORAGE_KEY = 'psm-yagna-haribhakt';

/** Remembers which haribhakt is using this browser so they don't have to sign in on every visit. */
@Injectable({ providedIn: 'root' })
export class SessionService {
  private readonly current = signal<Haribhakt | null>(this.load());

  readonly haribhakt = this.current.asReadonly();
  readonly isSignedIn = computed(() => this.current() !== null);

  signIn(haribhakt: Haribhakt): void {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(haribhakt));
    this.current.set(haribhakt);
  }

  signOut(): void {
    localStorage.removeItem(STORAGE_KEY);
    this.current.set(null);
  }

  private load(): Haribhakt | null {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      const stored = raw ? (JSON.parse(raw) as Partial<Haribhakt>) : null;
      // Sessions saved before name/mandal/BAPS ID sign-in lack some of these fields and must sign in again.
      return stored?.id && stored.name && stored.mandal && stored.bapsId ? (stored as Haribhakt) : null;
    } catch {
      return null;
    }
  }
}
