package com.open_f.fu.workspace.websocket;

import com.open_f.fu.workspace.service.ProjectAccessService;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.CloseReason;
import jakarta.websocket.Session;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class TaskWebSocketSessions {

    private static final Logger LOG = Logger.getLogger(TaskWebSocketSessions.class.getName());

    private record Connection(Session session, Long userId, Long projectId, Instant expiresAt) {}

    private final Map<String, Connection> connections = new ConcurrentHashMap<>();

    @Inject
    ProjectAccessService access;

    public void register(Session session, Long userId, Long projectId, Instant expiresAt) {
        connections.put(session.getId(), new Connection(session, userId, projectId, expiresAt));
    }

    public boolean contains(Session session) {
        return connections.containsKey(session.getId());
    }

    public boolean isAuthorized(Session session) {
        Connection connection = connections.get(session.getId());
        if (connection == null) return false;

        boolean valid = Instant.now().isBefore(connection.expiresAt())
                && access.canAccess(connection.userId(), connection.projectId());
        if (!valid) {
            close(session, CloseReason.CloseCodes.VIOLATED_POLICY, "Autorización expirada o revocada");
        }
        return valid;
    }

    public void broadcast(Long projectId, String json) {
        for (Connection connection : connections.values()) {
            if (connection.projectId().equals(projectId) && isAuthorized(connection.session())) {
                send(connection.session(), json);
            }
        }
    }

    public void send(Session session, String json) {
        try {
            // Serializa las escrituras y espera un tiempo acotado por cliente.
            synchronized (session) {
                if (session.isOpen()) {
                    session.getAsyncRemote().sendText(json).get(5, TimeUnit.SECONDS);
                }
            }
        } catch (InterruptedException failure) {
            Thread.currentThread().interrupt();
            closeAfterSendFailure(session);
        } catch (Exception failure) {
            LOG.log(Level.FINE, "No se pudo entregar un evento WebSocket", failure);
            closeAfterSendFailure(session);
        }
    }

    public void remove(Session session) {
        connections.remove(session.getId());
    }

    public void close(Session session, CloseReason.CloseCode code, String reason) {
        remove(session);
        try {
            if (session.isOpen()) session.close(new CloseReason(code, reason));
        } catch (IOException ignored) {
            // El transporte ya se está cerrando.
        }
    }

    private void closeAfterSendFailure(Session session) {
        close(session, CloseReason.CloseCodes.UNEXPECTED_CONDITION, "No se pudo entregar el mensaje");
    }

    @PreDestroy
    public void shutdown() {
        for (Connection connection : connections.values()) {
            close(connection.session(), CloseReason.CloseCodes.GOING_AWAY, "Servidor detenido");
        }
    }
}
