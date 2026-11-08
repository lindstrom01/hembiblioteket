import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { BookInput } from '../../core/book.model';
import { QueueItem, QueueStatus, ScanQueue } from '../../core/scan-queue';
import { BookForm } from '../../shared/book-form/book-form';
import { IsbnScanner } from '../../shared/isbn-scanner/isbn-scanner';

const STATUS_LABELS: Record<QueueStatus, string> = {
  waiting: 'Väntar',
  searching: 'Söker i Libris…',
  found: 'Hittad',
  notFound: 'Saknas i Libris',
  error: 'Fel',
  saving: 'Sparar…',
  saved: 'Sparad',
  duplicate: 'Finns redan',
};

@Component({
  selector: 'app-scan-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, IsbnScanner, BookForm],
  templateUrl: './scan-page.html',
  styleUrl: './scan-page.scss',
})
export class ScanPage {
  protected readonly queue = inject(ScanQueue);

  protected readonly manualIsbn = signal('');
  protected readonly feedback = signal<string | null>(null);
  protected readonly editing = signal<string | null>(null);

  protected label(status: QueueStatus): string {
    return STATUS_LABELS[status];
  }

  protected canEdit(item: QueueItem): boolean {
    return item.book !== null && (item.status === 'found' || item.status === 'notFound');
  }

  protected onDetected(isbn: string): void {
    this.enqueue(isbn);
    navigator.vibrate?.(60);
  }

  protected submitManual(): void {
    const isbn = this.manualIsbn().replace(/[\s-]/g, '');
    if (isbn) {
      this.enqueue(isbn);
      this.manualIsbn.set('');
    }
  }

  protected saveEdited(item: QueueItem, book: BookInput): void {
    this.editing.set(null);
    this.queue.save(item.isbn, book);
  }

  private enqueue(isbn: string): void {
    this.feedback.set(this.queue.add(isbn) ? `Lade till ${isbn}` : `${isbn} finns redan i kön`);
  }
}
