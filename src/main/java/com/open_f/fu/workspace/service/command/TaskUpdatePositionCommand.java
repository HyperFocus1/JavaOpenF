package com.open_f.fu.workspace.service.command;

public record TaskUpdatePositionCommand(Long id, String title, String description, int positionOrder) {
}
