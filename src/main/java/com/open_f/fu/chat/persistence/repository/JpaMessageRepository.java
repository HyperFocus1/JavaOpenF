package com.open_f.fu.chat.persistence.repository;

import java.util.List;

import com.open_f.fu.chat.domain.interfaces.MessageRepository;
import com.open_f.fu.chat.domain.model.Message;
import com.open_f.fu.chat.persistence.entity.ChannelEntity;
import com.open_f.fu.chat.persistence.entity.MessageEntity;
import com.open_f.fu.chat.persistence.mapper.MessageMapper;
import com.open_f.fu.shared.persistence.JpaRepository;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

public class JpaMessageRepository extends JpaRepository<MessageEntity> implements MessageRepository {

    private final EntityManager em;

    @Inject
    public JpaMessageRepository(EntityManager em) {
        super(MessageEntity.class);
        this.em = em;
    }

    @Override
    public Message save(Message message) {
        if (message.getId() == null) {
            ChannelEntity channel = this.em.getReference(ChannelEntity.class, message.getChannelId());
            return MessageMapper.toDomain(persist(this.em, MessageMapper.toNewEntity(message, channel)));
        }
        MessageEntity e = findEntity(this.em, message.getId())
                .orElseThrow(() -> new IllegalStateException("Mensaje " + message.getId() + " no existe"));
        MessageMapper.copy(message, e);
        return MessageMapper.toDomain(e);
    }

    @Override
    public List<Message> findAllByChannelId(Long channelId) {
        return this.em.createQuery("""
                SELECT m FROM MessageEntity m
                WHERE m.channel.id = :channelId
                ORDER BY m.id
                """, MessageEntity.class)
                .setParameter("channelId", channelId)
                .getResultStream()
                .map(MessageMapper::toDomain)
                .toList();
    }
}
