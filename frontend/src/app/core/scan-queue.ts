import { HttpErrorResponse } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { switchMap } from 'rxjs';
import { BookInput, emptyBook } from './book.model';
import { BookService } from './book.service';
import { errorMessage } from './error';

export type QueueStatus = 'waiting' | 'searching' | 'found' | 'notFound' | 'error' | 'saving' | 'saved' | 'duplicate';

export interface QueueItem {
  isbn: string;
  status: QueueStatus;
  book: BookInput | null;
  message: string | null;
}

/**
 * Väntekö för inscannade ISBN. Slagningar mot Libris körs med begränsad parallellitet
 * i bakgrunden medan användaren fortsätter skanna.
 */
@Injectable({ providedIn: 'root' })
export class ScanQueue {
  private readonly books = inject(BookService);
  private readonly maxConcurrent = 2;

  readonly items = signal<QueueItem[]>([]);
  readonly autoSave = signal(false);

  readonly lookupsPending = computed(
    () => this.items().filter((i) => i.status === 'waiting' || i.status === 'searching').length,
  );
  readonly readyToSave = computed(() => this.items().filter((i) => i.status === 'found'));
  readonly finished = computed(() => this.items().filter((i) => i.status === 'saved' || i.status === 'duplicate'));

  /** Returnerar false om ISBN redan finns i kön. */
  add(isbn: string): boolean {
    if (this.items().some((i) => i.isbn === isbn)) {
      return false;
    }
    this.items.update((list) => [{ isbn, status: 'waiting', book: null, message: null }, ...list]);
    this.pump();
    return true;
  }

  retry(isbn: string): void {
    this.patch(isbn, { status: 'waiting', message: null });
    this.pump();
  }

  remove(isbn: string): void {
    this.items.update((list) => list.filter((i) => i.isbn !== isbn));
  }

  clearFinished(): void {
    this.items.update((list) => list.filter((i) => i.status !== 'saved' && i.status !== 'duplicate'));
  }

  /** Sparar en (eventuellt redigerad) bok. */
  save(isbn: string, book: BookInput): void {
    this.patch(isbn, { status: 'saving', book, message: null });
    this.books.create(book).subscribe({
      next: () => this.patch(isbn, { status: 'saved', message: null }),
      error: (err: unknown) => {
        if (err instanceof HttpErrorResponse && err.status === 409) {
          this.patch(isbn, { status: 'duplicate', message: 'Finns redan i biblioteket' });
        } else {
          this.patch(isbn, { status: 'found', message: errorMessage(err) });
        }
      },
    });
  }

  saveAllReady(): void {
    for (const item of this.readyToSave()) {
      if (item.book && item.book.title.trim()) {
        this.save(item.isbn, item.book);
      }
    }
  }

  private pump(): void {
    while (this.items().filter((i) => i.status === 'searching').length < this.maxConcurrent) {
      const next = [...this.items()].reverse().find((i) => i.status === 'waiting');
      if (!next) {
        return;
      }
      this.process(next.isbn);
    }
  }

  private process(isbn: string): void {
    this.patch(isbn, { status: 'searching' });
    this.books
      .list(isbn, 0, 1)
      .pipe(
        switchMap((existing) => {
          if (existing.totalItems > 0) {
            throw new HttpErrorResponse({ status: 409 });
          }
          return this.books.lookup(isbn);
        }),
      )
      .subscribe({
        next: (found) => {
          this.patch(isbn, { status: 'found', book: { ...found, notes: null }, message: null });
          if (this.autoSave()) {
            this.save(isbn, { ...found, notes: null });
          }
          this.pump();
        },
        error: (err: unknown) => {
          const status = err instanceof HttpErrorResponse ? err.status : 0;
          if (status === 409) {
            this.patch(isbn, { status: 'duplicate', message: 'Finns redan i biblioteket' });
          } else if (status === 404) {
            this.patch(isbn, {
              status: 'notFound',
              book: emptyBook(isbn),
              message: 'Hittades inte i Libris – fyll i manuellt',
            });
          } else {
            this.patch(isbn, { status: 'error', message: errorMessage(err) });
          }
          this.pump();
        },
      });
  }

  private patch(isbn: string, changes: Partial<QueueItem>): void {
    this.items.update((list) => list.map((i) => (i.isbn === isbn ? { ...i, ...changes } : i)));
  }
}
