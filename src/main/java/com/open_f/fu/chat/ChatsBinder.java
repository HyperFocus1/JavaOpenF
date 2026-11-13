package com.open_f.fu.chat;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

import com.open_f.fu.chat.domain.interfaces.ChannelRepository;
import com.open_f.fu.chat.domain.interfaces.MessageRepository;
import com.open_f.fu.chat.persistence.repository.JpaChannelRepository;
import com.open_f.fu.chat.persistence.repository.JpaMessageRepository;
import com.open_f.fu.chat.service.ChannelService;
import com.open_f.fu.chat.service.MessageService;

import jakarta.inject.Singleton;

public class ChatsBinder extends AbstractBinder {

    @Override
    protected void configure() {
        bind(JpaChannelRepository.class).to(ChannelRepository.class).in(Singleton.class);
        bind(JpaMessageRepository.class).to(MessageRepository.class).in(Singleton.class);
        bindAsContract(ChannelService.class).in(Singleton.class);
        bindAsContract(MessageService.class).in(Singleton.class);
    }
}
