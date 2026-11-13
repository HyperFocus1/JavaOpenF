package com.open_f.fu.account.api;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.open_f.fu.account.api.request.LoginRequest;
import com.open_f.fu.account.api.request.RegisterRequest;
import com.open_f.fu.account.api.response.UserResponse;
import com.open_f.fu.account.service.UserService;
import com.open_f.fu.shared.security.BrowserSessions;
import com.open_f.fu.shared.security.BrowserSessions.Entry;
import com.open_f.fu.shared.security.JwtService;

import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

@Path("/session")
@Produces(MediaType.APPLICATION_JSON)
public class SessionResource {
    public record SessionResponse(UserResponse user) {}

    private final UserService users;
    private final JwtService jwt;
    @Context private HttpHeaders headers;

    @Inject
    public SessionResource(UserService users, JwtService jwt) {
        this.users = users;
        this.jwt = jwt;
    }

    @GET
    public Response current() {
        Entry entry = currentEntry();
        if (entry == null) entry = BrowserSessions.anonymous();
        return sessionResponse(entry, Response.Status.OK);
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(LoginRequest request) {
        Entry previous = requireCsrf();
        if (request == null) throw new BadRequestException();
        return signIn(previous, request.email(), request.password(), Response.Status.OK);
    }

    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response register(RegisterRequest request) {
        Entry previous = requireCsrf();
        if (request == null || request.password() == null || request.password().length() < 8
                || request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new BadRequestException("La contraseña debe tener al menos 8 caracteres y como máximo 72 bytes");
        }
        users.register(request.toCommand());
        return signIn(previous, request.email(), request.password(), Response.Status.CREATED);
    }

    @POST
    @Path("/logout")
    public Response logout() {
        Entry entry = currentEntry();
        if (entry != null) requireCsrf();
        BrowserSessions.invalidate(entry == null ? null : entry.id());
        return noStore(Response.noContent())
            .cookie(cookie(BrowserSessions.SESSION_COOKIE, "", true, 0),
                    cookie(BrowserSessions.CSRF_COOKIE, "", false, 0))
            .build();
    }

    private Response signIn(Entry previous, String email, String password, Response.Status status) {
        String token = users.login(email, password);
        DecodedJWT decoded = jwt.verifyToken(token);
        UserResponse user = UserResponse.from(users.current(Long.valueOf(decoded.getSubject())));
        Entry entry = BrowserSessions.authenticate(previous.id(), token, user, decoded.getExpiresAt().toInstant());
        return sessionResponse(entry, status);
    }

    private Entry currentEntry() {
        Cookie cookie = headers.getCookies().get(BrowserSessions.SESSION_COOKIE);
        return BrowserSessions.find(cookie == null ? null : cookie.getValue());
    }

    private Entry requireCsrf() {
        Entry entry = currentEntry();
        if (!BrowserSessions.csrfMatches(entry, headers.getHeaderString(BrowserSessions.CSRF_HEADER))) {
            throw new ForbiddenException("Formulario caducado");
        }
        return entry;
    }

    private static Response sessionResponse(Entry entry, Response.Status status) {
        int maxAge = (int) Math.min(Integer.MAX_VALUE,
            Math.max(0, Duration.between(Instant.now(), entry.expiresAt()).getSeconds()));
        return noStore(Response.status(status))
            .entity(new SessionResponse(entry.user()))
            .cookie(cookie(BrowserSessions.SESSION_COOKIE, entry.id(), true, maxAge),
                    cookie(BrowserSessions.CSRF_COOKIE, entry.csrf(), false, maxAge))
            .build();
    }

    private static NewCookie cookie(String name, String value, boolean httpOnly, int maxAge) {
        return new NewCookie.Builder(name).value(value).path("/").secure(true)
            .httpOnly(httpOnly).sameSite(NewCookie.SameSite.LAX).maxAge(maxAge).build();
    }

    private static Response.ResponseBuilder noStore(Response.ResponseBuilder builder) {
        return builder.header("Cache-Control", "no-store").header("Pragma", "no-cache");
    }
}
