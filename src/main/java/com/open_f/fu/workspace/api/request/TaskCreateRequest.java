package com.open_f.fu.workspace.api.request;

import com.open_f.fu.workspace.service.command.TaskCreateCommand;

public record TaskCreateRequest(String title, String description, String priority) {

    public TaskCreateCommand toCommand(Long projectId, Long columnId, Long userId, int positionOrder) {
        return new TaskCreateCommand(projectId, columnId, userId, title, description, priority, positionOrder);
    }
}
