package com.open_f.fu.workspace.domain.event;

import com.open_f.fu.workspace.domain.model.Task;

import java.util.UUID;

public record TaskCreated(String eventId, Long projectId, Long taskId, Long columnId,
                          String title, String description, double positionY) {

    public static TaskCreated from(Task task) {
        return new TaskCreated(UUID.randomUUID().toString(),
                task.getProjectId(), task.getId(), task.getColumnId(),
                task.getTitle(), task.getDescription(), task.getPositionY());
    }
}
