package com.open_f.fu.shared.realtime;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.open_f.fu.shared.security.JwtService;
import com.open_f.fu.workspace.service.ProjectAccessService;
import jakarta.websocket.CloseReason;
import jakarta.websocket.Session;

public final class ProjectSockets implements AutoCloseable {
    private final Map<String, Peer> peers = new ConcurrentHashMap<>();
    private final ObjectMapper mapper;
    private final JwtService jwt;
    private final ProjectAccessService access;

    public ProjectSockets(ObjectMapper mapper, JwtService jwt, ProjectAccessService access) {
        this.mapper = mapper;
        this.jwt = jwt;
        this.access = access;
    }

    public Peer open(Session session) {
        if (peers.size() >= 10_000) throw new IllegalStateException("Sin capacidad de conexión");
        session.setMaxIdleTimeout(120_000);
        session.setMaxTextMessageBufferSize(8_192);
        Peer peer = new Peer(session);
        peers.put(session.getId(), peer);
        send(peer, Map.of("type", "connected"));
        return peer;
    }

    public void authenticate(Peer peer, String token) {
        if (peer.userId != null) throw new IllegalArgumentException("Ya autenticado");
        var decoded = jwt.verifyToken(token);
        if (decoded.getExpiresAt() == null) throw new IllegalArgumentException("Falta expiración");
        long expiry = decoded.getExpiresAt().getTime();
        Long userId = Long.valueOf(decoded.getSubject());
        if (expiry <= System.currentTimeMillis() || userId <= 0) {
            throw new IllegalArgumentException("Token no válido");
        }
        peer.expiresAt = expiry;
        peer.userId = userId;
        send(peer, Map.of("type", "authenticated", "userId", userId));
    }

    public void subscribe(Peer peer, Long projectId) {
        requireAuthenticated(peer);
        if (projectId == null || projectId <= 0 || peer.projects.size() >= 32) {
            throw new IllegalArgumentException("Suscripción no válida");
        }
        access.requireOwner(peer.userId, projectId);
        peer.projects.add(projectId);
        send(peer, Map.of("type", "subscribed", "projectId", projectId));
    }

    public void unsubscribe(Peer peer, Long projectId) {
        requireAuthenticated(peer);
        if (projectId == null || projectId <= 0) throw new IllegalArgumentException("Proyecto no válido");
        peer.projects.remove(projectId);
        send(peer, Map.of("type", "unsubscribed", "projectId", projectId));
    }

    public void requireAuthenticated(Peer peer) {
        if (peer.userId == null || peer.expiresAt <= System.currentTimeMillis()) {
            throw new IllegalArgumentException("Autenticación requerida");
        }
    }

    public void broadcast(Long projectId, String json) {
        if (peers.values().stream().noneMatch(peer -> peer.projects.contains(projectId))) return;
        // Se vuelve a comprobar el propietario en cada evento, también tras un cambio de permisos.
        Long owner = access.ownerOf(projectId);
        if (owner == null) return;
        for (Peer peer : peers.values()) {
            if (!peer.projects.contains(projectId)) continue;
            if (peer.expiresAt <= System.currentTimeMillis() || !owner.equals(peer.userId)) {
                disconnect(peer, "La sesión o el permiso expiró");
            } else {
                peer.sender.offer(json);
            }
        }
    }

    public void send(Peer peer, Object message) {
        try {
            peer.sender.offer(mapper.writeValueAsString(message));
        } catch (JsonProcessingException error) {
            throw new IllegalStateException("No se pudo serializar la respuesta", error);
        }
    }

    public void expire() {
        long now = System.currentTimeMillis();
        for (Peer peer : peers.values()) {
            if (!peer.session.isOpen()) {
                remove(peer.session.getId());
            } else if ((peer.userId == null && now - peer.openedAt > 10_000)
                    || (peer.userId != null && now >= peer.expiresAt)) {
                disconnect(peer, "Autenticación expirada");
            }
        }
    }

    public void remove(String sessionId) {
        Peer peer = peers.remove(sessionId);
        if (peer != null) peer.sender.clear();
    }

    public void disconnect(Peer peer, String reason) {
        disconnect(peer, CloseReason.CloseCodes.VIOLATED_POLICY, reason);
    }

    private void disconnect(Peer peer, CloseReason.CloseCode code, String reason) {
        remove(peer.session.getId());
        try {
            peer.session.close(new CloseReason(code, reason));
        } catch (IOException ignored) { }
    }

    @Override public void close() {
        for (Peer peer : peers.values()) {
            disconnect(peer, CloseReason.CloseCodes.GOING_AWAY, "La aplicación se está deteniendo");
        }
    }

    public static final class Peer {
        private final Session session;
        private final WebSocketSender sender;
        private final long openedAt = System.currentTimeMillis();
        private final Set<Long> projects = ConcurrentHashMap.newKeySet();
        private volatile Long userId;
        private volatile long expiresAt;

        private Peer(Session session) {
            this.session = session;
            this.sender = new WebSocketSender(session);
        }
    }
}
