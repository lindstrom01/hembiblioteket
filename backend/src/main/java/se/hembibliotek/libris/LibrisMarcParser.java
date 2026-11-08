package se.hembibliotek.libris;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/** Översätter MARCXML från Libris xsearch till {@link LibrisBook}. */
final class LibrisMarcParser {

    private static final String NS = "http://www.loc.gov/MARC21/slim";
    private static final Pattern NUMBER = Pattern.compile("(\\d{1,5})");
    private static final Pattern YEAR = Pattern.compile("(\\d{4})");
    private static final Pattern TRAILING_PUNCTUATION = Pattern.compile("[\\s/:;,]+$");

    private record Field(String tag, String ind2, List<String[]> subfields) {

        String first(char code) {
            return subfields.stream().filter(s -> s[0].charAt(0) == code).map(s -> s[1]).findFirst().orElse(null);
        }
    }

    private LibrisMarcParser() {
    }

    static Optional<LibrisBook> parse(String xml, String isbn) {
        if (xml == null || xml.isBlank()) {
            return Optional.empty();
        }
        Element record = firstRecord(xml);
        if (record == null) {
            return Optional.empty();
        }
        List<Field> fields = fields(record);
        String controlNumber = controlField(record, "001");
        String f008 = controlField(record, "008");

        String title = clean(first(fields, "245", 'a'), true);
        if (title == null) {
            return Optional.empty();
        }

        List<String> authors = new ArrayList<>();
        List<String> translators = new ArrayList<>();
        for (Field f : fields) {
            boolean main = f.tag().equals("100");
            if (!main && !f.tag().equals("700")) {
                continue;
            }
            String name = clean(f.first('a'), false);
            String role = f.first('4');
            if (name == null) {
                continue;
            }
            if ("trl".equals(role)) {
                translators.add(name);
            } else if (main || role == null || "aut".equals(role)) {
                authors.add(name);
            }
        }

        String publisher = null;
        String year = null;
        for (Field f : fields) {
            if (f.tag().equals("264") || f.tag().equals("260")) {
                // 264 ind2=1 är utgivning; senare 264 (copyright, tryck) ska inte skriva över.
                boolean publication = f.tag().equals("260") || "1".equals(f.ind2());
                if (publication && publisher == null) {
                    publisher = clean(f.first('b'), false);
                }
                if (publication && year == null) {
                    year = f.first('c');
                }
            }
        }

        return Optional.of(new LibrisBook(
                isbn,
                title,
                clean(first(fields, "245", 'b'), true),
                distinct(authors),
                distinct(translators),
                clean(first(fields, "240", 'a'), true),
                publisher,
                year(year != null ? year : f008 != null && f008.length() > 10 ? f008.substring(7, 11) : null),
                clean(first(fields, "250", 'a'), false),
                pages(first(fields, "300", 'a')),
                f008 != null && f008.length() >= 38 ? blankToNull(f008.substring(35, 38)) : null,
                clean(firstOf(fields, List.of("490", "830"), 'a'), false),
                summary(fields),
                subfieldValues(fields, List.of("655"), 'a'),
                subfieldValues(fields, List.of("600", "650", "651"), 'a'),
                clean(first(fields, "082", 'a'), false),
                sab(fields),
                controlNumber == null ? null : "http://libris.kb.se/bib/" + controlNumber));
    }

    private static Element firstRecord(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            Document doc = factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
            NodeList records = doc.getElementsByTagNameNS(NS, "record");
            return records.getLength() == 0 ? null : (Element) records.item(0);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new IllegalStateException("Kunde inte tolka svaret från Libris", e);
        }
    }

    private static List<Field> fields(Element record) {
        List<Field> result = new ArrayList<>();
        NodeList nodes = record.getElementsByTagNameNS(NS, "datafield");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element df = (Element) nodes.item(i);
            List<String[]> subs = new ArrayList<>();
            NodeList sfs = df.getElementsByTagNameNS(NS, "subfield");
            for (int j = 0; j < sfs.getLength(); j++) {
                Element sf = (Element) sfs.item(j);
                String code = sf.getAttribute("code");
                if (!code.isEmpty()) {
                    subs.add(new String[] {code, sf.getTextContent()});
                }
            }
            result.add(new Field(df.getAttribute("tag"), df.getAttribute("ind2"), subs));
        }
        return result;
    }

    private static String controlField(Element record, String tag) {
        NodeList nodes = record.getElementsByTagNameNS(NS, "controlfield");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element e = (Element) nodes.item(i);
            if (tag.equals(e.getAttribute("tag"))) {
                return e.getTextContent().trim();
            }
        }
        return null;
    }

    private static String first(List<Field> fields, String tag, char code) {
        return firstOf(fields, List.of(tag), code);
    }

    private static String firstOf(List<Field> fields, List<String> tags, char code) {
        for (String tag : tags) {
            for (Field f : fields) {
                if (f.tag().equals(tag) && f.first(code) != null) {
                    return f.first(code);
                }
            }
        }
        return null;
    }

    private static List<String> subfieldValues(List<Field> fields, List<String> tags, char code) {
        LinkedHashSet<String> values = new LinkedHashSet<>();
        for (Field f : fields) {
            if (tags.contains(f.tag())) {
                String v = clean(f.first(code), false);
                if (v != null) {
                    values.add(v);
                }
            }
        }
        return List.copyOf(values);
    }

    private static String sab(List<Field> fields) {
        return fields.stream()
                .filter(f -> f.tag().equals("084") && f.first('2') != null && f.first('2').startsWith("kssb"))
                .map(f -> clean(f.first('a'), false))
                .filter(v -> v != null)
                .findFirst().orElse(null);
    }

    private static String summary(List<Field> fields) {
        String joined = String.join(" ", fields.stream()
                .filter(f -> f.tag().equals("520"))
                .map(f -> f.first('a'))
                .filter(v -> v != null && !v.isBlank())
                .toList()).trim();
        return joined.isEmpty() ? null : joined;
    }

    private static List<String> distinct(List<String> values) {
        return List.copyOf(new LinkedHashSet<>(values));
    }

    private static Integer pages(String extent) {
        if (extent == null) {
            return null;
        }
        Matcher m = NUMBER.matcher(extent);
        return m.find() ? Integer.valueOf(m.group(1)) : null;
    }

    private static Integer year(String date) {
        if (date == null) {
            return null;
        }
        Matcher m = YEAR.matcher(date);
        return m.find() ? Integer.valueOf(m.group(1)) : null;
    }

    /** Tar bort avslutande ISBD-skiljetecken, och för titlar även avslutande punkt. */
    private static String clean(String s, boolean title) {
        if (s == null) {
            return null;
        }
        String cleaned = TRAILING_PUNCTUATION.matcher(s.trim()).replaceAll("");
        if (title && cleaned.endsWith(".")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        }
        return blankToNull(cleaned);
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
