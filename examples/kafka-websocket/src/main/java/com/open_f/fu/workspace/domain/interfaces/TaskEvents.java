package com.open_f.fu.workspace.domain.interfaces;

import com.open_f.fu.workspace.domain.model.Task;

/** El servicio registra lo ocurrido dentro de la misma transacción de la tarea. */
public interface TaskEvents {
    void created(Task task);
}
