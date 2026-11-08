package se.hembibliotek.book;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;
import se.hembibliotek.libris.LibrisClient;
import se.hembibliotek.libris.LibrisMarcParserTest;

@QuarkusTest
class BookResourceTest {

    @InjectMock
    @RestClient
    LibrisClient libris;

    @Inject
    ObjectMapper mapper;

    private static final String BOOK = """
            {"isbn":"%s","title":"Inte ett farväl","authors":["Connelly, Michael"],
             "genres":["Deckare"],"pages":371,
             "publisher":"Norstedts","publishedYear":2018,"language":"swe"}""";

    @Test
    void lookupMapsLibrisResponse() {
        when(libris.search(anyString(), anyString(), anyInt())).thenReturn(LibrisMarcParserTest.XML);

        given().when().get("/api/lookup/978-91-130-8495-4")
                .then().statusCode(200)
                .body("title", equalTo("Inte ett farväl"))
                .body("publisher", equalTo("Norstedts"))
                .body("publishedYear", equalTo(2018))
                .body("genres", hasItem("Deckare"))
                .body("authors", hasItem("Connelly, Michael"));
    }

    @Test
    void lookupUnknownIsbnGives404() {
        when(libris.search(anyString(), anyString(), anyInt()))
                .thenReturn("<xsearch xmlns:marc=\"http://www.loc.gov/MARC21/slim\" records=\"0\"/>");

        given().when().get("/api/lookup/9780306406157").then().statusCode(404);
    }

    @Test
    void lookupInvalidIsbnGives400() {
        given().when().get("/api/lookup/123").then().statusCode(400);
    }

    @Test
    void createReadSearchUpdateDelete() {
        String location = given().contentType(ContentType.JSON).body(BOOK.formatted("9780306406157"))
                .when().post("/api/books")
                .then().statusCode(201).body("isbn", equalTo("9780306406157"))
                .extract().header("Location");

        given().contentType(ContentType.JSON).body(BOOK.formatted("9780306406157"))
                .when().post("/api/books").then().statusCode(409);

        given().queryParam("q", "connelly").when().get("/api/books")
                .then().statusCode(200).body("items.title", hasItem("Inte ett farväl"));

        given().queryParam("genre", "Deckare").when().get("/api/books")
                .then().statusCode(200).body("totalItems", equalTo(1));
        given().queryParam("genre", "Fantasy").when().get("/api/books")
                .then().statusCode(200).body("totalItems", equalTo(0));
        given().when().get("/api/books/genres").then().statusCode(200).body("", hasItem("Deckare"));

        given().contentType(ContentType.JSON)
                .body(BOOK.formatted("9780306406157").replace("2018", "2020"))
                .when().put(location).then().statusCode(200).body("publishedYear", equalTo(2020));

        given().when().delete(location).then().statusCode(204);
        given().when().get(location).then().statusCode(404);
    }

    @Test
    void createWithInvalidDataGives400() {
        given().contentType(ContentType.JSON)
                .body("{\"isbn\":\"9780306406157\",\"title\":\"\",\"authors\":[]}")
                .when().post("/api/books")
                .then().statusCode(400).body("message", containsString("Ogiltig"));
    }
}
