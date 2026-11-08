package se.hembibliotek.book;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, unique = true, length = 13)
    String isbn;

    @Column(nullable = false, length = 500)
    String title;

    @Column(length = 500)
    String subtitle;

    @Column(name = "original_title", length = 500)
    String originalTitle;

    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @CollectionTable(name = "book_author", joinColumns = @JoinColumn(name = "book_id"))
    @OrderColumn(name = "position")
    @Column(name = "name", nullable = false)
    List<String> authors = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @CollectionTable(name = "book_translator", joinColumns = @JoinColumn(name = "book_id"))
    @OrderColumn(name = "position")
    @Column(name = "name", nullable = false)
    List<String> translators = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @CollectionTable(name = "book_genre", joinColumns = @JoinColumn(name = "book_id"))
    @OrderColumn(name = "position")
    @Column(name = "name", nullable = false)
    List<String> genres = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @CollectionTable(name = "book_subject", joinColumns = @JoinColumn(name = "book_id"))
    @OrderColumn(name = "position")
    @Column(name = "name", nullable = false)
    List<String> subjects = new ArrayList<>();

    String publisher;

    @Column(name = "published_year")
    Integer publishedYear;

    String edition;

    Integer pages;

    String language;

    String series;

    @Column(length = 4000)
    String summary;

    @Column(name = "dewey_class", length = 50)
    String deweyClass;

    @Column(name = "sab_class", length = 50)
    String sabClass;

    @Column(name = "libris_url")
    String librisUrl;

    @Column(length = 2000)
    String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = now();
    }

    private static LocalDateTime now() {
        return LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
    }
}
