package com.open_f.fu.account.domain.exception;

import com.open_f.fu.shared.domain.NotFoundException;

public class UserNotFoundException extends NotFoundException {

    public UserNotFoundException(Long id) {
        super("Usuario " + id + " no existe");
    }
}
