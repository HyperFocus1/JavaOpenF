package com.open_f.fu.workspace.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.open_f.fu.workspace.domain.event.TaskCreated;
import com.open_f.fu.workspace.domain.event.TaskMoved;
import com.open_f.fu.workspace.domain.event.TaskUpdated;
import com.open_f.fu.workspace.websocket.TaskWebSocketSessions;
import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.logging.Logger;

@ApplicationScoped
public class TaskEventKafkaListener {

    private static final Logger LOG = Logger.getLogger(TaskEventKafkaListener.class.getName());

    private final ObjectMapper mapper = new ObjectMapper();

    @Inject
    TaskWebSocketSessions webSocketSessions;

    // Nombre global: este bean puede inicializarse desde un hilo del conector Kafka.
    @Resource(lookup = "java:jboss/ee/concurrency/executor/default")
    ManagedExecutorService executor;

    // 1. SmallRye entrega aquí los mensajes de tasks.events mediante el canal tasks-in.
    @Incoming("tasks-in")
    @Acknowledgment(Acknowledgment.Strategy.POST_PROCESSING)
    public CompletionStage<Void> listenFromKafka(String message) {
        return CompletableFuture.runAsync(() -> routeByEventType(message), executor);
    }

    // 2. El campo type del JSON selecciona el evento Java y su tratamiento.
    private void routeByEventType(String message) {
        try {
            if (!(mapper.readTree(message) instanceof ObjectNode payload)) {
                throw new IllegalArgumentException("El evento de Kafka debe ser un objeto JSON");
            }
            String eventType = payload.path("type").asText();
            payload.remove("type");

            switch (eventType) {
                case "task.created" -> {
                    TaskCreated event = mapper.treeToValue(payload, TaskCreated.class);
                    onTaskCreated(event, message);
                }
                case "task.updated" -> {
                    TaskUpdated event = mapper.treeToValue(payload, TaskUpdated.class);
                    onTaskUpdated(event, message);
                }
                case "task.moved" -> {
                    TaskMoved event = mapper.treeToValue(payload, TaskMoved.class);
                    onTaskMoved(event, message);
                }
                default -> throw new IllegalArgumentException("Evento no reconocido: " + eventType);
            }
        } catch (JsonProcessingException failure) {
            throw new IllegalArgumentException("No se pudo interpretar el evento de Kafka", failure);
        }
    }

    // 3. Cada evento tiene su propio método para notificar a las conexiones del proyecto.
    private void onTaskCreated(TaskCreated event, String message) {
        LOG.info(() -> "Tarea creada: " + event.taskId());
        webSocketSessions.broadcast(event.projectId(), message);
    }

    private void onTaskUpdated(TaskUpdated event, String message) {
        LOG.info(() -> "Tarea editada: " + event.taskId());
        webSocketSessions.broadcast(event.projectId(), message);
    }

    private void onTaskMoved(TaskMoved event, String message) {
        LOG.info(() -> "Tarea movida: " + event.taskId() + ", posición: " + event.positionY());
        webSocketSessions.broadcast(event.projectId(), message);
    }
}
