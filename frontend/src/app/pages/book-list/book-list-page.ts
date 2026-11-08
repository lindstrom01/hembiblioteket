import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Book, Page } from '../../core/book.model';
import { BookService } from '../../core/book.service';
import { errorMessage } from '../../core/error';

@Component({
  selector: 'app-book-list-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, RouterLink],
  templateUrl: './book-list-page.html',
  styleUrl: './book-list-page.scss',
})
export class BookListPage {
  private readonly books = inject(BookService);
  private readonly pageSize = 20;

  protected readonly query = signal('');
  protected readonly genre = signal('');
  protected readonly genres = signal<string[]>([]);
  protected readonly result = signal<Page<Book> | null>(null);
  protected readonly error = signal<string | null>(null);

  constructor() {
    this.books.genres().subscribe({ next: (g) => this.genres.set(g) });
    this.load(0);
  }

  protected search(): void {
    this.load(0);
  }

  protected load(page: number): void {
    this.books.list(this.query(), page, this.pageSize, this.genre()).subscribe({
      next: (r) => {
        this.error.set(null);
        this.result.set(r);
      },
      error: (err) => this.error.set(errorMessage(err)),
    });
  }

  protected remove(book: Book): void {
    if (!confirm(`Ta bort "${book.title}"?`)) {
      return;
    }
    this.books.delete(book.id).subscribe({
      next: () => this.load(this.result()?.page ?? 0),
      error: (err) => this.error.set(errorMessage(err)),
    });
  }
}
