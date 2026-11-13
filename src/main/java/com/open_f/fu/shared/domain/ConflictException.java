package com.open_f.fu.shared.domain;

/**
 * El recurso choca con uno que ya existe. La API responde 409.
 */
public abstract class ConflictException extends AppException {

    protected ConflictException(String code, String message) {
        super(code, message);
    }
}
