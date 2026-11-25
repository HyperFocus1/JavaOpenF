package com.open_f.fu.workspace.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.open_f.fu.workspace.domain.event.TaskCreated;
import com.open_f.fu.workspace.domain.event.TaskMoved;
import com.open_f.fu.workspace.domain.event.TaskUpdated;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class TaskEventKafkaPublisher {

    private static final Logger LOG = Logger.getLogger(TaskEventKafkaPublisher.class.getName());

    private final ObjectMapper mapper = new ObjectMapper();

    // microprofile-config.properties conecta tasks-out con el topic Kafka tasks.events.
    @Inject
    @Channel("tasks-out")
    Emitter<String> emitter;

    public void publishCreated(@Observes(during = TransactionPhase.AFTER_SUCCESS) TaskCreated event) {
        send("task.created", event, event.eventId());
    }

    public void publishUpdated(@Observes(during = TransactionPhase.AFTER_SUCCESS) TaskUpdated event) {
        send("task.updated", event, event.eventId());
    }

    public void publishMoved(@Observes(during = TransactionPhase.AFTER_SUCCESS) TaskMoved event) {
        send("task.moved", event, event.eventId());
    }

    private void send(String type, Object event, String eventId) {
        try {
            ObjectNode payload = mapper.valueToTree(event);
            payload.put("type", type);

            emitter.send(payload.toString()).whenComplete((ignored, failure) -> {
                if (failure != null) logFailure(type, eventId, failure);
            });
        } catch (RuntimeException failure) {
            logFailure(type, eventId, failure);
        }
    }

    private void logFailure(String type, String eventId, Throwable failure) {
        // La BD ya confirmó: un fallo de Kafka no puede deshacer esa transacción.
        LOG.log(Level.SEVERE, "No se pudo publicar " + type + " (" + eventId + ")", failure);
    }
}
