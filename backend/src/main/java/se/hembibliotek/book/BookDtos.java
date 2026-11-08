package se.hembibliotek.book;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

public final class BookDtos {

    private BookDtos() {
    }

    /** Indata för att skapa/uppdatera en bok. ISBN kan anges med eller utan bindestreck. */
    public record BookRequest(
            @NotBlank String isbn,
            @NotBlank @Size(max = 500) String title,
            @Size(max = 500) String subtitle,
            @Size(max = 500) String originalTitle,
            @NotNull @Size(max = 50) List<@NotBlank @Size(max = 255) String> authors,
            @Size(max = 50) List<@NotBlank @Size(max = 255) String> translators,
            @Size(max = 255) String publisher,
            @Min(0) @Max(3000) Integer publishedYear,
            @Size(max = 255) String edition,
            @Min(0) @Max(100000) Integer pages,
            @Size(max = 10) String language,
            @Size(max = 255) String series,
            @Size(max = 4000) String summary,
            @Size(max = 50) List<@NotBlank @Size(max = 255) String> genres,
            @Size(max = 100) List<@NotBlank @Size(max = 255) String> subjects,
            @Size(max = 50) String deweyClass,
            @Size(max = 50) String sabClass,
            @Size(max = 500) String librisUrl,
            @Size(max = 2000) String notes) {
    }

    public record BookResponse(
            Long id,
            String isbn,
            String title,
            String subtitle,
            String originalTitle,
            List<String> authors,
            List<String> translators,
            String publisher,
            Integer publishedYear,
            String edition,
            Integer pages,
            String language,
            String series,
            String summary,
            List<String> genres,
            List<String> subjects,
            String deweyClass,
            String sabClass,
            String librisUrl,
            String notes,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {

        static BookResponse from(Book b) {
            return new BookResponse(b.id, b.isbn, b.title, b.subtitle, b.originalTitle,
                    List.copyOf(b.authors), List.copyOf(b.translators), b.publisher, b.publishedYear,
                    b.edition, b.pages, b.language, b.series, b.summary,
                    List.copyOf(b.genres), List.copyOf(b.subjects), b.deweyClass, b.sabClass,
                    b.librisUrl, b.notes, b.createdAt, b.updatedAt);
        }
    }

    public record PageResponse<T>(List<T> items, int page, int size, long totalItems, int totalPages) {
    }
}
