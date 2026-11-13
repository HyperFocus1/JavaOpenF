package com.open_f.fu.config;

import java.util.Set;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.open_f.fu.account.security.BrowserSessions;
import com.open_f.fu.account.security.BrowserSessions.Entry;
import com.open_f.fu.shared.security.JwtSecurityContext;
import com.open_f.fu.shared.security.JwtService;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class JwtAuthenticationFilter implements ContainerRequestFilter {
    private static final Set<String> PUBLIC_PATHS = Set.of(
        "health", "users/login", "users/register", "session", "session/login", "session/register", "session/logout");
    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS");
    private static final String BEARER = "Bearer ";
    private final JwtService jwtService;

    @Inject
    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public void filter(ContainerRequestContext context) {
        String path = context.getUriInfo().getPath();
        if (path.startsWith("/")) path = path.substring(1);
        if (PUBLIC_PATHS.contains(path)) return;

        String authorization = context.getHeaderString(HttpHeaders.AUTHORIZATION);
        String token;
        if (authorization != null) {
            if (!authorization.startsWith(BEARER)) { unauthorized(context); return; }
            token = authorization.substring(BEARER.length());
        } else {
            Cookie cookie = context.getCookies().get(BrowserSessions.SESSION_COOKIE);
            Entry session = BrowserSessions.find(cookie == null ? null : cookie.getValue());
            if (session == null || session.user() == null || session.jwt() == null) {
                unauthorized(context); return;
            }
            if (!SAFE_METHODS.contains(context.getMethod())
                    && !BrowserSessions.csrfMatches(session, context.getHeaderString(BrowserSessions.CSRF_HEADER))) {
                context.abortWith(Response.status(Response.Status.FORBIDDEN).build()); return;
            }
            token = session.jwt();
        }

        try {
            DecodedJWT decoded = jwtService.verifyToken(token);
            context.setSecurityContext(new JwtSecurityContext(decoded.getSubject(), context.getSecurityContext()));
        } catch (JWTVerificationException | IllegalArgumentException exception) {
            unauthorized(context);
        }
    }

    private static void unauthorized(ContainerRequestContext context) {
        context.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
    }
}
