package se.hembibliotek.libris;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import se.hembibliotek.error.ApiException;
import se.hembibliotek.isbn.Isbn;

/** Förhandsvisning av bokdata från Libris innan boken sparas. */
@Path("/api/lookup")
@Produces(MediaType.APPLICATION_JSON)
public class LookupResource {

    @Inject
    LibrisService libris;

    @GET
    @Path("/{isbn}")
    public LibrisBook lookup(@PathParam("isbn") String rawIsbn) {
        String isbn = Isbn.normalize(rawIsbn);
        if (isbn == null) {
            throw ApiException.badRequest("Ogiltigt ISBN: " + rawIsbn);
        }
        return libris.findByIsbn(isbn)
                .orElseThrow(() -> ApiException.notFound("Ingen bok med ISBN " + isbn + " hittades i Libris"));
    }
}
