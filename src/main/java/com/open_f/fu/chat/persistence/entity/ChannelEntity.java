package com.open_f.fu.chat.persistence.entity;

import java.util.ArrayList;
import java.util.List;

import com.open_f.fu.shared.persistence.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Solo la tabla "channels". Las reglas viven en chats.domain.model.Channel.
 */
@Entity
@Table(name = "channels")
public class ChannelEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255)
    private String title;

    @OneToMany(mappedBy = "channel", fetch = FetchType.LAZY)
    private List<MessageEntity> messages = new ArrayList<>();

    protected ChannelEntity() {
    }

    public ChannelEntity(String title) {
        this.title = title;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public List<MessageEntity> getMessages() { return messages; }

    public void setTitle(String title) { this.title = title; }
}
