package se.hembibliotek.libris;

import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class LibrisService {

    @Inject
    @RestClient
    LibrisClient client;

    /** Slår upp ett (normaliserat) ISBN-13 i Libris. Tom Optional om ingen träff. */
    @CacheResult(cacheName = "libris-lookup")
    @Retry(maxRetries = 2, delay = 300, delayUnit = ChronoUnit.MILLIS)
    @Timeout(value = 8, unit = ChronoUnit.SECONDS)
    public Optional<LibrisBook> findByIsbn(String isbn13) {
        return LibrisMarcParser.parse(client.search("isbn:" + isbn13, "marcxml", 1), isbn13);
    }
}
