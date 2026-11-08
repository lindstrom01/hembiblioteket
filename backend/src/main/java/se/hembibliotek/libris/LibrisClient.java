package se.hembibliotek.libris;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "libris")
@Path("/xsearch")
public interface LibrisClient {

    /** Returnerar MARCXML för träffarna. */
    @GET
    @Produces("text/xml")
    String search(@QueryParam("query") String query,
                  @QueryParam("format") String format,
                  @QueryParam("n") int limit);
}
