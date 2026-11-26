export type BookingStatus = 'BOOKED' | 'COMPLETED';

export interface Haribhakt {
  id: number;
  name: string;
  mandal: string;
  bapsId: string;
}

export interface Mandal {
  id: number;
  name: string;
}

export interface VanchanSummary {
  pagesPerBooking: number;
  totalBooks: number;
  totalPages: number;
  allocatedPages: number;
  remainingPages: number;
  nextBookTitle: string | null;
  nextStartPage: number | null;
}

export interface VanchanBooking {
  id: number;
  date: string;
  bookId: number;
  bookTitle: string;
  startPage: number;
  endPage: number;
  pageCount: number;
  status: BookingStatus;
}

export interface ShravanTrack {
  id: number;
  sequenceNo: number;
  title: string;
  audioUrl: string | null;
}

export interface ShravanSummary {
  totalTracks: number;
  remainingTracks: number;
}

export interface ShravanBooking {
  id: number;
  date: string;
  track: ShravanTrack;
  status: BookingStatus;
}

export interface GroupName {
  id: number;
  name: string;
}

export interface GroupVanchan {
  id: number;
  groupNameId: number;
  groupName: string;
  date: string;
  members: string[];
  status: BookingStatus;
}
