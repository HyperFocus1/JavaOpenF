package com.open_f.fu.shared.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.open_f.fu.shared.realtime.ProjectSockets;
import com.open_f.fu.shared.security.JwtService;
import com.open_f.fu.workspace.messaging.TaskEventsHandler;
import com.open_f.fu.workspace.messaging.TaskEventsRoute;
import com.open_f.fu.workspace.service.ProjectAccessService;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Una instancia por WAR. El listener la crea y HK2 reutiliza exactamente esa instancia. */
public final class RealtimeServices implements AutoCloseable {
    public static final String ATTRIBUTE = RealtimeServices.class.getName();
    private static final Logger log = LoggerFactory.getLogger(RealtimeServices.class);
    private final KafkaSettings settings;
    private final ObjectMapper mapper = new ObjectMapper();
    private final ProjectAccessService access = new ProjectAccessService();
    private final ProjectSockets sockets;
    private final OutboxStore outbox = new OutboxStore(mapper);
    private final KafkaPublisher publisher;
    private final CamelContext camel = new DefaultCamelContext();

    public RealtimeServices(KafkaSettings settings) {
        this.settings = settings;
        sockets = new ProjectSockets(mapper, new JwtService(), access);
        publisher = new KafkaPublisher(settings);
    }

    public void start() throws Exception {
        KafkaComponent kafka = new KafkaComponent();
        kafka.getConfiguration().setBrokers(settings.brokers());
        camel.addComponent("kafka", kafka);
        camel.getShutdownStrategy().setTimeout(20);
        camel.addRoutes(new TaskEventsRoute(settings,
                new TaskEventsHandler(mapper, sockets), publisher, mapper));
        camel.addRoutes(new RouteBuilder() {
            @Override public void configure() {
                // Cada tick publica un registro; un fallo conserva el outbox para el siguiente tick.
                from("timer:outbox?period=250&delay=1000&fixedRate=false")
                        .routeId("publish-outbox")
                        .process(exchange -> outbox.publishPending(publisher));
                from("timer:socket-expiry?period=1000&delay=1000")
                        .routeId("expire-sockets")
                        .process(exchange -> sockets.expire());
            }
        });
        camel.start();
        log.info("Tiempo real iniciado. Grupo Kafka: {}", settings.consumerGroup());
    }

    public ObjectMapper mapper() { return mapper; }
    public ProjectSockets sockets() { return sockets; }
    public ProjectAccessService access() { return access; }
    public OutboxStore outbox() { return outbox; }

    @Override public void close() {
        try { camel.stop(); }
        catch (Exception error) { log.warn("Error al detener Camel", error); }
        try { publisher.close(); }
        finally { sockets.close(); }
    }
}
