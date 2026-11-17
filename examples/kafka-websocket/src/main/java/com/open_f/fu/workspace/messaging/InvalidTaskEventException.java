package com.open_f.fu.workspace.messaging;

public final class InvalidTaskEventException extends RuntimeException {
    public InvalidTaskEventException(Throwable cause) { super("Evento de tarea no válido", cause); }
}
