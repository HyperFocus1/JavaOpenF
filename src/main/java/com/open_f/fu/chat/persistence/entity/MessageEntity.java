package com.open_f.fu.chat.persistence.entity;

import com.open_f.fu.shared.persistence.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Solo la tabla "messages". Las reglas viven en chats.domain.model.Message.
 * El usuario se guarda por taskId: chats no depende de las entidades de accounts.
 */
@Entity
@Table(name = "messages")
public class MessageEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id")
    private ChannelEntity channel;

    @Column(name = "user_id")
    private Long userId;

    @Column(columnDefinition = "TEXT")
    private String content;

    protected MessageEntity() {
    }

    public MessageEntity(ChannelEntity channel, Long userId, String content) {
        this.channel = channel;
        this.userId = userId;
        this.content = content;
    }

    public Long getId() { return id; }
    public ChannelEntity getChannel() { return channel; }
    public Long getUserId() { return userId; }
    public String getContent() { return content; }

    public void setContent(String content) { this.content = content; }
}
