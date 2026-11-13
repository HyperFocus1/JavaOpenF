package com.open_f.fu;

import org.glassfish.jersey.server.ResourceConfig;

import com.open_f.fu.account.AccountsBinder;
import com.open_f.fu.chat.ChatsBinder;
import com.open_f.fu.config.ApiExceptionMapper;
import com.open_f.fu.config.JwtAuthenticationFilter;
import com.open_f.fu.config.SharedBinder;
import com.open_f.fu.workspace.WorkspacesBinder;

import jakarta.ws.rs.ApplicationPath;

/**
 * Punto de arranque de la API: une los módulos. Es la única clase que conoce a todos.
 */
@ApplicationPath("/api")
public class RestApplication extends ResourceConfig {

    public RestApplication() {
        packages(
                "com.open_f.fu.account.api",
                "com.open_f.fu.workspace.api",
                "com.open_f.fu.chat.api",
                "com.open_f.fu.shared.api");

        register(ApiExceptionMapper.class);
        register(JwtAuthenticationFilter.class);

        register(new SharedBinder());
        register(new AccountsBinder());
        register(new WorkspacesBinder());
        register(new ChatsBinder());
    }
}
