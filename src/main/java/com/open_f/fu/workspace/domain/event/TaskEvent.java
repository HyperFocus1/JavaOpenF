package com.open_f.fu.workspace.domain.event;

import com.open_f.fu.workspace.domain.model.Task;

import java.util.UUID;

public record TaskEvent(String eventId, String type, Long projectId,
                        Long taskId, Long columnId, String title) {

    public static TaskEvent created(Task task) {
        return new TaskEvent(UUID.randomUUID().toString(), "task.created",
                task.getProjectId(), task.getId(), task.getColumnId(), task.getTitle());
    }
}
