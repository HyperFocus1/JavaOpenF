package com.open_f.fu.shared.messaging;

import java.time.Instant;
import jakarta.persistence.*;

@Entity
@Table(name = "outbox_events", uniqueConstraints = @UniqueConstraint(columnNames = "event_id"))
public class OutboxEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "event_id", nullable = false, length = 36)
    private String eventId;
    @Column(nullable = false, length = 120)
    private String topic;
    @Column(name = "partition_key", nullable = false, length = 80)
    private String partitionKey;
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String payload;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected OutboxEvent() {}

    public OutboxEvent(String eventId, String topic, String partitionKey, String payload) {
        this.eventId = eventId;
        this.topic = topic;
        this.partitionKey = partitionKey;
        this.payload = payload;
        this.createdAt = Instant.now();
    }

    public String topic() { return topic; }
    public String partitionKey() { return partitionKey; }
    public String payload() { return payload; }
}
