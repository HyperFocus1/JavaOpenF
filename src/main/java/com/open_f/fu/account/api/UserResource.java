package com.open_f.fu.account.api;

import java.net.URI;

import com.open_f.fu.account.api.request.LoginRequest;
import com.open_f.fu.account.api.request.RegisterRequest;
import com.open_f.fu.account.api.response.LoginResponse;
import com.open_f.fu.account.api.response.UserResponse;
import com.open_f.fu.account.domain.model.User;
import com.open_f.fu.account.service.UserService;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

    private final UserService service;

    @Inject
    public UserResource(UserService service) {
        this.service = service;
    }

    @POST
    @Path("/register")
    public Response register(RegisterRequest req) {
        User created = service.register(req.toCommand());
        return Response.created(URI.create("users/" + created.getId())).entity(UserResponse.from(created)).build();
    }

    @POST
    @Path("/login")
    public LoginResponse login(LoginRequest req) {
        return LoginResponse.bearer(service.login(req.email(), req.password()));
    }
}
