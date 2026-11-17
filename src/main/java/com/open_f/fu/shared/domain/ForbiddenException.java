package com.open_f.fu.shared.domain;

/** Usuario autenticado sin acceso al recurso solicitado. */
public class ForbiddenException extends AppException {

    public ForbiddenException(String message) {
        super("FORBIDDEN", message);
    }
}
