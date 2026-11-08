package se.hembibliotek.book;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import se.hembibliotek.book.BookDtos.BookRequest;
import se.hembibliotek.book.BookDtos.BookResponse;
import se.hembibliotek.book.BookDtos.PageResponse;
import se.hembibliotek.error.ApiException;
import se.hembibliotek.isbn.Isbn;

@ApplicationScoped
public class BookService {

    @Inject
    BookRepository repository;

    public PageResponse<BookResponse> list(String query, String genre, int page, int size) {
        long total = repository.countSearch(query, genre);
        List<BookResponse> items = repository.search(query, genre, page, size).stream().map(BookResponse::from).toList();
        int totalPages = (int) Math.ceil((double) total / size);
        return new PageResponse<>(items, page, size, total, totalPages);
    }

    public List<String> genres() {
        return repository.distinctGenres();
    }

    public BookResponse get(Long id) {
        return BookResponse.from(find(id));
    }

    @Transactional
    public BookResponse create(BookRequest request) {
        String isbn = requireIsbn(request.isbn());
        if (repository.existsByIsbn(isbn)) {
            throw ApiException.conflict("Boken med ISBN " + isbn + " finns redan i biblioteket");
        }
        Book book = new Book();
        apply(book, request, isbn);
        repository.persist(book);
        return BookResponse.from(book);
    }

    @Transactional
    public BookResponse update(Long id, BookRequest request) {
        Book book = find(id);
        String isbn = requireIsbn(request.isbn());
        if (repository.existsByIsbnAndIdNot(isbn, id)) {
            throw ApiException.conflict("En annan bok med ISBN " + isbn + " finns redan");
        }
        apply(book, request, isbn);
        return BookResponse.from(book);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(find(id));
    }

    private Book find(Long id) {
        return repository.findByIdOptional(id)
                .orElseThrow(() -> ApiException.notFound("Boken " + id + " hittades inte"));
    }

    private static String requireIsbn(String raw) {
        String isbn = Isbn.normalize(raw);
        if (isbn == null) {
            throw ApiException.badRequest("Ogiltigt ISBN: " + raw);
        }
        return isbn;
    }

    private static void apply(Book book, BookRequest r, String isbn) {
        book.isbn = isbn;
        book.title = r.title().trim();
        book.subtitle = blankToNull(r.subtitle());
        book.originalTitle = blankToNull(r.originalTitle());
        book.authors = trimmed(r.authors());
        book.translators = trimmed(r.translators());
        book.genres = trimmed(r.genres());
        book.subjects = trimmed(r.subjects());
        book.publisher = blankToNull(r.publisher());
        book.publishedYear = r.publishedYear();
        book.edition = blankToNull(r.edition());
        book.pages = r.pages();
        book.language = blankToNull(r.language());
        book.series = blankToNull(r.series());
        book.summary = blankToNull(r.summary());
        book.deweyClass = blankToNull(r.deweyClass());
        book.sabClass = blankToNull(r.sabClass());
        book.librisUrl = blankToNull(r.librisUrl());
        book.notes = blankToNull(r.notes());
    }

    private static List<String> trimmed(List<String> values) {
        return values == null ? new ArrayList<>() : new ArrayList<>(values.stream().map(String::trim).toList());
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
