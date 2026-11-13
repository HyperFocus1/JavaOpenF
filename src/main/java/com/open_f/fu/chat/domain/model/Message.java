package com.open_f.fu.chat.domain.model;

import java.time.Duration;
import java.time.Instant;

import com.open_f.fu.shared.domain.DomainException;

/**
 * Mensaje de un canal. Solo reglas, sin JPA.
 */
public class Message {

    public static final Duration EDIT_WINDOW = Duration.ofMinutes(5);

    private final Long id;
    private final Long channelId;
    private final Long userId;
    private final Instant createdAt;
    private String content;

    private Message(Long id, Long channelId, Long userId, String content, Instant createdAt) {
        if (channelId == null) throw new DomainException("El canal es obligatorio");
        if (userId == null) throw new DomainException("El usuario es obligatorio");
        this.id = id;
        this.channelId = channelId;
        this.userId = userId;
        this.createdAt = createdAt;
        this.content = cleanContent(content);
    }

    public static Message create(Long channelId, Long userId, String content) {
        return new Message(null, channelId, userId, content, null);
    }

    public static Message restore(Long id, Long channelId, Long userId, String content, Instant createdAt) {
        if (id == null) {
            throw new IllegalArgumentException("El taskId es obligatorio al restaurar");
        }
        return new Message(id, channelId, userId, content, createdAt);
    }

    // Regla: solo se edita dentro de los primeros 5 minutos
    public void edit(String newContent, Instant now) {
        if (createdAt != null && now.isAfter(createdAt.plus(EDIT_WINDOW))) {
            throw new DomainException("Pasó el límite para actualizar el contenido");
        }
        this.content = cleanContent(newContent);
    }

    private static String cleanContent(String content) {
        if (content == null || content.isBlank()) {
            throw new DomainException("El mensaje no puede estar vacío");
        }
        return content.trim();
    }

    public Long getId() { return id; }
    public Long getChannelId() { return channelId; }
    public Long getUserId() { return userId; }
    public String getContent() { return content; }
    public Instant getCreatedAt() { return createdAt; }
}
