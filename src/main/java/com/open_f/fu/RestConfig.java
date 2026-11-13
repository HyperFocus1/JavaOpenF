package com.open_f.fu;

import org.glassfish.jersey.server.ResourceConfig;

import com.open_f.fu.account.AccountsBinder;
import com.open_f.fu.chat.ChatsBinder;
import com.open_f.fu.shared.SharedBinder;
import com.open_f.fu.workspace.WorkspacesBinder;

import jakarta.ws.rs.ApplicationPath;

/**
 * Punto de arranque de la API: une los módulos. Es la única clase que conoce a todos.
 */
@ApplicationPath("/api")
public class RestConfig extends ResourceConfig {

    public RestConfig() {
        packages(
                "com.open_f.fu.account.api",
                "com.open_f.fu.workspaces.api",
                "com.open_f.fu.chats.api",
                "com.open_f.fu.shared.error",
                "com.open_f.fu.shared.security");

        register(new SharedBinder());
        register(new AccountsBinder());
        register(new WorkspacesBinder());
        register(new ChatsBinder());
    }
}
