package com.open_f.fu.chat.service;

import com.open_f.fu.chat.domain.exception.ChannelNotFoundException;
import com.open_f.fu.chat.domain.interfaces.ChannelRepository;
import com.open_f.fu.chat.domain.model.Channel;

import jakarta.inject.Inject;
import jakarta.enterprise.context.Dependent;
import jakarta.transaction.Transactional;

@Dependent
@Transactional
public class ChannelService {

    private final ChannelRepository channels;

    @Inject
    public ChannelService(ChannelRepository channels) {
        this.channels = channels;
    }

    public Channel create(String title) {
        Channel channel = Channel.create(title);
        return channels.save(channel);
    }

    public Channel find(Long id) {
        return channels.findById(id).orElseThrow(() -> new ChannelNotFoundException(id));
    }

    public Channel findByTitle(String title) {
        return channels.findFirstByTitleContaining(title).orElseThrow(() -> new ChannelNotFoundException(title));
    }

    public Channel update(Long id, String title) {
        Channel channel = channels.findById(id).orElseThrow(() -> new ChannelNotFoundException(id));
        channel.rename(title);
        return channels.save(channel);
    }
}
