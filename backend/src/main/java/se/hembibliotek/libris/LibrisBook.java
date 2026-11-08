package se.hembibliotek.libris;

import java.util.List;

/** Bokdata hämtad från Libris (MARC21). */
public record LibrisBook(
        String isbn,
        String title,
        String subtitle,
        List<String> authors,
        List<String> translators,
        String originalTitle,
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
        String librisUrl) {
}
