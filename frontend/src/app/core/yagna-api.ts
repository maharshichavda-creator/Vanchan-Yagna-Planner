import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  GroupName,
  GroupVanchan,
  Haribhakt,
  Mandal,
  ShravanBooking,
  ShravanSummary,
  VanchanBooking,
  VanchanSummary
} from './models';

@Injectable({ providedIn: 'root' })
export class YagnaApi {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiBaseUrl;

  mandals(): Observable<Mandal[]> {
    return this.http.get<Mandal[]>(`${this.base}/mandals`);
  }

  signIn(name: string, mandal: string, bapsId: string): Observable<Haribhakt> {
    return this.http.post<Haribhakt>(`${this.base}/haribhakt/sign-in`, { name, mandal, bapsId });
  }

  // Vanchan Yagna
  vanchanSummary(): Observable<VanchanSummary> {
    return this.http.get<VanchanSummary>(`${this.base}/vanchan/summary`);
  }

  vanchanBookings(haribhaktId: number): Observable<VanchanBooking[]> {
    const params = new HttpParams().set('haribhaktId', haribhaktId);
    return this.http.get<VanchanBooking[]>(`${this.base}/vanchan/bookings`, { params });
  }

  bookVanchan(haribhaktId: number, date: string): Observable<VanchanBooking> {
    return this.http.post<VanchanBooking>(`${this.base}/vanchan/bookings`, { haribhaktId, date });
  }

  completeVanchan(bookingId: number, haribhaktId: number): Observable<VanchanBooking> {
    const params = new HttpParams().set('haribhaktId', haribhaktId);
    return this.http.post<VanchanBooking>(`${this.base}/vanchan/bookings/${bookingId}/complete`, null, { params });
  }

  // Shravan Yagna
  shravanSummary(): Observable<ShravanSummary> {
    return this.http.get<ShravanSummary>(`${this.base}/shravan/summary`);
  }

  shravanBookings(haribhaktId: number): Observable<ShravanBooking[]> {
    const params = new HttpParams().set('haribhaktId', haribhaktId);
    return this.http.get<ShravanBooking[]>(`${this.base}/shravan/bookings`, { params });
  }

  bookShravan(haribhaktId: number, date: string): Observable<ShravanBooking> {
    return this.http.post<ShravanBooking>(`${this.base}/shravan/bookings`, { haribhaktId, date });
  }

  completeShravan(bookingId: number, haribhaktId: number): Observable<ShravanBooking> {
    const params = new HttpParams().set('haribhaktId', haribhaktId);
    return this.http.post<ShravanBooking>(`${this.base}/shravan/bookings/${bookingId}/complete`, null, { params });
  }

  // Group Vanchan Yagna
  groupNames(): Observable<GroupName[]> {
    return this.http.get<GroupName[]>(`${this.base}/group-vanchan/group-names`);
  }

  groupVanchans(): Observable<GroupVanchan[]> {
    return this.http.get<GroupVanchan[]>(`${this.base}/group-vanchan`);
  }

  createGroupVanchan(groupNameId: number, date: string, members: string[]): Observable<GroupVanchan> {
    return this.http.post<GroupVanchan>(`${this.base}/group-vanchan`, { groupNameId, date, members });
  }

  completeGroupVanchan(id: number): Observable<GroupVanchan> {
    return this.http.post<GroupVanchan>(`${this.base}/group-vanchan/${id}/complete`, null);
  }
}

export function errorMessage(err: unknown): string {
  if (err instanceof HttpErrorResponse) {
    if (err.status === 0) {
      return 'Cannot reach the server. Please try again.';
    }
    return err.error?.message ?? 'Something went wrong. Please try again.';
  }
  return 'Something went wrong. Please try again.';
}

/** Today's date as yyyy-MM-dd in the user's local time zone (what <input type="date"> expects). */
export function todayIso(): string {
  const d = new Date();
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
}
