package com.open_f.fu.workspace.api.request;

import com.open_f.fu.workspace.service.command.TaskUpdateCommand;
import com.open_f.fu.workspace.service.command.TaskUpdatePositionCommand;

public record TaskUpdateRequest(Long taskId, String title, String description, int positionOrder) {

    public TaskUpdateCommand toCommand(Long taskId) {
        return new TaskUpdateCommand(taskId, title, description);
    }

    public TaskUpdatePositionCommand taskUpdatePositionCommand(Long taskId, int positionOrder) {
        return new TaskUpdatePositionCommand(taskId, title, description, positionOrder);
    }
}
