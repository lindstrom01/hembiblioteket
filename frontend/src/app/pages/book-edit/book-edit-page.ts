import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Book, BookInput } from '../../core/book.model';
import { BookService } from '../../core/book.service';
import { errorMessage } from '../../core/error';
import { BookForm } from '../../shared/book-form/book-form';

@Component({
  selector: 'app-book-edit-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [BookForm],
  template: `
    <h1>Redigera bok</h1>
    @if (error()) {
      <p class="error" role="alert">{{ error() }}</p>
    }
    @if (book(); as b) {
      <app-book-form [book]="b" [saving]="saving()" (save)="save($event)" />
    }
  `,
})
export class BookEditPage {
  private readonly books = inject(BookService);
  private readonly router = inject(Router);
  private readonly id = Number(inject(ActivatedRoute).snapshot.paramMap.get('id'));

  protected readonly book = signal<Book | null>(null);
  protected readonly saving = signal(false);
  protected readonly error = signal<string | null>(null);

  constructor() {
    this.books.get(this.id).subscribe({
      next: (b) => this.book.set(b),
      error: (err) => this.error.set(errorMessage(err)),
    });
  }

  protected save(input: BookInput): void {
    this.saving.set(true);
    this.books.update(this.id, input).subscribe({
      next: () => void this.router.navigate(['/books']),
      error: (err) => {
        this.saving.set(false);
        this.error.set(errorMessage(err));
      },
    });
  }
}
