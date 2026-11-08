import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { BookService } from './book.service';

describe('BookService', () => {
  let service: BookService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(BookService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('sends search query and paging params', () => {
    service.list(' tolkien ', 1, 20).subscribe();
    const req = http.expectOne((r) => r.url === '/api/books');
    expect(req.request.params.get('q')).toBe('tolkien');
    expect(req.request.params.get('page')).toBe('1');
    req.flush({ items: [], page: 1, size: 20, totalItems: 0, totalPages: 0 });
  });

  it('looks up ISBN via the backend', () => {
    service.lookup('9789113084954').subscribe();
    http.expectOne('/api/lookup/9789113084954').flush({});
  });
});
