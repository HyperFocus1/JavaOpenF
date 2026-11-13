package com.open_f.fu.account.api;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import com.open_f.fu.account.api.request.LoginRequest;
import com.open_f.fu.shared.security.BrowserSessions;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

class SessionResourceTest {
    @Test
    void bootstrapSetsAnOpaqueHttpOnlyCookieAndASeparateReadableCsrfCookie() throws Exception {
        SessionResource resource = resource(Map.of(), null);
        try (Response response = resource.current()) {
            NewCookie session = response.getCookies().get(BrowserSessions.SESSION_COOKIE);
            NewCookie csrf = response.getCookies().get(BrowserSessions.CSRF_COOKIE);
            assertTrue(session.isHttpOnly());
            assertTrue(session.isSecure());
            assertEquals("/", session.getPath());
            assertEquals(NewCookie.SameSite.LAX, session.getSameSite());
            assertFalse(csrf.isHttpOnly());
            assertTrue(csrf.isSecure());
            assertNotEquals(session.getValue(), csrf.getValue());
            assertEquals("no-store", response.getHeaderString("Cache-Control"));
            assertNull(((SessionResource.SessionResponse) response.getEntity()).user());
            BrowserSessions.invalidate(session.getValue());
        }
    }

    @Test
    void loginRejectsMissingOrWrongCsrfBeforeCallingAuthentication() throws Exception {
        var session = BrowserSessions.anonymous();
        try {
            var cookies = Map.of(BrowserSessions.SESSION_COOKIE,
                new Cookie(BrowserSessions.SESSION_COOKIE, session.id()));
            assertThrows(ForbiddenException.class, () -> resource(cookies, null)
                .login(new LoginRequest("a@example.test", "password")));
            assertThrows(ForbiddenException.class, () -> resource(cookies, "wrong")
                .login(new LoginRequest("a@example.test", "password")));
        } finally {
            BrowserSessions.invalidate(session.id());
        }
    }

    @Test
    void logoutRevokesServerStateAndExpiresBothCookies() throws Exception {
        var session = BrowserSessions.anonymous();
        var cookies = Map.of(BrowserSessions.SESSION_COOKIE,
            new Cookie(BrowserSessions.SESSION_COOKIE, session.id()));
        try (Response response = resource(cookies, session.csrf()).logout()) {
            assertEquals(204, response.getStatus());
            assertNull(BrowserSessions.find(session.id()));
            assertEquals(0, response.getCookies().get(BrowserSessions.SESSION_COOKIE).getMaxAge());
            assertEquals(0, response.getCookies().get(BrowserSessions.CSRF_COOKIE).getMaxAge());
        }
    }

    private static SessionResource resource(Map<String, ? extends Cookie> cookies, String csrf) throws Exception {
        HttpHeaders headers = (HttpHeaders) Proxy.newProxyInstance(HttpHeaders.class.getClassLoader(),
            new Class<?>[]{HttpHeaders.class}, (proxy, method, args) -> switch (method.getName()) {
                case "getCookies" -> new HashMap<>(cookies);
                case "getHeaderString" -> BrowserSessions.CSRF_HEADER.equals(args[0]) ? csrf : null;
                default -> null;
            });
        SessionResource resource = new SessionResource(null, null);
        var field = SessionResource.class.getDeclaredField("headers");
        field.setAccessible(true);
        field.set(resource, headers);
        return resource;
    }
}
