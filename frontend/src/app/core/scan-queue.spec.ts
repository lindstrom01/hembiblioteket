import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { emptyBook } from './book.model';
import { ScanQueue } from './scan-queue';

describe('ScanQueue', () => {
  let queue: ScanQueue;
  let http: HttpTestingController;

  const emptyPage = { items: [], page: 0, size: 1, totalItems: 0, totalPages: 0 };

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    queue = TestBed.inject(ScanQueue);
    http = TestBed.inject(HttpTestingController);
  });

  it('ignores duplicate ISBNs', () => {
    expect(queue.add('9789113084954')).toBe(true);
    expect(queue.add('9789113084954')).toBe(false);
    expect(queue.items().length).toBe(1);
  });

  it('runs at most two lookups at a time and continues as they finish', () => {
    ['9780000000001', '9780000000002', '9780000000003'].forEach((i) => queue.add(i));
    expect(queue.items().filter((i) => i.status === 'searching').length).toBe(2);

    http.expectOne((r) => r.params.get('q') === '9780000000001').flush(emptyPage);
    http.expectOne('/api/lookup/9780000000001').flush({ ...emptyBook('9780000000001'), title: 'A' });

    expect(queue.items().find((i) => i.isbn === '9780000000001')?.status).toBe('found');
    expect(queue.items().find((i) => i.isbn === '9780000000003')?.status).toBe('searching');
  });

  it('marks books already in the library as duplicates without calling Libris', () => {
    queue.add('9780000000001');
    http.expectOne((r) => r.url === '/api/books').flush({ ...emptyPage, totalItems: 1 });
    http.expectNone('/api/lookup/9780000000001');
    expect(queue.items()[0].status).toBe('duplicate');
  });

  it('offers manual entry when Libris has no match', () => {
    queue.add('9780000000001');
    http.expectOne((r) => r.url === '/api/books').flush(emptyPage);
    http.expectOne('/api/lookup/9780000000001').flush({ message: 'x' }, { status: 404, statusText: 'Not Found' });
    expect(queue.items()[0].status).toBe('notFound');
    expect(queue.items()[0].book?.isbn).toBe('9780000000001');
  });
});
