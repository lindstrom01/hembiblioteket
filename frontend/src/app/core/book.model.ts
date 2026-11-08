export interface BookInput {
  isbn: string;
  title: string;
  subtitle: string | null;
  originalTitle: string | null;
  authors: string[];
  translators: string[];
  publisher: string | null;
  publishedYear: number | null;
  edition: string | null;
  pages: number | null;
  language: string | null;
  series: string | null;
  summary: string | null;
  genres: string[];
  subjects: string[];
  deweyClass: string | null;
  sabClass: string | null;
  librisUrl: string | null;
  notes: string | null;
}

export interface Book extends BookInput {
  id: number;
  createdAt: string;
  updatedAt: string;
}

export type LibrisBook = Omit<BookInput, 'notes'>;

export function emptyBook(isbn: string): BookInput {
  return {
    isbn, title: '', subtitle: null, originalTitle: null, authors: [], translators: [],
    publisher: null, publishedYear: null, edition: null, pages: null, language: null,
    series: null, summary: null, genres: [], subjects: [], deweyClass: null, sabClass: null,
    librisUrl: null, notes: null,
  };
}

export interface Page<T> {
  items: T[];
  page: number;
  size: number;
  totalItems: number;
  totalPages: number;
}

export interface ApiError {
  message: string;
  details: string[];
}
