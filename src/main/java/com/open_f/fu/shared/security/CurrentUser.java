package com.open_f.fu.shared.security;

import java.security.Principal;

import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.core.SecurityContext;

/**
 * Lee el taskId del usuario autenticado que dejó JwtAuthenticationFilter.
 */
public final class CurrentUser {

    private CurrentUser() {
    }

    public static Long id(SecurityContext securityContext) {
        Principal principal = securityContext == null ? null : securityContext.getUserPrincipal();
        if (principal == null) {
            throw new NotAuthorizedException("Bearer");
        }
        return Long.valueOf(principal.getName());
    }
}
