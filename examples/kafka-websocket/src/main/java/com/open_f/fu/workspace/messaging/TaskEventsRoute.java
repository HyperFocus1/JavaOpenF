package com.open_f.fu.workspace.messaging;

import java.time.Instant;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.open_f.fu.shared.messaging.KafkaPublisher;
import com.open_f.fu.shared.messaging.KafkaSettings;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.camel.component.kafka.consumer.KafkaManualCommit;

public final class TaskEventsRoute extends RouteBuilder {
    private final KafkaSettings settings;
    private final TaskEventsHandler handler;
    private final KafkaPublisher publisher;
    private final ObjectMapper mapper;

    public TaskEventsRoute(KafkaSettings settings, TaskEventsHandler handler,
            KafkaPublisher publisher, ObjectMapper mapper) {
        this.settings = settings;
        this.handler = handler;
        this.publisher = publisher;
        this.mapper = mapper;
    }

    @Override public void configure() {
        // Un JSON/esquema inválido no mejorará reintentándolo.
        // Confirmamos el origen solo después de que Kafka acepte la copia en DLT.
        onException(InvalidTaskEventException.class)
                .maximumRedeliveries(0).handled(true).useOriginalMessage()
                .process(this::deadLetterAndCommit);

        // Un fallo transitorio no se convierte en un evento inválido.
        // Si persiste, breakOnFirstError conserva el offset para reintentarlo.
        onException(Exception.class)
                .maximumRedeliveries(3).redeliveryDelay(1_000).handled(false);

        from("kafka:" + KafkaSettings.TASKS_TOPIC + "?groupId=" + settings.consumerGroup()
                + "&autoCommitEnable=false&allowManualCommit=true&breakOnFirstError=true"
                + "&autoOffsetReset=earliest&maxPollRecords=25&consumersCount=1")
                .routeId("task-events")
                .process(exchange -> handler.handle(exchange.getMessage().getBody(String.class)))
                .process(TaskEventsRoute::commit);
    }

    private void deadLetterAndCommit(Exchange exchange) throws Exception {
        String raw = exchange.getMessage().getBody(String.class);
        String key = exchange.getMessage().getHeader(KafkaConstants.KEY, String.class);
        String deadLetter = mapper.writeValueAsString(Map.of(
                "originalEvent", raw,
                "reason", "invalid_task_event",
                "sourceTopic", KafkaSettings.TASKS_TOPIC,
                "partition", String.valueOf(exchange.getMessage().getHeader(KafkaConstants.PARTITION)),
                "offset", String.valueOf(exchange.getMessage().getHeader(KafkaConstants.OFFSET)),
                "failedAt", Instant.now().toString()));
        publisher.publishAndWait(KafkaSettings.DEAD_LETTER_TOPIC, key, deadLetter);
        commit(exchange);
    }

    private static void commit(Exchange exchange) {
        KafkaManualCommit manual = exchange.getMessage()
                .getHeader(KafkaConstants.MANUAL_COMMIT, KafkaManualCommit.class);
        if (manual == null) throw new IllegalStateException("Falta el commit manual de Kafka");
        // La ruta conserva el hilo del consumidor; no usar threads()/parallelProcessing() aquí.
        manual.commit();
    }
}
