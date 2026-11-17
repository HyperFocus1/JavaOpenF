package com.open_f.fu.shared.messaging;

import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

public final class KafkaPublisher implements AutoCloseable {
    private final KafkaProducer<String, String> producer;

    public KafkaPublisher(KafkaSettings settings) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, settings.brokers());
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "openf-publisher-" + settings.instanceId());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 5_000);
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 10_000);
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 2_000);
        producer = new KafkaProducer<>(props);
    }

    /** Solo se usa desde los workers de Camel; no bloquea la petición HTTP. */
    public void publishAndWait(String topic, String key, String json) {
        try {
            producer.send(new ProducerRecord<>(topic, key, json)).get(12, TimeUnit.SECONDS);
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Publicación interrumpida", error);
        } catch (Exception error) {
            throw new IllegalStateException("No se pudo publicar en " + topic, error);
        }
    }

    @Override public void close() { producer.close(Duration.ofSeconds(10)); }
}
