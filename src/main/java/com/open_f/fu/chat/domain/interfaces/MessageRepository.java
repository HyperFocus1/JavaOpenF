package com.open_f.fu.chat.domain.interfaces;

import java.util.List;

import com.open_f.fu.chat.domain.model.Message;

public interface MessageRepository {

    Message save(Message message);

    List<Message> findAllByChannelId(Long channelId);
}
