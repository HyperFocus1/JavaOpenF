package com.open_f.fu.config;

import com.open_f.fu.shared.messaging.KafkaSettings;
import com.open_f.fu.shared.messaging.RealtimeServices;
import com.open_f.fu.shared.realtime.EventsEndpoint;
import com.open_f.fu.shared.realtime.EventsEndpointConfigurator;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.websocket.server.ServerContainer;
import jakarta.websocket.server.ServerEndpointConfig;

public final class RealtimeBootstrapListener implements ServletContextListener {
    private RealtimeServices services;

    @Override public void contextInitialized(ServletContextEvent event) {
        try {
            var context = event.getServletContext();
            var container = (ServerContainer) context.getAttribute(ServerContainer.class.getName());
            if (container == null) throw new IllegalStateException("Tomcat no expuso ServerContainer");
            KafkaSettings settings = KafkaSettings.fromEnvironment();
            services = new RealtimeServices(settings);
            container.addEndpoint(ServerEndpointConfig.Builder
                    .create(EventsEndpoint.class, "/ws/events")
                    .configurator(new EventsEndpointConfigurator(
                            settings, services.sockets(), services.mapper()))
                    .build());
            services.start();
            context.setAttribute(RealtimeServices.ATTRIBUTE, services);
        } catch (Exception error) {
            if (services != null) services.close();
            throw new IllegalStateException("No se pudo iniciar el tiempo real", error);
        }
    }

    @Override public void contextDestroyed(ServletContextEvent event) {
        event.getServletContext().removeAttribute(RealtimeServices.ATTRIBUTE);
        if (services != null) services.close();
    }
}
