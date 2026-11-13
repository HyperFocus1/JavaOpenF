package com.open_f.fu.workspace.service.command;

public record TaskCreateCommand(
        Long projectId,
        Long columnId,
        Long userId,
        String title,
        String description,
        String priority,
        int positionOrder) {
}
