package com.open_f.fu.shared.security;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import com.open_f.fu.account.api.response.UserResponse;

class BrowserSessionsTest {
    @Test
    void loginRotatesIdentifierAndCsrfAndLogoutRevokesTheSession() {
        var anonymous = BrowserSessions.anonymous();
        var user = new UserResponse(1L, "persona@example.test", "Persona", "Prueba");
        var session = BrowserSessions.authenticate(anonymous.id(), "server-only-jwt", user, Instant.now().plusSeconds(60));
        assertNull(BrowserSessions.find(anonymous.id()));
        assertNotEquals(anonymous.id(), session.id());
        assertNotEquals(anonymous.csrf(), session.csrf());
        assertFalse(BrowserSessions.csrfMatches(session, anonymous.csrf()));
        assertTrue(BrowserSessions.csrfMatches(session, session.csrf()));
        BrowserSessions.invalidate(session.id());
        assertNull(BrowserSessions.find(session.id()));
    }

    @Test
    void rejectsMissingOrIncorrectCsrfAndExpiredAuthentication() {
        var session = BrowserSessions.anonymous();
        try {
            assertFalse(BrowserSessions.csrfMatches(session, null));
            assertFalse(BrowserSessions.csrfMatches(session, "incorrect"));
            assertFalse(BrowserSessions.csrfMatches(null, session.csrf()));
            assertThrows(IllegalArgumentException.class, () -> BrowserSessions.authenticate(
                session.id(), "token", new UserResponse(1L, "a@example.test", "A", "B"), Instant.EPOCH));
        } finally {
            BrowserSessions.invalidate(session.id());
        }
    }
}
