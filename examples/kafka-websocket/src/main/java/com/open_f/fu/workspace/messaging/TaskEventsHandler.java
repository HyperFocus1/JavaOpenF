package com.open_f.fu.workspace.messaging;

import java.time.Instant;
import java.util.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.open_f.fu.shared.realtime.ProjectSockets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Adaptador del módulo workspace: JSON Kafka -> evento validado -> conexiones del proyecto. */
public final class TaskEventsHandler {
    private static final Logger log = LoggerFactory.getLogger(TaskEventsHandler.class);
    private final ObjectMapper mapper;
    private final ProjectSockets sockets;

    public TaskEventsHandler(ObjectMapper mapper, ProjectSockets sockets) {
        this.mapper = mapper;
        this.sockets = sockets;
    }

    public void handle(String json) {
        TaskCreatedEvent event;
        try {
            event = mapper.readValue(json, TaskCreatedEvent.class);
            if (event == null || !"task.created".equals(event.type()) || event.version() != 1
                    || event.projectId() == null || event.projectId() <= 0
                    || event.data() == null || event.data().id() == null || event.data().id() <= 0
                    || event.data().title() == null) {
                throw new IllegalArgumentException("Esquema no soportado");
            }
            UUID.fromString(event.eventId());
            Instant.parse(event.occurredAt());
        } catch (Exception error) {
            throw new InvalidTaskEventException(error);
        }
        // Los errores de DB/infraestructura conservan su tipo para poder reintentarse.
        sockets.broadcast(event.projectId(), json);
        log.info("Evento {} procesado: task.created, proyecto {}, tarea {}",
                event.eventId(), event.projectId(), event.data().id());
    }
}
