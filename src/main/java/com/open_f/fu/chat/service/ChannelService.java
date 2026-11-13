package com.open_f.fu.chat.service;

import com.open_f.fu.chat.domain.exception.ChannelNotFoundException;
import com.open_f.fu.chat.domain.interfaces.ChannelRepository;
import com.open_f.fu.chat.domain.model.Channel;
import com.open_f.fu.shared.persistence.TransactionManager;

import jakarta.inject.Inject;

public class ChannelService {

    private final ChannelRepository channels;
    private final TransactionManager tx;

    @Inject
    public ChannelService(ChannelRepository channels, TransactionManager tx) {
        this.channels = channels;
        this.tx = tx;
    }

    public Channel create(String title) {
        Channel channel = Channel.create(title);
        return tx.inTransaction(() -> channels.save(channel));
    }

    public Channel find(Long id) {
        return tx.inTransaction(() -> channels.findById(id)
                .orElseThrow(() -> new ChannelNotFoundException(id)));
    }

    public Channel findByTitle(String title) {
        return tx.inTransaction(() -> channels.findFirstByTitleContaining(title)
                .orElseThrow(() -> new ChannelNotFoundException(title)));
    }

    public Channel update(Long id, String title) {
        return tx.inTransaction(() -> {
            Channel channel = channels.findById(id)
                    .orElseThrow(() -> new ChannelNotFoundException(id));
            channel.rename(title);
            return channels.save(channel);
        });
    }
}
