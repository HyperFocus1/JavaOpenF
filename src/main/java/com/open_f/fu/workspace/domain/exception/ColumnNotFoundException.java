package com.open_f.fu.workspace.domain.exception;

import com.open_f.fu.shared.domain.NotFoundException;

public class ColumnNotFoundException extends NotFoundException {

    public ColumnNotFoundException(Long columnId, Long projectId) {
        super("La columna " + columnId + " no existe en el proyecto " + projectId);
    }
}
