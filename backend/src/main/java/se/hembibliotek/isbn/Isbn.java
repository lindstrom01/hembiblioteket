package se.hembibliotek.isbn;

/** Normalisering och validering av ISBN-10/ISBN-13. Alla giltiga ISBN returneras som ISBN-13. */
public final class Isbn {

    private Isbn() {
    }

    /** Returnerar ISBN-13 utan bindestreck, eller null om värdet inte är ett giltigt ISBN. */
    public static String normalize(String raw) {
        if (raw == null) {
            return null;
        }
        String digits = raw.replaceAll("[\\s-]", "").toUpperCase();
        if (digits.matches("\\d{13}")) {
            return validIsbn13(digits) ? digits : null;
        }
        if (digits.matches("\\d{9}[\\dX]")) {
            return validIsbn10(digits) ? toIsbn13(digits) : null;
        }
        return null;
    }

    private static boolean validIsbn13(String isbn) {
        return checkDigit13(isbn.substring(0, 12)) == isbn.charAt(12) - '0';
    }

    private static boolean validIsbn10(String isbn) {
        int sum = 0;
        for (int i = 0; i < 10; i++) {
            char c = isbn.charAt(i);
            int value = c == 'X' ? 10 : c - '0';
            sum += value * (10 - i);
        }
        return sum % 11 == 0;
    }

    private static String toIsbn13(String isbn10) {
        String base = "978" + isbn10.substring(0, 9);
        return base + checkDigit13(base);
    }

    private static int checkDigit13(String first12) {
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            sum += (first12.charAt(i) - '0') * (i % 2 == 0 ? 1 : 3);
        }
        return (10 - sum % 10) % 10;
    }
}
