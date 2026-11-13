package com.open_f.fu.workspace.service.command;

public record TaskUpdateCommand(Long id, String title, String description) {
}