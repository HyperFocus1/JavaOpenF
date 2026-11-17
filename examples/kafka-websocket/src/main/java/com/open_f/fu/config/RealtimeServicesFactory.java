package com.open_f.fu.config;

import com.open_f.fu.shared.messaging.RealtimeServices;
import jakarta.inject.Inject;
import jakarta.servlet.ServletContext;
import org.glassfish.hk2.api.Factory;

/** Puente entre los objetos de Tomcat y la inyección de Jersey/HK2. */
public final class RealtimeServicesFactory implements Factory<RealtimeServices> {
    private final ServletContext context;

    @Inject public RealtimeServicesFactory(ServletContext context) { this.context = context; }

    @Override public RealtimeServices provide() {
        Object services = context.getAttribute(RealtimeServices.ATTRIBUTE);
        if (!(services instanceof RealtimeServices realtime)) {
            throw new IllegalStateException("RealtimeBootstrapListener no se ha inicializado");
        }
        return realtime;
    }

    @Override public void dispose(RealtimeServices services) {
        // El dueño del ciclo de vida es RealtimeBootstrapListener.
    }
}
