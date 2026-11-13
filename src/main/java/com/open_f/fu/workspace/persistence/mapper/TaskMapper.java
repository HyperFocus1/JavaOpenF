package com.open_f.fu.workspace.persistence.mapper;

import com.open_f.fu.workspace.domain.model.Task;
import com.open_f.fu.workspace.persistence.entity.ProjectColumnEntity;
import com.open_f.fu.workspace.persistence.entity.ProjectEntity;
import com.open_f.fu.workspace.persistence.entity.TaskEntity;

/**
 * Traduce entre {@link TaskEntity} y el modelo de dominio {@link Task}.
 */
public final class TaskMapper {

    private TaskMapper() {
    }

    public static Task toDomain(TaskEntity e) {
        double position = e.getPositionY() == null ? Task.DEFAULT_POSITION : e.getPositionY();
        return Task.restore(e.getId(), e.getCode(), e.getTitle(), e.getDescription(), e.getPriority(),
                position, e.getProject().getId(), e.getColumn().getId(), e.getAuthorId());
    }

    public static TaskEntity toNewEntity(Task t, ProjectEntity project, ProjectColumnEntity column) {
        return new TaskEntity(t.getCode(), t.getTitle(), t.getDescription(), t.getPriority(),
                t.getPositionY(), project, column, t.getAuthorId());
    }

    public static void copy(Task from, TaskEntity to) {
        to.setTitle(from.getTitle());
        to.setDescription(from.getDescription());
        to.setPriority(from.getPriority());
        to.setPositionY(from.getPositionY());
    }
}
