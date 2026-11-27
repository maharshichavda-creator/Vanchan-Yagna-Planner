export type BookingStatus = 'BOOKED' | 'COMPLETED';

export interface Haribhakt {
  id: number;
  name: string;
  mandal: string;
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
  pushpNumber: number;
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

export interface VanchanBook {
  id: number;
  sequenceNo: number;
  title: string;
  totalPages: number;
}

/** A haribhakt reading one book of a group; book fields are null for groups created before books were assigned. */
export interface GroupMember {
  name: string;
  bookTitle: string | null;
  bookPages: number | null;
  pagesRead: number;
}

export interface GroupVanchan {
  id: number;
  groupNameId: number;
  groupName: string;
  date: string;
  members: GroupMember[];
  pagesRead: number;
  totalPages: number;
  percentComplete: number;
  status: BookingStatus;
  createdById: number | null;
}

export interface StatusCounts {
  total: number;
  pending: number;
  completed: number;
}

export interface StatusVanchanEntry {
  date: string;
  bookTitle: string;
  startPage: number;
  endPage: number;
  status: BookingStatus;
}

export interface StatusShravanEntry {
  date: string;
  trackNo: number;
  trackTitle: string;
  status: BookingStatus;
}

export interface StatusHaribhakt {
  id: number;
  name: string;
  vanchan: StatusVanchanEntry[];
  shravan: StatusShravanEntry[];
}

export interface StatusMandal {
  mandal: string;
  haribhaktCount: number;
  vanchan: StatusCounts;
  shravan: StatusCounts;
  haribhakts: StatusHaribhakt[];
}

export interface StatusGroup {
  id: number;
  groupName: string;
  date: string;
  members: { name: string; mandal: string | null; bookTitle: string | null; pagesRead: number }[];
  pagesRead: number;
  totalPages: number;
  percentComplete: number;
  status: BookingStatus;
}

export interface AppSettings {
  ebookUrl: string | null;
}

export interface StatusReport {
  summary: {
    mandals: number;
    haribhakts: number;
    vanchan: StatusCounts;
    shravan: StatusCounts;
    groupVanchan: StatusCounts;
  };
  mandals: StatusMandal[];
  groups: StatusGroup[];
}
