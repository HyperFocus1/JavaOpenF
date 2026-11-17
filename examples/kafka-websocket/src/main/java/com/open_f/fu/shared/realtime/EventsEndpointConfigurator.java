package com.open_f.fu.shared.realtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.open_f.fu.shared.messaging.KafkaSettings;
import jakarta.websocket.server.ServerEndpointConfig;

public final class EventsEndpointConfigurator extends ServerEndpointConfig.Configurator {
    private final KafkaSettings settings;
    private final ProjectSockets sockets;
    private final ObjectMapper mapper;

    public EventsEndpointConfigurator(KafkaSettings settings, ProjectSockets sockets, ObjectMapper mapper) {
        this.settings = settings;
        this.sockets = sockets;
        this.mapper = mapper;
    }

    @Override public boolean checkOrigin(String origin) {
        return origin != null && settings.allowedOrigins().contains(origin);
    }

    @Override public <T> T getEndpointInstance(Class<T> type) throws InstantiationException {
        if (type != EventsEndpoint.class) throw new InstantiationException("Endpoint no soportado");
        return type.cast(new EventsEndpoint(sockets, mapper));
    }
}
