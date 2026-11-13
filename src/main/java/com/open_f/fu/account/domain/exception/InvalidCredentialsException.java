package com.open_f.fu.account.domain.exception;

import com.open_f.fu.shared.domain.UnauthorizedException;

public class InvalidCredentialsException extends UnauthorizedException {

    public InvalidCredentialsException() {
        super("Credenciales inválidas");
    }
}
