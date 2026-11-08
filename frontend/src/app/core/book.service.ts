import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Book, BookInput, LibrisBook, Page } from './book.model';

@Injectable({ providedIn: 'root' })
export class BookService {
  private readonly http = inject(HttpClient);

  list(query: string, page: number, size: number, genre = ''): Observable<Page<Book>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (query.trim()) {
      params = params.set('q', query.trim());
    }
    if (genre) {
      params = params.set('genre', genre);
    }
    return this.http.get<Page<Book>>('/api/books', { params });
  }

  genres(): Observable<string[]> {
    return this.http.get<string[]>('/api/books/genres');
  }

  get(id: number): Observable<Book> {
    return this.http.get<Book>(`/api/books/${id}`);
  }

  create(book: BookInput): Observable<Book> {
    return this.http.post<Book>('/api/books', book);
  }

  update(id: number, book: BookInput): Observable<Book> {
    return this.http.put<Book>(`/api/books/${id}`, book);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`/api/books/${id}`);
  }

  lookup(isbn: string): Observable<LibrisBook> {
    return this.http.get<LibrisBook>(`/api/lookup/${encodeURIComponent(isbn)}`);
  }
}
