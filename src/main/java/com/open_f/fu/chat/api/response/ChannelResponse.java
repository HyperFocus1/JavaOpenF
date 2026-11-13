package com.open_f.fu.chat.api.response;

import com.open_f.fu.chat.domain.model.Channel;

public record ChannelResponse(Long id, String title) {

    public static ChannelResponse from(Channel c) {
        return new ChannelResponse(c.getId(), c.getTitle());
    }
}
