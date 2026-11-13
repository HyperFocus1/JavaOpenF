package com.open_f.fu.chat.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MessageCreateRequest(
        @JsonProperty("channel_id") Long channelId,
        @JsonProperty("user_id") Long userId,
        String content) {
}
