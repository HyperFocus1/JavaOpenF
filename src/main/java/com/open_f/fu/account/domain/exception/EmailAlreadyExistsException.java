package com.open_f.fu.account.domain.exception;

import com.open_f.fu.shared.domain.ConflictException;

public class EmailAlreadyExistsException extends ConflictException {

    public EmailAlreadyExistsException(String email) {
        super("EMAIL_ALREADY_EXISTS", "El correo " + email + " ingresado ya existe");
    }
}
