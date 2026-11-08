package se.hembibliotek.isbn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class IsbnTest {

    @Test
    void acceptsValidIsbn13WithHyphens() {
        assertEquals("9789113084954", Isbn.normalize("978-91-130-8495-4"));
    }

    @Test
    void convertsIsbn10ToIsbn13() {
        assertEquals("9780306406157", Isbn.normalize("0-306-40615-2"));
    }

    @Test
    void acceptsIsbn10WithXCheckDigit() {
        assertEquals("9780804429573", Isbn.normalize("080442957x"));
    }

    @Test
    void rejectsBadChecksumAndGarbage() {
        assertNull(Isbn.normalize("9789113084955"));
        assertNull(Isbn.normalize("abc"));
        assertNull(Isbn.normalize(null));
    }
}
