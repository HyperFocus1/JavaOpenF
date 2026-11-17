package com.open_f.fu.shared.realtime;

import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.open_f.fu.shared.realtime.ProjectSockets.Peer;
import jakarta.websocket.*;
import jakarta.ws.rs.ForbiddenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Endpoint programático: el configurador le entrega la misma instancia del hub. */
public final class EventsEndpoint extends Endpoint {
    private static final Logger log = LoggerFactory.getLogger(EventsEndpoint.class);
    private final ProjectSockets sockets;
    private final ObjectMapper mapper;
    private Peer peer;

    public EventsEndpoint(ProjectSockets sockets, ObjectMapper mapper) {
        this.sockets = sockets;
        this.mapper = mapper;
    }

    @Override public void onOpen(Session session, EndpointConfig config) {
        try {
            peer = sockets.open(session);
            session.addMessageHandler(String.class, text -> receive(text));
        } catch (RuntimeException error) {
            onError(session, error);
        }
    }

    private void receive(String text) {
        try {
            JsonNode command = mapper.readTree(text);
            if (command == null || !command.isObject()) throw new IllegalArgumentException();
            String type = command.path("type").asText();
            if ("authenticate".equals(type)) {
                sockets.authenticate(peer, command.path("token").asText());
                return;
            }
            sockets.requireAuthenticated(peer);
            switch (type) {
                case "subscribe" -> sockets.subscribe(peer, projectId(command));
                case "unsubscribe" -> sockets.unsubscribe(peer, projectId(command));
                case "ping" -> sockets.send(peer, Map.of("type", "pong"));
                default -> sockets.send(peer, Map.of("type", "error", "code", "unknown_command"));
            }
        } catch (ForbiddenException error) {
            sockets.send(peer, Map.of("type", "error", "code", "project_access_denied"));
        } catch (IllegalArgumentException | com.auth0.jwt.exceptions.JWTVerificationException error) {
            sockets.disconnect(peer, "Autenticación o solicitud no válida");
        } catch (com.fasterxml.jackson.core.JsonProcessingException error) {
            sockets.send(peer, Map.of("type", "error", "code", "invalid_json"));
        } catch (RuntimeException error) {
            log.error("No se pudo procesar el comando WebSocket", error);
            sockets.send(peer, Map.of("type", "error", "code", "temporarily_unavailable"));
        }
    }

    private Long projectId(JsonNode command) {
        JsonNode id = command.path("projectId");
        if (!id.isIntegralNumber() || !id.canConvertToLong()) throw new IllegalArgumentException();
        return id.longValue();
    }

    @Override public void onClose(Session session, CloseReason reason) {
        sockets.remove(session.getId());
    }

    @Override public void onError(Session session, Throwable error) {
        log.debug("Conexión WebSocket cerrada por error", error);
        if (peer != null) sockets.disconnect(peer, "Error en la conexión");
        else {
            try { session.close(); } catch (java.io.IOException ignored) { }
        }
    }
}
