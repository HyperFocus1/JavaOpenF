package com.open_f.fu.workspace.websocket;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.open_f.fu.shared.security.JwtService;
import com.open_f.fu.workspace.service.ProjectAccessService;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.websocket.CloseReason;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import jakarta.websocket.server.ServerEndpointConfig;
import org.eclipse.microprofile.config.ConfigProvider;

import java.io.StringReader;
import java.time.Instant;
import java.util.Arrays;

@Dependent
@ServerEndpoint(value = "/ws/projects/{projectId}/tasks",
        configurator = TaskWebSocketEndpoint.OriginPolicy.class)
public class TaskWebSocketEndpoint {

    @Inject
    JwtService jwtService;
    @Inject
    ProjectAccessService access;
    @Inject
    TaskWebSocketSessions sessions;

    private Long projectId;

    @OnOpen
    public void open(Session session, @PathParam("projectId") Long projectId) {
        this.projectId = projectId;
        session.setMaxIdleTimeout(10_000);
        session.setMaxTextMessageBufferSize(8192);
        session.getAsyncRemote().setSendTimeout(5_000);
        if (projectId == null || projectId <= 0) reject(session);
    }

    @OnMessage(maxMessageSize = 8192)
    public void message(Session session, String text) {
        if (!session.isOpen()) return;
        try {
            JsonObject message;
            try (var reader = Json.createReader(new StringReader(text))) {
                message = reader.readObject();
            }

            String type = message.getString("type", "");
            if (sessions.contains(session)) {
                if (!sessions.isAuthorized(session)) return;
                if (!"ping".equals(type)) throw new SecurityException("Mensaje no permitido");
                sessions.send(session, "{\"type\":\"pong\"}");
                return;
            }

            if (!"authenticate".equals(type)) throw new SecurityException("Autenticación requerida");
            DecodedJWT jwt = jwtService.verifyToken(message.getString("token"));
            Long userId = Long.valueOf(jwt.getSubject());
            if (userId <= 0 || jwt.getExpiresAt() == null) throw new SecurityException("Token inválido");
            Instant expiresAt = jwt.getExpiresAt().toInstant();
            if (!Instant.now().isBefore(expiresAt)) throw new SecurityException("Token expirado");

            access.requireAccess(userId, projectId);
            sessions.register(session, userId, projectId, expiresAt);
            session.setMaxIdleTimeout(90_000);
            sessions.send(session, "{\"type\":\"ready\"}");
        } catch (RuntimeException failure) {
            reject(session);
        }
    }

    @OnClose
    public void close(Session session) {
        sessions.remove(session);
    }

    @OnError
    public void error(Session session, Throwable failure) {
        if (session != null) {
            sessions.close(session, CloseReason.CloseCodes.UNEXPECTED_CONDITION, "Error de conexión");
        }
    }

    private void reject(Session session) {
        sessions.close(session, CloseReason.CloseCodes.VIOLATED_POLICY, "Conexión rechazada");
    }

    public static class OriginPolicy extends ServerEndpointConfig.Configurator {
        @Override
        public boolean checkOrigin(String origin) {
            String allowed = ConfigProvider.getConfig().getValue("app.websocket.origins", String.class);
            return origin != null && Arrays.stream(allowed.split(","))
                    .map(String::trim).anyMatch(origin::equals);
        }
    }
}
