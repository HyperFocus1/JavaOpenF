package com.open_f.fu.workspace.api.response;

import com.open_f.fu.workspace.domain.model.Task;

public record TaskResponse(Long id, String code, String title, String description, String priority, double positionY) {

    public static TaskResponse from(Task t) {
        return new TaskResponse(t.getId(), t.getCode(), t.getTitle(), t.getDescription(),
                t.getPriority().name(), t.getPositionY());
    }
}
