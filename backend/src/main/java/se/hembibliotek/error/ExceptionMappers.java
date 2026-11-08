package se.hembibliotek.error;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.List;
import org.eclipse.microprofile.faulttolerance.exceptions.FaultToleranceException;
import org.jboss.logging.Logger;

public class ExceptionMappers {

    public record ErrorResponse(String message, List<String> details) {
    }

    @Provider
    public static class Api implements ExceptionMapper<ApiException> {
        @Override
        public Response toResponse(ApiException e) {
            return Response.status(e.status()).entity(new ErrorResponse(e.getMessage(), List.of())).build();
        }
    }

    @Provider
    public static class Validation implements ExceptionMapper<ConstraintViolationException> {
        @Override
        public Response toResponse(ConstraintViolationException e) {
            List<String> details = e.getConstraintViolations().stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .sorted()
                    .toList();
            return Response.status(400).entity(new ErrorResponse("Ogiltig indata", details)).build();
        }
    }

    /** Libris onåbart eller för långsamt. */
    @Provider
    public static class Upstream implements ExceptionMapper<FaultToleranceException> {
        private static final Logger LOG = Logger.getLogger(Upstream.class);

        @Override
        public Response toResponse(FaultToleranceException e) {
            LOG.warn("Anrop mot Libris misslyckades", e);
            return Response.status(502)
                    .entity(new ErrorResponse("Libris är inte tillgängligt just nu", List.of()))
                    .build();
        }
    }
}
