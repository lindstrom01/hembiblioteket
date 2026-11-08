package se.hembibliotek.libris;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

public class LibrisMarcParserTest {

    public static final String XML = """
            <?xml version='1.0' encoding='UTF-8'?>
            <xsearch xmlns:marc="http://www.loc.gov/MARC21/slim" records="1"><collection xmlns="http://www.loc.gov/MARC21/slim"><record>
            <controlfield tag="001">22413404</controlfield>
            <controlfield tag="008">180131s2018    sw |||||||||||000 f|swe|d</controlfield>
            <datafield ind1="0" ind2="4" tag="082"><subfield code="a">813.54</subfield></datafield>
            <datafield ind1=" " ind2=" " tag="084"><subfield code="a">Hcee.01</subfield><subfield code="2">kssb/8</subfield></datafield>
            <datafield ind1="1" ind2=" " tag="100"><subfield code="a">Connelly, Michael,</subfield><subfield code="d">1956-</subfield><subfield code="4">aut</subfield></datafield>
            <datafield ind1="1" ind2="0" tag="240"><subfield code="a">The wrong side of goodbye.</subfield></datafield>
            <datafield ind1="1" ind2="0" tag="245"><subfield code="a">Inte ett farväl /</subfield></datafield>
            <datafield ind1=" " ind2=" " tag="250"><subfield code="a">Pocketutgåva</subfield></datafield>
            <datafield ind1=" " ind2="1" tag="264"><subfield code="a">Stockholm :</subfield><subfield code="b">Norstedts,</subfield><subfield code="c">2018</subfield></datafield>
            <datafield ind1=" " ind2="4" tag="264"><subfield code="b">Tryckeri,</subfield><subfield code="c">©2019</subfield></datafield>
            <datafield ind1=" " ind2=" " tag="300"><subfield code="a">371 sidor</subfield></datafield>
            <datafield ind1="1" ind2="4" tag="600"><subfield code="a">Bosch, Harry</subfield></datafield>
            <datafield ind1=" " ind2="7" tag="650"><subfield code="a">Försvunna personer</subfield></datafield>
            <datafield ind1=" " ind2="7" tag="655"><subfield code="a">Deckare</subfield></datafield>
            <datafield ind1=" " ind2="7" tag="655"><subfield code="a">Romaner</subfield></datafield>
            <datafield ind1="1" ind2=" " tag="700"><subfield code="a">Hammarsten, Patrik,</subfield><subfield code="4">trl</subfield></datafield>
            </record></collection></xsearch>""";

    @Test
    void parsesRichMetadata() {
        LibrisBook b = LibrisMarcParser.parse(XML, "9789113084954").orElseThrow();

        assertEquals("Inte ett farväl", b.title());
        assertEquals(List.of("Connelly, Michael"), b.authors());
        assertEquals(List.of("Hammarsten, Patrik"), b.translators());
        assertEquals("The wrong side of goodbye", b.originalTitle());
        assertEquals("Norstedts", b.publisher());
        assertEquals(2018, b.publishedYear());
        assertEquals("Pocketutgåva", b.edition());
        assertEquals(371, b.pages());
        assertEquals("swe", b.language());
        assertEquals(List.of("Deckare", "Romaner"), b.genres());
        assertTrue(b.subjects().containsAll(List.of("Bosch, Harry", "Försvunna personer")));
        assertEquals("813.54", b.deweyClass());
        assertEquals("Hcee.01", b.sabClass());
        assertEquals("http://libris.kb.se/bib/22413404", b.librisUrl());
    }

    @Test
    void emptyResultGivesEmptyOptional() {
        String empty = "<xsearch xmlns:marc=\"http://www.loc.gov/MARC21/slim\" records=\"0\"/>";
        assertTrue(LibrisMarcParser.parse(empty, "1").isEmpty());
    }
}
