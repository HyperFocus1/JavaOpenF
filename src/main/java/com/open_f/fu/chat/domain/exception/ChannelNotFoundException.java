package com.open_f.fu.chat.domain.exception;

import com.open_f.fu.shared.domain.NotFoundException;

public class ChannelNotFoundException extends NotFoundException {

    public ChannelNotFoundException(Long id) {
        super("Canal " + id + " no existe");
    }

    public ChannelNotFoundException(String title) {
        super("No hay canal con título '" + title + "'");
    }
}
