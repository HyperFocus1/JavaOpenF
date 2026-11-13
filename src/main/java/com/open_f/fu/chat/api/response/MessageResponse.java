package com.open_f.fu.chat.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.open_f.fu.chat.domain.model.Message;

public record MessageResponse(
        Long id,
        @JsonProperty("channel_id") Long channelId,
        String content) {

    public static MessageResponse from(Message m) {
        return new MessageResponse(m.getId(), m.getChannelId(), m.getContent());
    }
}
