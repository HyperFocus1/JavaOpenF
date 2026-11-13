package com.open_f.fu.account.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

import com.open_f.fu.account.api.response.UserResponse;
import jakarta.ws.rs.ServiceUnavailableException;

/**
 * Sesiones de navegador para una sola instancia.
 * Sustituir el repositorio en memoria por Redis antes de desplegar varias réplicas.
 * Los JWT quedan en el servidor; el navegador recibe únicamente un ID opaco.
 */
public final class BrowserSessions {
    public static final String SESSION_COOKIE = "__Host-openf_session";
    public static final String CSRF_COOKIE = "OPENF-XSRF-TOKEN";
    public static final String CSRF_HEADER = "X-XSRF-TOKEN";
    private static final int MAX_SESSIONS = 20_000;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final ConcurrentHashMap<String, Entry> ENTRIES = new ConcurrentHashMap<>();

    public record Entry(String id, String csrf, String jwt, UserResponse user, Instant expiresAt) {}

    private BrowserSessions() {}

    public static Entry find(String id) {
        if (id == null) return null;
        Entry entry = ENTRIES.get(id);
        if (entry != null && !entry.expiresAt().isAfter(Instant.now())) {
            ENTRIES.remove(id, entry);
            return null;
        }
        return entry;
    }

    public static Entry anonymous() {
        return create(null, null, Instant.now().plusSeconds(900));
    }

    public static Entry authenticate(String previousId, String jwt, UserResponse user, Instant expiresAt) {
        if (jwt == null || user == null || expiresAt == null || !expiresAt.isAfter(Instant.now())) {
            throw new IllegalArgumentException("Sesión no válida");
        }
        Entry result = create(jwt, user, expiresAt);
        invalidate(previousId);
        return result;
    }

    private static synchronized Entry create(String jwt, UserResponse user, Instant expiresAt) {
        Instant now = Instant.now();
        ENTRIES.entrySet().removeIf(item -> !item.getValue().expiresAt().isAfter(now));
        if (ENTRIES.size() >= MAX_SESSIONS) throw new ServiceUnavailableException("Servicio de acceso ocupado");
        Entry entry = new Entry(random(), random(), jwt, user, expiresAt);
        ENTRIES.put(entry.id(), entry);
        return entry;
    }

    public static boolean csrfMatches(Entry entry, String supplied) {
        return entry != null && supplied != null
            && MessageDigest.isEqual(entry.csrf().getBytes(StandardCharsets.UTF_8), supplied.getBytes(StandardCharsets.UTF_8));
    }

    public static void invalidate(String id) {
        if (id != null) ENTRIES.remove(id);
    }

    private static String random() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
