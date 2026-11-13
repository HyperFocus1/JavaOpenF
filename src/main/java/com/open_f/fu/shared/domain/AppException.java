package com.open_f.fu.shared.domain;

/**
 * Base de los errores de negocio. El código viaja en la respuesta HTTP (campo "error").
 */
public abstract class AppException extends RuntimeException {

    private final String code;

    protected AppException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
