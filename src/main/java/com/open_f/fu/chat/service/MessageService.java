package com.open_f.fu.chat.service;

import java.util.List;

import com.open_f.fu.account.domain.exception.UserNotFoundException;
import com.open_f.fu.account.service.UserService;
import com.open_f.fu.chat.domain.exception.ChannelNotFoundException;
import com.open_f.fu.chat.domain.interfaces.ChannelRepository;
import com.open_f.fu.chat.domain.interfaces.MessageRepository;
import com.open_f.fu.chat.domain.model.Message;
import com.open_f.fu.shared.persistence.TransactionManager;

import jakarta.inject.Inject;

public class MessageService {

    private final MessageRepository messages;
    private final ChannelRepository channels;
    private final UserService userService; // otro módulo: solo por su servicio
    private final TransactionManager tx;

    @Inject
    public MessageService(MessageRepository messages, ChannelRepository channels,
                          UserService userService, TransactionManager tx) {
        this.messages = messages;
        this.channels = channels;
        this.userService = userService;
        this.tx = tx;
    }

    public Message send(Long channelId, Long userId, String content) {
        Message message = Message.create(channelId, userId, content);

        return tx.inTransaction(() -> {
            if (!channels.existsById(message.getChannelId())) {
                throw new ChannelNotFoundException(message.getChannelId());
            }
            if (!userService.exists(message.getUserId())) {
                throw new UserNotFoundException(message.getUserId());
            }
            return messages.save(message);
        });
    }

    public List<Message> allMessages(Long channelId) {
        return tx.inTransaction(() -> messages.findAllByChannelId(channelId));
    }
}
