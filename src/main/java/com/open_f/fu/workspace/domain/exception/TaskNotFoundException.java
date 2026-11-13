package com.open_f.fu.workspace.domain.exception;

import com.open_f.fu.shared.domain.NotFoundException;

public class TaskNotFoundException extends NotFoundException {

    public TaskNotFoundException(Long id) {
        super("Tarea " + id + " no existe");
    }
}
