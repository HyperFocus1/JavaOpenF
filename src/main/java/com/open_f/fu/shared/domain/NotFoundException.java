package com.open_f.fu.shared.domain;

/**
 * El recurso pedido no existe. La API responde 404.
 */
public abstract class NotFoundException extends AppException {

    protected NotFoundException(String message) {
        super("NOT_FOUND", message);
    }
}
