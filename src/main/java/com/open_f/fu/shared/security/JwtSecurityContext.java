package com.open_f.fu.shared.security;

import java.security.Principal;

import jakarta.ws.rs.core.SecurityContext;

public class JwtSecurityContext implements SecurityContext {

    private final Principal principal;
    private final SecurityContext original;

    public JwtSecurityContext(String userId, SecurityContext original) {
        this.principal = () -> userId;
        this.original = original;
    }

    @Override
    public Principal getUserPrincipal() {
        return principal;
    }

    @Override
    public boolean isUserInRole(String role) {
        return false;
    }

    @Override
    public boolean isSecure() {
        return original.isSecure();
    }

    @Override
    public String getAuthenticationScheme() {
        return "Bearer";
    }
}
