package com.open_f.fu.workspace.messaging;

import com.open_f.fu.workspace.domain.event.TaskEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.inject.Inject;
import jakarta.json.Json;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class TaskEventKafkaPublisher {

    private static final Logger LOG = Logger.getLogger(TaskEventKafkaPublisher.class.getName());

    @Inject
    @Channel("tasks-out")
    Emitter<String> emitter;

    public void publish(@Observes(during = TransactionPhase.AFTER_SUCCESS) TaskEvent event) {
        try {
            String json = Json.createObjectBuilder()
                    .add("eventId", event.eventId())
                    .add("type", event.type())
                    .add("projectId", event.projectId())
                    .add("taskId", event.taskId())
                    .add("columnId", event.columnId())
                    .add("title", event.title())
                    .build().toString();

            emitter.send(json).whenComplete((ignored, failure) -> {
                if (failure != null) logFailure(event, failure);
            });
        } catch (RuntimeException failure) {
            logFailure(event, failure);
        }
    }

    private void logFailure(TaskEvent event, Throwable failure) {
        // La BD ya confirmó: un fallo de Kafka no puede deshacer esa transacción.
        LOG.log(Level.SEVERE, "No se pudo publicar el evento " + event.eventId(), failure);
    }
}
