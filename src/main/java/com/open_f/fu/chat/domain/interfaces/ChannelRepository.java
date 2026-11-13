package com.open_f.fu.chat.domain.interfaces;

import java.util.Optional;

import com.open_f.fu.chat.domain.model.Channel;

public interface ChannelRepository {

    Channel save(Channel channel);

    Optional<Channel> findById(Long id);

    Optional<Channel> findFirstByTitleContaining(String text);

    boolean existsById(Long id);
}
