package com.open_f.fu.shared.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.open_f.fu.shared.persistence.JpaTransactionManager;
import com.open_f.fu.shared.persistence.PersistenceManager;
import com.open_f.fu.workspace.messaging.TaskCreatedEvent;

public final class OutboxStore {
    private final ObjectMapper mapper;

    public OutboxStore(ObjectMapper mapper) { this.mapper = mapper; }

    public void append(TaskCreatedEvent event) {
        try {
            JpaTransactionManager.currentEntityManager().persist(new OutboxEvent(
                    event.eventId(), KafkaSettings.TASKS_TOPIC, event.projectId().toString(),
                    mapper.writeValueAsString(event)));
        } catch (JsonProcessingException error) {
            throw new IllegalStateException("No se pudo serializar task.created", error);
        }
    }

    public void publishPending(KafkaPublisher publisher) {
        PersistenceManager.getEntityManagerFactory().runInTransaction(em -> {
            // Un registro por transacción. SKIP LOCKED permite varios publicadores.
            // Si Kafka falla se revierte la eliminación y se reintentará en el próximo tick.
            var ids = em.createNativeQuery(
                    "SELECT id FROM outbox_events ORDER BY id LIMIT 1 FOR UPDATE SKIP LOCKED",
                    Long.class).getResultList();
            if (ids.isEmpty()) return;
            OutboxEvent event = em.find(OutboxEvent.class, ids.getFirst());
            publisher.publishAndWait(event.topic(), event.partitionKey(), event.payload());
            em.remove(event);
        });
    }
}
