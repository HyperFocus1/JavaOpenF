package com.open_f.fu.shared.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.open_f.fu.shared.domain.AppException;
import com.open_f.fu.shared.domain.ConflictException;
import com.open_f.fu.shared.domain.DomainException;
import com.open_f.fu.shared.domain.NotFoundException;
import com.open_f.fu.shared.domain.UnauthorizedException;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Un solo formato de error para toda la API.
 */
@Provider
public class ApiExceptionMapper implements ExceptionMapper<RuntimeException> {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionMapper.class);

    @Override
    public Response toResponse(RuntimeException ex) {
        if (ex instanceof WebApplicationException wae) {
            return wae.getResponse(); // 404 de ruta, 405, etc. de Jersey
        }
        if (ex instanceof AppException app) {
            return build(statusOf(app), app.getCode(), app.getMessage());
        }
        log.error("Error no controlado", ex);
        return build(Status.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Error interno");
    }

    private static Status statusOf(AppException ex) {
        if (ex instanceof NotFoundException) return Status.NOT_FOUND;
        if (ex instanceof ConflictException) return Status.CONFLICT;
        if (ex instanceof UnauthorizedException) return Status.UNAUTHORIZED;
        if (ex instanceof DomainException) return Status.BAD_REQUEST;
        return Status.BAD_REQUEST;
    }

    private static Response build(Status status, String code, String message) {
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ApiError(status.getStatusCode(), code, message))
                .build();
    }
}
