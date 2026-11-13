package com.open_f.fu.shared.domain;

/**
 * Se rompió una regla de negocio. La API responde 400.
 */
public class DomainException extends AppException {

    public DomainException(String message) {
        super("VALIDATION_ERROR", message);
    }
}
