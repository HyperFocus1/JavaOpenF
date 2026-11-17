package com.open_f.fu.workspace.messaging;

import com.open_f.fu.workspace.websocket.TaskWebSocketSessions;
import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import java.io.StringReader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class TaskEventKafkaListener {

    @Inject
    TaskWebSocketSessions sessions;

    // Nombre global: este bean puede inicializarse desde un hilo del conector Kafka.
    @Resource(lookup = "java:jboss/ee/concurrency/executor/default")
    ManagedExecutorService executor;

    @Incoming("tasks-in")
    @Acknowledgment(Acknowledgment.Strategy.POST_PROCESSING)
    public CompletionStage<Void> receive(String json) {
        return CompletableFuture.runAsync(() -> {
            Long projectId;
            try (var reader = Json.createReader(new StringReader(json))) {
                projectId = reader.readObject().getJsonNumber("projectId").longValueExact();
            }
            sessions.broadcast(projectId, json);
        }, executor);
    }
}
