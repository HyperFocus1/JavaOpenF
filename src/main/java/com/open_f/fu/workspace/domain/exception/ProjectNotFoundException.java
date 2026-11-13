package com.open_f.fu.workspace.domain.exception;

import com.open_f.fu.shared.domain.NotFoundException;

public class ProjectNotFoundException extends NotFoundException {

    public ProjectNotFoundException(Long id) {
        super("Proyecto " + id + " no existe");
    }
}
