package com.open_f.fu.shared.messaging;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public record KafkaSettings(String brokers, String instanceId, Set<String> allowedOrigins) {
    public static final String TASKS_TOPIC = "tasks.events";
    public static final String DEAD_LETTER_TOPIC = "tasks.events.dlt";

    public KafkaSettings {
        if (!instanceId.matches("[a-zA-Z0-9._-]{1,80}")) {
            throw new IllegalArgumentException("REALTIME_INSTANCE_ID no es válido");
        }
        allowedOrigins = Set.copyOf(allowedOrigins);
    }

    public String consumerGroup() {
        // Cada nodo WebSocket debe recibir todos los eventos para sus conexiones locales.
        return "openf-ws-" + instanceId;
    }

    public static KafkaSettings fromEnvironment() {
        String origins = env("REALTIME_ALLOWED_ORIGINS",
                "http://localhost:8080,http://127.0.0.1:8080");
        return new KafkaSettings(
                env("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092"),
                env("REALTIME_INSTANCE_ID", "local"),
                Arrays.stream(origins.split(",")).map(String::trim)
                        .filter(value -> !value.isBlank()).collect(Collectors.toSet()));
    }

    private static String env(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
