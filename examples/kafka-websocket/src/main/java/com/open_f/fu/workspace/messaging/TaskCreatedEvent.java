package com.open_f.fu.workspace.messaging;

import java.time.Instant;
import java.util.UUID;
import com.open_f.fu.workspace.domain.model.Task;

public record TaskCreatedEvent(String eventId, String type, int version,
        Long projectId, String occurredAt, TaskData data) {

    public static TaskCreatedEvent from(Task task) {
        return new TaskCreatedEvent(UUID.randomUUID().toString(), "task.created", 1,
                task.getProjectId(), Instant.now().toString(),
                new TaskData(task.getId(), task.getCode(), task.getTitle(),
                        task.getDescription(), task.getPriority().name(), task.getPositionY(),
                        task.getColumnId(), task.getUserId()));
    }

    public record TaskData(Long id, String code, String title, String description,
            String priority, double positionY, Long columnId, Long userId) {}
}
