package com.open_f.fu.chat.persistence.mapper;

import com.open_f.fu.chat.domain.model.Message;
import com.open_f.fu.chat.persistence.entity.ChannelEntity;
import com.open_f.fu.chat.persistence.entity.MessageEntity;

public final class MessageMapper {

    private MessageMapper() {
    }

    public static Message toDomain(MessageEntity e) {
        return Message.restore(e.getId(), e.getChannel().getId(), e.getUserId(), e.getContent(), e.getCreatedAt());
    }

    public static MessageEntity toNewEntity(Message m, ChannelEntity channel) {
        return new MessageEntity(channel, m.getUserId(), m.getContent());
    }

    public static void copy(Message from, MessageEntity to) {
        to.setContent(from.getContent());
    }
}
