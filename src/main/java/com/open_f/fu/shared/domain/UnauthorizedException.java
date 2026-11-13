package com.open_f.fu.shared.domain;

/**
 * Credenciales inválidas o ausentes. La API responde 401.
 */
public abstract class UnauthorizedException extends AppException {

    protected UnauthorizedException(String message) {
        super("UNAUTHORIZED", message);
    }
}
