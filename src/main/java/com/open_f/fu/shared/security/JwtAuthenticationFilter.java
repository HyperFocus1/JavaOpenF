package com.open_f.fu.shared.security;

import java.util.Set;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class JwtAuthenticationFilter implements ContainerRequestFilter {

    // Rutas que no piden token (relativas a /api)
    private static final Set<String> PUBLIC_PATHS = Set.of("users/login", "users/register");
    private static final String BEARER = "Bearer ";

    private final JwtService jwtService;

    @Inject
    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public void filter(ContainerRequestContext ctx) {
        String path = ctx.getUriInfo().getPath();
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (PUBLIC_PATHS.contains(path)) {
            return;
        }

        String authorization = ctx.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith(BEARER)) {
            ctx.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
            return;
        }

        try {
            DecodedJWT jwt = jwtService.verifyToken(authorization.substring(BEARER.length()));
            ctx.setSecurityContext(new JwtSecurityContext(jwt.getSubject(), ctx.getSecurityContext()));
        } catch (JWTVerificationException | IllegalArgumentException e) {
            ctx.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
        }
    }
}
