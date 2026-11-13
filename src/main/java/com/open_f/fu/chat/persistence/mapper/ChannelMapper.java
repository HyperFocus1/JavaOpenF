package com.open_f.fu.chat.persistence.mapper;

import com.open_f.fu.chat.domain.model.Channel;
import com.open_f.fu.chat.persistence.entity.ChannelEntity;

public final class ChannelMapper {

    private ChannelMapper() {
    }

    public static Channel toDomain(ChannelEntity e) {
        return Channel.restore(e.getId(), e.getTitle());
    }

    public static ChannelEntity toNewEntity(Channel c) {
        return new ChannelEntity(c.getTitle());
    }

    public static void copy(Channel from, ChannelEntity to) {
        to.setTitle(from.getTitle());
    }
}
