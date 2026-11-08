import { ChangeDetectionStrategy, Component, effect, inject, input, output } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { BookInput } from '../../core/book.model';

const lines = (text: string): string[] =>
  text.split('\n').map((l) => l.trim()).filter((l) => l);
const orNull = (text: string): string | null => text.trim() || null;

@Component({
  selector: 'app-book-form',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule],
  templateUrl: './book-form.html',
  styleUrl: './book-form.scss',
})
export class BookForm {
  readonly book = input.required<BookInput>();
  readonly saving = input(false);
  readonly save = output<BookInput>();

  private readonly fb = inject(NonNullableFormBuilder);

  protected readonly form = this.fb.group({
    isbn: ['', [Validators.required, Validators.pattern(/^[\d\s-]{9,17}[\dXx]?$/)]],
    title: ['', [Validators.required, Validators.maxLength(500)]],
    subtitle: [''],
    originalTitle: [''],
    authors: [''],
    translators: [''],
    publisher: [''],
    publishedYear: this.fb.control<number | null>(null, [Validators.min(0), Validators.max(3000)]),
    edition: [''],
    pages: this.fb.control<number | null>(null, [Validators.min(0)]),
    language: [''],
    series: [''],
    genres: [''],
    subjects: [''],
    summary: ['', Validators.maxLength(4000)],
    deweyClass: [''],
    sabClass: [''],
    notes: ['', Validators.maxLength(2000)],
  });

  constructor() {
    effect(() => {
      const b = this.book();
      this.form.reset({
        isbn: b.isbn,
        title: b.title,
        subtitle: b.subtitle ?? '',
        originalTitle: b.originalTitle ?? '',
        authors: b.authors.join('\n'),
        translators: b.translators.join('\n'),
        publisher: b.publisher ?? '',
        publishedYear: b.publishedYear,
        edition: b.edition ?? '',
        pages: b.pages,
        language: b.language ?? '',
        series: b.series ?? '',
        genres: b.genres.join('\n'),
        subjects: b.subjects.join('\n'),
        summary: b.summary ?? '',
        deweyClass: b.deweyClass ?? '',
        sabClass: b.sabClass ?? '',
        notes: b.notes ?? '',
      });
    });
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    this.save.emit({
      isbn: v.isbn.trim(),
      title: v.title.trim(),
      subtitle: orNull(v.subtitle),
      originalTitle: orNull(v.originalTitle),
      authors: lines(v.authors),
      translators: lines(v.translators),
      publisher: orNull(v.publisher),
      publishedYear: v.publishedYear,
      edition: orNull(v.edition),
      pages: v.pages,
      language: orNull(v.language),
      series: orNull(v.series),
      summary: orNull(v.summary),
      genres: lines(v.genres),
      subjects: lines(v.subjects),
      deweyClass: orNull(v.deweyClass),
      sabClass: orNull(v.sabClass),
      librisUrl: this.book().librisUrl,
      notes: orNull(v.notes),
    });
  }
}
