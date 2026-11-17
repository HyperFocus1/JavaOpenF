# Código completo: Kafka, Camel y WebSocket

Este documento reúne todos los archivos nuevos y los reemplazos completos. Los demás archivos de la aplicación se conservan del proyecto actual. Lee README.md para preparar y ejecutar una copia aislada.

## pom.xml

[Abrir archivo del ejemplo](D:/Dev/JavaOpenF/examples/kafka-websocket/pom.xml)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.open_f.fu</groupId>
    <artifactId>open_f</artifactId>
    <version>1.0-SNAPSHOT</version>

    <!-- Aplicación WAR para desplegar en Tomcat. -->
    <packaging>war</packaging>

    <properties>
        <maven.compiler.release>21</maven.compiler.release>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>

        <hibernate.version>7.4.13.Final</hibernate.version>
        <jersey.version>3.1.11</jersey.version>
        <reactor-bom.version>2025.0.8</reactor-bom.version>
        <junit.version>6.0.3</junit.version>
        <mockito.version>5.24.0</mockito.version>
        <camel.version>4.22.2</camel.version>
    </properties>

    <!-- Alinea Reactor Core y Reactor Netty, sin Spring ni Spring Boot. -->
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>io.projectreactor</groupId>
                <artifactId>reactor-bom</artifactId>
                <version>${reactor-bom.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>

        <!-- Camel administra las rutas Kafka y el publicador periódico del outbox. -->
        <dependency>
            <groupId>org.apache.camel</groupId>
            <artifactId>camel-core</artifactId>
            <version>${camel.version}</version>
        </dependency>
        <dependency>
            <groupId>org.apache.camel</groupId>
            <artifactId>camel-kafka</artifactId>
            <version>${camel.version}</version>
        </dependency>
        <!-- El cliente Kafka llega con camel-kafka; no mezclamos versiones. -->
        <dependency>
            <groupId>jakarta.websocket</groupId>
            <artifactId>jakarta.websocket-api</artifactId>
            <version>2.2.0</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>jakarta.websocket</groupId>
            <artifactId>jakarta.websocket-client-api</artifactId>
            <version>2.2.0</version>
            <scope>provided</scope>
        </dependency>

        <!-- Piezas de reactividad: ver webflux.md. No cambia el arranque de Jersey. -->
        <dependency>
            <groupId>io.projectreactor</groupId>
            <artifactId>reactor-core</artifactId>
        </dependency>

        <!-- Cliente y servidor HTTP no bloqueantes sobre Netty. -->
        <dependency>
            <groupId>io.projectreactor.netty</groupId>
            <artifactId>reactor-netty-http</artifactId>
        </dependency>

        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>${junit.version}</version>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>org.mockito</groupId>
            <artifactId>mockito-junit-jupiter</artifactId>
            <version>${mockito.version}</version>
            <scope>test</scope>
        </dependency>

        <!-- servlet -->
        <dependency>
            <groupId>jakarta.servlet</groupId>
            <artifactId>jakarta.servlet-api</artifactId>
            <version>6.0.0</version>
            <scope>provided</scope>
        </dependency>


        <!-- Source: https://mvnrepository.com/artifact/org.hibernate.orm/hibernate-core -->
        <dependency>
            <groupId>org.hibernate.orm</groupId>
            <artifactId>hibernate-core</artifactId>
            <version>${hibernate.version}</version>
            <scope>compile</scope>
        </dependency>


        <!-- Source: https://mvnrepository.com/artifact/org.hibernate.orm/hibernate-hikaricp -->
        <dependency>
            <groupId>org.hibernate.orm</groupId>
            <artifactId>hibernate-hikaricp</artifactId>
            <version>${hibernate.version}</version>
            <scope>compile</scope>
        </dependency>

        <!-- Ya viene incluido en hibernate-core -->
        <!--        <dependency>-->
        <!--            <groupId>jakarta.persistence</groupId>-->
        <!--            <artifactId>jakarta.persistence-api</artifactId>-->
        <!--            <version>3.2.0</version>-->
        <!--        </dependency>-->

        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <version>26.10.0</version>
        </dependency>

        <!-- Para logs -->
        <dependency>
            <groupId>ch.qos.logback</groupId>
            <artifactId>logback-classic</artifactId>
            <version>1.6.5</version>
        </dependency>

        <!-- Se necesita para que hibernate detecte los @Entity -->
        <dependency>
            <groupId>org.hibernate.orm</groupId>
            <artifactId>hibernate-scan-jandex</artifactId>
            <version>${hibernate.version}</version>
        </dependency>


        <!-- Jackson -->
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.22.4</version>
        </dependency>

        <!-- Este sirve para implementar clientes rest externos -->
        <!-- Jersey -->
        <!-- <dependency> -->
        <!--     <groupId>org.glassfish.jersey.core</groupId>-->
        <!--     <artifactId>jersey-client</artifactId>-->
        <!--     <version>4.0.3</version>-->
        <!--     <scope>compile</scope>-->
        <!-- </dependency> -->

        <!-- <dependency> -->
        <!--     <groupId>org.glassfish.jersey.core</groupId> -->
        <!--     <artifactId>jersey-server</artifactId> -->
        <!--     <version>4.0.3</version> -->
        <!--     <scope>compile</scope> -->
        <!-- </dependency> -->

        <dependency>
            <groupId>org.glassfish.jersey.containers</groupId>
            <artifactId>jersey-container-servlet</artifactId>
            <version>4.0.3</version>
            <scope>compile</scope>
        </dependency>

        <dependency>
            <groupId>org.glassfish.jersey.inject</groupId>
            <artifactId>jersey-hk2</artifactId>
            <version>4.0.3</version>
            <scope>compile</scope>
        </dependency>

        <dependency>
            <groupId>org.glassfish.jersey.media</groupId>
            <artifactId>jersey-media-json-jackson</artifactId>
            <version>4.0.3</version>
            <scope>compile</scope>
        </dependency>

        <dependency>
            <groupId>com.auth0</groupId>
            <artifactId>java-jwt</artifactId>
            <version>4.6.2</version>
        </dependency>

        <dependency>
            <groupId>org.mindrot</groupId>
            <artifactId>jbcrypt</artifactId>
            <version>0.4</version>
        </dependency>

    </dependencies>

    <build>
        <finalName>open_f</finalName>

        <plugins>
            <!-- Ejecuta los ejercicios JUnit Jupiter desde Maven. -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.5.4</version>
                <configuration>
                    <!-- Carga Mockito como agente al ejecutar las pruebas en Java 21+. -->
                    <argLine>-javaagent:"${settings.localRepository}/org/mockito/mockito-core/${mockito.version}/mockito-core-${mockito.version}.jar"</argLine>
                </configuration>
            </plugin>

            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.14.1</version>
                <configuration>
                    <release>21</release>
                </configuration>
            </plugin>

            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-war-plugin</artifactId>
                <version>3.4.0</version>
            </plugin>
        </plugins>
    </build>
</project>
```

## docker-compose.yml

[Abrir archivo del ejemplo](D:/Dev/JavaOpenF/examples/kafka-websocket/docker-compose.yml)

```yaml
services:
  kafka:
    image: apache/kafka:4.4.0
    ports:
      - "127.0.0.1:9092:9092"
    environment:
      CLUSTER_ID: "4L6g3nShT-eMCtK--X86sw"
      KAFKA_NODE_ID: "1"
      KAFKA_PROCESS_ROLES: "broker,controller"
      KAFKA_LISTENERS: "INTERNAL://:19092,EXTERNAL://:9092,CONTROLLER://:9093"
      KAFKA_ADVERTISED_LISTENERS: "INTERNAL://kafka:19092,EXTERNAL://localhost:9092"
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: "INTERNAL:PLAINTEXT,EXTERNAL:PLAINTEXT,CONTROLLER:PLAINTEXT"
      KAFKA_INTER_BROKER_LISTENER_NAME: "INTERNAL"
      KAFKA_CONTROLLER_LISTENER_NAMES: "CONTROLLER"
      KAFKA_CONTROLLER_QUORUM_VOTERS: "1@kafka:9093"
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: "1"
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: "false"
      KAFKA_LOG_DIRS: "/tmp/kraft-combined-logs"
    volumes:
      - kafka_data:/tmp/kraft-combined-logs
    healthcheck:
      test: ["CMD", "/opt/kafka/bin/kafka-topics.sh", "--bootstrap-server", "kafka:19092", "--list"]
      interval: 10s
      timeout: 10s
      retries: 12
    restart: unless-stopped

  kafka_init:
    image: apache/kafka:4.4.0
    depends_on:
      kafka:
        condition: service_healthy
    entrypoint: ["/bin/bash", "-ec"]
    command:
      - |
        /opt/kafka/bin/kafka-topics.sh --bootstrap-server kafka:19092 --create --if-not-exists --topic tasks.events --partitions 3 --replication-factor 1
        /opt/kafka/bin/kafka-topics.sh --bootstrap-server kafka:19092 --create --if-not-exists --topic tasks.events.dlt --partitions 3 --replication-factor 1

  data_db:
    image: mysql:8.4
    ports:
      - "3306:3306"
    environment:
      MYSQL_ROOT_PASSWORD: 1234
      MYSQL_DATABASE: open_f
    volumes:
      - data_db:/var/lib/mysql
    restart: unless-stopped

volumes:
  data_db:
  kafka_data:
```

## prepare-example.ps1

[Abrir archivo del ejemplo](D:/Dev/JavaOpenF/examples/kafka-websocket/prepare-example.ps1)

```powershell
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

# Construye una copia ejecutable y conserva intactos src/, pom.xml y compose del repo.
$repoRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '../..')).Path
$buildRoot = Join-Path $PSScriptRoot ('.build/' + [Guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $buildRoot -Force | Out-Null
Copy-Item -LiteralPath (Join-Path $repoRoot 'src') -Destination (Join-Path $buildRoot 'src') -Recurse
Copy-Item -LiteralPath (Join-Path $PSScriptRoot 'pom.xml') -Destination (Join-Path $buildRoot 'pom.xml')
Copy-Item -LiteralPath (Join-Path $PSScriptRoot 'docker-compose.yml') -Destination (Join-Path $buildRoot 'docker-compose.yml')
Copy-Item -Path (Join-Path $PSScriptRoot 'src/*') -Destination (Join-Path $buildRoot 'src') -Recurse -Force
Write-Output $buildRoot
```

## src/main/java/com/open_f/fu/config/RealtimeBootstrapListener.java

[Abrir archivo del ejemplo](D:/Dev/JavaOpenF/examples/kafka-websocket/src/main/java/com/open_f/fu/config/RealtimeBootstrapListener.java)

```java
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
```

## src/main/java/com/open_f/fu/config/RealtimeServicesFactory.java

[Abrir archivo del ejemplo](D:/Dev/JavaOpenF/examples/kafka-websocket/src/main/java/com/open_f/fu/config/RealtimeServicesFactory.java)

```java
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
```

## src/main/java/com/open_f/fu/config/SharedBinder.java

[Abrir archivo del ejemplo](D:/Dev/JavaOpenF/examples/kafka-websocket/src/main/java/com/open_f/fu/config/SharedBinder.java)

```java
package com.open_f.fu.config;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

import com.open_f.fu.shared.persistence.JpaTransactionManager;
import com.open_f.fu.shared.persistence.TransactionManager;
import com.open_f.fu.shared.persistence.TransactionalEntityManager;
import com.open_f.fu.shared.security.JwtService;
import com.open_f.fu.shared.messaging.RealtimeServices;
import com.open_f.fu.workspace.domain.interfaces.TaskEvents;
import com.open_f.fu.workspace.messaging.OutboxTaskEvents;

import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;

public class SharedBinder extends AbstractBinder {

    @Override
    protected void configure() {
        bind(JpaTransactionManager.class).to(TransactionManager.class).in(Singleton.class);
        bind(TransactionalEntityManager.create()).to(EntityManager.class);
        bindAsContract(JwtService.class).in(Singleton.class);
        bindFactory(RealtimeServicesFactory.class).to(RealtimeServices.class).in(Singleton.class);
        bind(OutboxTaskEvents.class).to(TaskEvents.class).in(Singleton.class);
    }
}
```

## src/main/java/com/open_f/fu/shared/messaging/KafkaPublisher.java

[Abrir archivo del ejemplo](D:/Dev/JavaOpenF/examples/kafka-websocket/src/main/java/com/open_f/fu/shared/messaging/KafkaPublisher.java)

```java
package com.open_f.fu.shared.messaging;

import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

public final class KafkaPublisher implements AutoCloseable {
    private final KafkaProducer<String, String> producer;

    public KafkaPublisher(KafkaSettings settings) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, settings.brokers());
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "openf-publisher-" + settings.instanceId());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 5_000);
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 10_000);
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 2_000);
        producer = new KafkaProducer<>(props);
    }

    /** Solo se usa desde los workers de Camel; no bloquea la petición HTTP. */
    public void publishAndWait(String topic, String key, String json) {
        try {
            producer.send(new ProducerRecord<>(topic, key, json)).get(12, TimeUnit.SECONDS);
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Publicación interrumpida", error);
        } catch (Exception error) {
            throw new IllegalStateException("No se pudo publicar en " + topic, error);
        }
    }

    @Override public void close() { producer.close(Duration.ofSeconds(10)); }
}
```

## src/main/java/com/open_f/fu/shared/messaging/KafkaSettings.java

[Abrir archivo del ejemplo](D:/Dev/JavaOpenF/examples/kafka-websocket/src/main/java/com/open_f/fu/shared/messaging/KafkaSettings.java)

```java
package com.open_f.fu.shared.messaging;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public record KafkaSettings(String brokers, String instanceId, Set<String> allowedOrigins) {
    public static final String TASKS_TOPIC = "tasks.events";
    public static final String DEAD_LETTER_TOPIC = "tasks.events.dlt";

    public KafkaSettings {
        if (!instanceId.matches("[a-zA-Z0-9._-]{1,80}")) {
            throw new IllegalArgumentException("REALTIME_INSTANCE_ID no es válido");
        }
        allowedOrigins = Set.copyOf(allowedOrigins);
    }

    public String consumerGroup() {
        // Cada nodo WebSocket debe recibir todos los eventos para sus conexiones locales.
        return "openf-ws-" + instanceId;
    }

    public static KafkaSettings fromEnvironment() {
        String origins = env("REALTIME_ALLOWED_ORIGINS",
                "http://localhost:8080,http://127.0.0.1:8080");
        return new KafkaSettings(
                env("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092"),
                env("REALTIME_INSTANCE_ID", "local"),
                Arrays.stream(origins.split(",")).map(String::trim)
                        .filter(value -> !value.isBlank()).collect(Collectors.toSet()));
    }

    private static String env(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
```

## src/main/java/com/open_f/fu/shared/messaging/OutboxEvent.java

[Abrir archivo del ejemplo](D:/Dev/JavaOpenF/examples/kafka-websocket/src/main/java/com/open_f/fu/shared/messaging/OutboxEvent.java)

```java
package com.open_f.fu.shared.messaging;

import java.time.Instant;
import jakarta.persistence.*;

@Entity
@Table(name = "outbox_events", uniqueConstraints = @UniqueConstraint(columnNames = "event_id"))
public class OutboxEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "event_id", nullable = false, length = 36)
    private String eventId;
    @Column(nullable = false, length = 120)
    private String topic;
    @Column(name = "partition_key", nullable = false, length = 80)
    private String partitionKey;
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String payload;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected OutboxEvent() {}

    public OutboxEvent(String eventId, String topic, String partitionKey, String payload) {
        this.eventId = eventId;
        this.topic = topic;
        this.partitionKey = partitionKey;
        this.payload = payload;
        this.createdAt = Instant.now();
    }

    public String topic() { return topic; }
    public String partitionKey() { return partitionKey; }
    public String payload() { return payload; }
}
```

## src/main/java/com/open_f/fu/shared/messaging/OutboxStore.java

[Abrir archivo del ejemplo](D:/Dev/JavaOpenF/examples/kafka-websocket/src/main/java/com/open_f/fu/shared/messaging/OutboxStore.java)

```java
package com.open_f.fu.shared.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.open_f.fu.shared.persistence.JpaTransactionManager;
import com.open_f.fu.shared.persistence.PersistenceManager;
import com.open_f.fu.workspace.messaging.TaskCreatedEvent;

public final class OutboxStore {
    private final ObjectMapper mapper;

    public OutboxStore(ObjectMapper mapper) { this.mapper = mapper; }

    public void append(TaskCreatedEvent event) {
        try {
            JpaTransactionManager.currentEntityManager().persist(new OutboxEvent(
                    event.eventId(), KafkaSettings.TASKS_TOPIC, event.projectId().toString(),
                    mapper.writeValueAsString(event)));
        } catch (JsonProcessingException error) {
            throw new IllegalStateException("No se pudo serializar task.created", error);
        }
    }

    public void publishPending(KafkaPublisher publisher) {
        PersistenceManager.getEntityManagerFactory().runInTransaction(em -> {
            // Un registro por transacción. SKIP LOCKED permite varios publicadores.
            // Si Kafka falla se revierte la eliminación y se reintentará en el próximo tick.
            var ids = em.createNativeQuery(
                    "SELECT id FROM outbox_events ORDER BY id LIMIT 1 FOR UPDATE SKIP LOCKED",
                    Long.class).getResultList();
            if (ids.isEmpty()) return;
            OutboxEvent event = em.find(OutboxEvent.class, ids.getFirst());
            publisher.publishAndWait(event.topic(), event.partitionKey(), event.payload());
            em.remove(event);
        });
    }
}
```

## src/main/java/com/open_f/fu/shared/messaging/RealtimeServices.java

[Abrir archivo del ejemplo](D:/Dev/JavaOpenF/examples/kafka-websocket/src/main/java/com/open_f/fu/shared/messaging/RealtimeServices.java)

```java
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
```

## src/main/java/com/open_f/fu/shared/realtime/EventsEndpoint.java

[Abrir archivo del ejemplo](D:/Dev/JavaOpenF/examples/kafka-websocket/src/main/java/com/open_f/fu/shared/realtime/EventsEndpoint.java)

```java
package com.open_f.fu.shared.realtime;

import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.open_f.fu.shared.realtime.ProjectSockets.Peer;
import jakarta.websocket.*;
import jakarta.ws.rs.ForbiddenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Endpoint programático: el configurador le entrega la misma instancia del hub. */
public final class EventsEndpoint extends Endpoint {
    private static final Logger log = LoggerFactory.getLogger(EventsEndpoint.class);
    private final ProjectSockets sockets;
    private final ObjectMapper mapper;
    private Peer peer;

    public EventsEndpoint(ProjectSockets sockets, ObjectMapper mapper) {
        this.sockets = sockets;
        this.mapper = mapper;
    }

    @Override public void onOpen(Session session, EndpointConfig config) {
        try {
            peer = sockets.open(session);
            session.addMessageHandler(String.class, text -> receive(text));
        } catch (RuntimeException error) {
            onError(session, error);
        }
    }

    private void receive(String text) {
        try {
            JsonNode command = mapper.readTree(text);
            if (command == null || !command.isObject()) throw new IllegalArgumentException();
            String type = command.path("type").asText();
            if ("authenticate".equals(type)) {
                sockets.authenticate(peer, command.path("token").asText());
                return;
            }
            sockets.requireAuthenticated(peer);
            switch (type) {
                case "subscribe" -> sockets.subscribe(peer, projectId(command));
                case "unsubscribe" -> sockets.unsubscribe(peer, projectId(command));
                case "ping" -> sockets.send(peer, Map.of("type", "pong"));
                default -> sockets.send(peer, Map.of("type", "error", "code", "unknown_command"));
            }
        } catch (ForbiddenException error) {
            sockets.send(peer, Map.of("type", "error", "code", "project_access_denied"));
        } catch (IllegalArgumentException | com.auth0.jwt.exceptions.JWTVerificationException error) {
            sockets.disconnect(peer, "Autenticación o solicitud no válida");
        } catch (com.fasterxml.jackson.core.JsonProcessingException error) {
            sockets.send(peer, Map.of("type", "error", "code", "invalid_json"));
        } catch (RuntimeException error) {
            log.error("No se pudo procesar el comando WebSocket", error);
            sockets.send(peer, Map.of("type", "error", "code", "temporarily_unavailable"));
        }
    }

    private Long projectId(JsonNode command) {
        JsonNode id = command.path("projectId");
        if (!id.isIntegralNumber() || !id.canConvertToLong()) throw new IllegalArgumentException();
        return id.longValue();
    }

    @Override public void onClose(Session session, CloseReason reason) {
        sockets.remove(session.getId());
    }

    @Override public void onError(Session session, Throwable error) {
        log.debug("Conexión WebSocket cerrada por error", error);
        if (peer != null) sockets.disconnect(peer, "Error en la conexión");
        else {
            try { session.close(); } catch (java.io.IOException ignored) { }
        }
    }
}
```

## src/main/java/com/open_f/fu/shared/realtime/EventsEndpointConfigurator.java

[Abrir archivo del ejemplo](D:/Dev/JavaOpenF/examples/kafka-websocket/src/main/java/com/open_f/fu/shared/realtime/EventsEndpointConfigurator.java)

```java
package com.open_f.fu.shared.realtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.open_f.fu.shared.messaging.KafkaSettings;
import jakarta.websocket.server.ServerEndpointConfig;

public final class EventsEndpointConfigurator extends ServerEndpointConfig.Configurator {
    private final KafkaSettings settings;
    private final ProjectSockets sockets;
    private final ObjectMapper mapper;

    public EventsEndpointConfigurator(KafkaSettings settings, ProjectSockets sockets, ObjectMapper mapper) {
        this.settings = settings;
        this.sockets = sockets;
        this.mapper = mapper;
    }

    @Override public boolean checkOrigin(String origin) {
        return origin != null && settings.allowedOrigins().contains(origin);
    }

    @Override public <T> T getEndpointInstance(Class<T> type) throws InstantiationException {
        if (type != EventsEndpoint.class) throw new InstantiationException("Endpoint no soportado");
        return type.cast(new EventsEndpoint(sockets, mapper));
    }
}
```

## src/main/java/com/open_f/fu/shared/realtime/ProjectSockets.java

[Abrir archivo del ejemplo](D:/Dev/JavaOpenF/examples/kafka-websocket/src/main/java/com/open_f/fu/shared/realtime/ProjectSockets.java)

```java
package com.open_f.fu.shared.realtime;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.open_f.fu.shared.security.JwtService;
import com.open_f.fu.workspace.service.ProjectAccessService;
import jakarta.websocket.CloseReason;
import jakarta.websocket.Session;

public final class ProjectSockets implements AutoCloseable {
    private final Map<String, Peer> peers = new ConcurrentHashMap<>();
    private final ObjectMapper mapper;
    private final JwtService jwt;
    private final ProjectAccessService access;

    public ProjectSockets(ObjectMapper mapper, JwtService jwt, ProjectAccessService access) {
        this.mapper = mapper;
        this.jwt = jwt;
        this.access = access;
    }

    public Peer open(Session session) {
        if (peers.size() >= 10_000) throw new IllegalStateException("Sin capacidad de conexión");
        session.setMaxIdleTimeout(120_000);
        session.setMaxTextMessageBufferSize(8_192);
        Peer peer = new Peer(session);
        peers.put(session.getId(), peer);
        send(peer, Map.of("type", "connected"));
        return peer;
    }

    public void authenticate(Peer peer, String token) {
        if (peer.userId != null) throw new IllegalArgumentException("Ya autenticado");
        var decoded = jwt.verifyToken(token);
        if (decoded.getExpiresAt() == null) throw new IllegalArgumentException("Falta expiración");
        long expiry = decoded.getExpiresAt().getTime();
        Long userId = Long.valueOf(decoded.getSubject());
        if (expiry <= System.currentTimeMillis() || userId <= 0) {
            throw new IllegalArgumentException("Token no válido");
        }
        peer.expiresAt = expiry;
        peer.userId = userId;
        send(peer, Map.of("type", "authenticated", "userId", userId));
    }

    public void subscribe(Peer peer, Long projectId) {
        requireAuthenticated(peer);
        if (projectId == null || projectId <= 0 || peer.projects.size() >= 32) {
            throw new IllegalArgumentException("Suscripción no válida");
        }
        access.requireOwner(peer.userId, projectId);
        peer.projects.add(projectId);
        send(peer, Map.of("type", "subscribed", "projectId", projectId));
    }

    public void unsubscribe(Peer peer, Long projectId) {
        requireAuthenticated(peer);
        if (projectId == null || projectId <= 0) throw new IllegalArgumentException("Proyecto no válido");
        peer.projects.remove(projectId);
        send(peer, Map.of("type", "unsubscribed", "projectId", projectId));
    }

    public void requireAuthenticated(Peer peer) {
        if (peer.userId == null || peer.expiresAt <= System.currentTimeMillis()) {
            throw new IllegalArgumentException("Autenticación requerida");
        }
    }

    public void broadcast(Long projectId, String json) {
        if (peers.values().stream().noneMatch(peer -> peer.projects.contains(projectId))) return;
        // Se vuelve a comprobar el propietario en cada evento, también tras un cambio de permisos.
        Long owner = access.ownerOf(projectId);
        if (owner == null) return;
        for (Peer peer : peers.values()) {
            if (!peer.projects.contains(projectId)) continue;
            if (peer.expiresAt <= System.currentTimeMillis() || !owner.equals(peer.userId)) {
                disconnect(peer, "La sesión o el permiso expiró");
            } else {
                peer.sender.offer(json);
            }
        }
    }

    public void send(Peer peer, Object message) {
        try {
            peer.sender.offer(mapper.writeValueAsString(message));
        } catch (JsonProcessingException error) {
            throw new IllegalStateException("No se pudo serializar la respuesta", error);
        }
    }

    public void expire() {
        long now = System.currentTimeMillis();
        for (Peer peer : peers.values()) {
            if (!peer.session.isOpen()) {
                remove(peer.session.getId());
            } else if ((peer.userId == null && now - peer.openedAt > 10_000)
                    || (peer.userId != null && now >= peer.expiresAt)) {
                disconnect(peer, "Autenticación expirada");
            }
        }
    }

    public void remove(String sessionId) {
        Peer peer = peers.remove(sessionId);
        if (peer != null) peer.sender.clear();
    }

    public void disconnect(Peer peer, String reason) {
        disconnect(peer, CloseReason.CloseCodes.VIOLATED_POLICY, reason);
    }

    private void disconnect(Peer peer, CloseReason.CloseCode code, String reason) {
        remove(peer.session.getId());
        try {
            peer.session.close(new CloseReason(code, reason));
        } catch (IOException ignored) { }
    }

    @Override public void close() {
        for (Peer peer : peers.values()) {
            disconnect(peer, CloseReason.CloseCodes.GOING_AWAY, "La aplicación se está deteniendo");
        }
    }

    public static final class Peer {
        private final Session session;
        private final WebSocketSender sender;
        private final long openedAt = System.currentTimeMillis();
        private final Set<Long> projects = ConcurrentHashMap.newKeySet();
        private volatile Long userId;
        private volatile long expiresAt;

        private Peer(Session session) {
            this.session = session;
            this.sender = new WebSocketSender(session);
        }
    }
}
```

## src/main/java/com/open_f/fu/shared/realtime/WebSocketSender.java

[Abrir archivo del ejemplo](D:/Dev/JavaOpenF/examples/kafka-websocket/src/main/java/com/open_f/fu/shared/realtime/WebSocketSender.java)

```java
package com.open_f.fu.shared.realtime;

import java.io.IOException;
import java.util.ArrayDeque;
import jakarta.websocket.CloseReason;
import jakarta.websocket.Session;

/** Una sola escritura en curso y una cola acotada por conexión. */
final class WebSocketSender {
    private static final int MAX_MESSAGES = 128;
    private static final int MAX_CHARS = 128 * 1024;
    private final Session session;
    private final ArrayDeque<String> pending = new ArrayDeque<>();
    private boolean sending;
    private boolean closed;
    private int chars;

    WebSocketSender(Session session) {
        this.session = session;
        session.getAsyncRemote().setSendTimeout(5_000);
    }

    void offer(String text) {
        boolean overflow;
        String first = null;
        synchronized (this) {
            if (closed || !session.isOpen()) return;
            overflow = text.length() > MAX_CHARS || pending.size() >= MAX_MESSAGES
                    || chars > MAX_CHARS - text.length();
            if (!overflow) {
                pending.addLast(text);
                chars += text.length();
                if (!sending) {
                    sending = true;
                    first = pending.getFirst();
                }
            }
        }
        if (overflow) {
            fail();
        } else if (first != null) {
            send(first);
        }
    }

    private void send(String text) {
        try {
            session.getAsyncRemote().sendText(text, result -> {
                if (!result.isOK()) {
                    fail();
                    return;
                }
                String next;
                synchronized (this) {
                    if (closed) return;
                    chars -= pending.removeFirst().length();
                    next = pending.peekFirst();
                    if (next == null) sending = false;
                }
                if (next != null) send(next);
            });
        } catch (RuntimeException error) {
            fail();
        }
    }

    synchronized void clear() {
        closed = true;
        pending.clear();
        chars = 0;
    }

    private void fail() {
        clear();
        try {
            session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY,
                    "No se pudo entregar el evento; vuelve a conectar"));
        } catch (IOException ignored) {
            // El endpoint o el barrido periódico eliminan la sesión cerrada.
        }
    }
}
```

## src/main/java/com/open_f/fu/shared/security/JwtService.java

[Abrir archivo del ejemplo](D:/Dev/JavaOpenF/examples/kafka-websocket/src/main/java/com/open_f/fu/shared/security/JwtService.java)

```java
package com.open_f.fu.shared.security;

import java.time.Instant;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

public class JwtService {
    private static final String ISSUER = "open-f";
    private static final long TTL_SECONDS = 3600;
    private final Algorithm algorithm;
    private final JWTVerifier verifier;

    public JwtService() {
        String secret = System.getenv("JWT_SECRET");
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("Configura JWT_SECRET con al menos 32 caracteres");
        }
        algorithm = Algorithm.HMAC256(secret);
        verifier = JWT.require(algorithm).withIssuer(ISSUER).build();
    }

    public String generateToken(Long userId, String username) {
        Instant now = Instant.now();
        return JWT.create().withIssuer(ISSUER).withSubject(userId.toString())
                .withClaim("username", username).withIssuedAt(now)
                .withExpiresAt(now.plusSeconds(TTL_SECONDS)).sign(algorithm);
    }

    public DecodedJWT verifyToken(String token) { return verifier.verify(token); }
}
```

## src/main/java/com/open_f/fu/workspace/api/ProjectRealtimeResource.java

[Abrir archivo del ejemplo](D:/Dev/JavaOpenF/examples/kafka-websocket/src/main/java/com/open_f/fu/workspace/api/ProjectRealtimeResource.java)

```java
package com.open_f.fu.workspace.api;

import java.net.URI;
import java.util.List;
import com.open_f.fu.shared.messaging.RealtimeServices;
import com.open_f.fu.shared.security.CurrentUser;
import com.open_f.fu.workspace.api.request.TaskCreateRequest;
import com.open_f.fu.workspace.api.response.TaskResponse;
import com.open_f.fu.workspace.service.ProjectAccessService.ColumnView;
import com.open_f.fu.workspace.service.TaskService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

@Path("/realtime/projects/{projectId}")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public final class ProjectRealtimeResource {
    private final RealtimeServices realtime;
    private final TaskService tasks;

    @Inject public ProjectRealtimeResource(RealtimeServices realtime, TaskService tasks) {
        this.realtime = realtime;
        this.tasks = tasks;
    }

    @GET public Snapshot snapshot(@PathParam("projectId") Long projectId,
            @Context SecurityContext security) {
        Long userId = CurrentUser.id(security);
        var columns = realtime.access().columns(userId, projectId);
        return new Snapshot(columns, tasks.getAll(projectId).stream().map(TaskResponse::from).toList());
    }

    @POST @Path("/columns/{columnId}/tasks")
    public Response create(@PathParam("projectId") Long projectId,
            @PathParam("columnId") Long columnId,
            @QueryParam("positionOrder") @DefaultValue("0") int positionOrder,
            @Context SecurityContext security, TaskCreateRequest request) {
        if (request == null) throw new BadRequestException("Falta la tarea");
        Long userId = CurrentUser.id(security);
        realtime.access().requireOwner(userId, projectId);
        var created = tasks.create(request.toCommand(projectId, columnId, userId, positionOrder));
        return Response.created(URI.create("project/" + projectId + "/tasks/" + created.getId()))
                .entity(TaskResponse.from(created)).build();
    }

    public record Snapshot(List<ColumnView> columns, List<TaskResponse> tasks) {}
}
```

## src/main/java/com/open_f/fu/workspace/domain/interfaces/TaskEvents.java

[Abrir archivo del ejemplo](D:/Dev/JavaOpenF/examples/kafka-websocket/src/main/java/com/open_f/fu/workspace/domain/interfaces/TaskEvents.java)

```java
package com.open_f.fu.workspace.domain.interfaces;

import com.open_f.fu.workspace.domain.model.Task;

/** El servicio registra lo ocurrido dentro de la misma transacción de la tarea. */
public interface TaskEvents {
    void created(Task task);
}
```

## src/main/java/com/open_f/fu/workspace/messaging/InvalidTaskEventException.java

[Abrir archivo del ejemplo](D:/Dev/JavaOpenF/examples/kafka-websocket/src/main/java/com/open_f/fu/workspace/messaging/InvalidTaskEventException.java)

```java
package com.open_f.fu.workspace.messaging;

public final class InvalidTaskEventException extends RuntimeException {
    public InvalidTaskEventException(Throwable cause) { super("Evento de tarea no válido", cause); }
}
```

## src/main/java/com/open_f/fu/workspace/messaging/OutboxTaskEvents.java

[Abrir archivo del ejemplo](D:/Dev/JavaOpenF/examples/kafka-websocket/src/main/java/com/open_f/fu/workspace/messaging/OutboxTaskEvents.java)

```java
package com.open_f.fu.workspace.messaging;

import com.open_f.fu.shared.messaging.RealtimeServices;
import com.open_f.fu.workspace.domain.interfaces.TaskEvents;
import com.open_f.fu.workspace.domain.model.Task;
import jakarta.inject.Inject;

public final class OutboxTaskEvents implements TaskEvents {
    private final RealtimeServices realtime;

    @Inject public OutboxTaskEvents(RealtimeServices realtime) { this.realtime = realtime; }

    @Override public void created(Task task) {
        realtime.outbox().append(TaskCreatedEvent.from(task));
    }
}
```

## src/main/java/com/open_f/fu/workspace/messaging/TaskCreatedEvent.java

[Abrir archivo del ejemplo](D:/Dev/JavaOpenF/examples/kafka-websocket/src/main/java/com/open_f/fu/workspace/messaging/TaskCreatedEvent.java)

```java
package com.open_f.fu.workspace.messaging;

import java.time.Instant;
import java.util.UUID;
import com.open_f.fu.workspace.domain.model.Task;

public record TaskCreatedEvent(String eventId, String type, int version,
        Long projectId, String occurredAt, TaskData data) {

    public static TaskCreatedEvent from(Task task) {
        return new TaskCreatedEvent(UUID.randomUUID().toString(), "task.created", 1,
                task.getProjectId(), Instant.now().toString(),
                new TaskData(task.getId(), task.getCode(), task.getTitle(),
                        task.getDescription(), task.getPriority().name(), task.getPositionY(),
                        task.getColumnId(), task.getUserId()));
    }

    public record TaskData(Long id, String code, String title, String description,
            String priority, double positionY, Long columnId, Long userId) {}
}
```

## src/main/java/com/open_f/fu/workspace/messaging/TaskEventsHandler.java

[Abrir archivo del ejemplo](D:/Dev/JavaOpenF/examples/kafka-websocket/src/main/java/com/open_f/fu/workspace/messaging/TaskEventsHandler.java)

```java
package com.open_f.fu.workspace.messaging;

import java.time.Instant;
import java.util.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.open_f.fu.shared.realtime.ProjectSockets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Adaptador del módulo workspace: JSON Kafka -> evento validado -> conexiones del proyecto. */
public final class TaskEventsHandler {
    private static final Logger log = LoggerFactory.getLogger(TaskEventsHandler.class);
    private final ObjectMapper mapper;
    private final ProjectSockets sockets;

    public TaskEventsHandler(ObjectMapper mapper, ProjectSockets sockets) {
        this.mapper = mapper;
        this.sockets = sockets;
    }

    public void handle(String json) {
        TaskCreatedEvent event;
        try {
            event = mapper.readValue(json, TaskCreatedEvent.class);
            if (event == null || !"task.created".equals(event.type()) || event.version() != 1
                    || event.projectId() == null || event.projectId() <= 0
                    || event.data() == null || event.data().id() == null || event.data().id() <= 0
                    || event.data().title() == null) {
                throw new IllegalArgumentException("Esquema no soportado");
            }
            UUID.fromString(event.eventId());
            Instant.parse(event.occurredAt());
        } catch (Exception error) {
            throw new InvalidTaskEventException(error);
        }
        // Los errores de DB/infraestructura conservan su tipo para poder reintentarse.
        sockets.broadcast(event.projectId(), json);
        log.info("Evento {} procesado: task.created, proyecto {}, tarea {}",
                event.eventId(), event.projectId(), event.data().id());
    }
}
```

## src/main/java/com/open_f/fu/workspace/messaging/TaskEventsRoute.java

[Abrir archivo del ejemplo](D:/Dev/JavaOpenF/examples/kafka-websocket/src/main/java/com/open_f/fu/workspace/messaging/TaskEventsRoute.java)

```java
package com.open_f.fu.workspace.messaging;

import java.time.Instant;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.open_f.fu.shared.messaging.KafkaPublisher;
import com.open_f.fu.shared.messaging.KafkaSettings;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.camel.component.kafka.consumer.KafkaManualCommit;

public final class TaskEventsRoute extends RouteBuilder {
    private final KafkaSettings settings;
    private final TaskEventsHandler handler;
    private final KafkaPublisher publisher;
    private final ObjectMapper mapper;

    public TaskEventsRoute(KafkaSettings settings, TaskEventsHandler handler,
            KafkaPublisher publisher, ObjectMapper mapper) {
        this.settings = settings;
        this.handler = handler;
        this.publisher = publisher;
        this.mapper = mapper;
    }

    @Override public void configure() {
        // Un JSON/esquema inválido no mejorará reintentándolo.
        // Confirmamos el origen solo después de que Kafka acepte la copia en DLT.
        onException(InvalidTaskEventException.class)
                .maximumRedeliveries(0).handled(true).useOriginalMessage()
                .process(this::deadLetterAndCommit);

        // Un fallo transitorio no se convierte en un evento inválido.
        // Si persiste, breakOnFirstError conserva el offset para reintentarlo.
        onException(Exception.class)
                .maximumRedeliveries(3).redeliveryDelay(1_000).handled(false);

        from("kafka:" + KafkaSettings.TASKS_TOPIC + "?groupId=" + settings.consumerGroup()
                + "&autoCommitEnable=false&allowManualCommit=true&breakOnFirstError=true"
                + "&autoOffsetReset=earliest&maxPollRecords=25&consumersCount=1")
                .routeId("task-events")
                .process(exchange -> handler.handle(exchange.getMessage().getBody(String.class)))
                .process(TaskEventsRoute::commit);
    }

    private void deadLetterAndCommit(Exchange exchange) throws Exception {
        String raw = exchange.getMessage().getBody(String.class);
        String key = exchange.getMessage().getHeader(KafkaConstants.KEY, String.class);
        String deadLetter = mapper.writeValueAsString(Map.of(
                "originalEvent", raw,
                "reason", "invalid_task_event",
                "sourceTopic", KafkaSettings.TASKS_TOPIC,
                "partition", String.valueOf(exchange.getMessage().getHeader(KafkaConstants.PARTITION)),
                "offset", String.valueOf(exchange.getMessage().getHeader(KafkaConstants.OFFSET)),
                "failedAt", Instant.now().toString()));
        publisher.publishAndWait(KafkaSettings.DEAD_LETTER_TOPIC, key, deadLetter);
        commit(exchange);
    }

    private static void commit(Exchange exchange) {
        KafkaManualCommit manual = exchange.getMessage()
                .getHeader(KafkaConstants.MANUAL_COMMIT, KafkaManualCommit.class);
        if (manual == null) throw new IllegalStateException("Falta el commit manual de Kafka");
        // La ruta conserva el hilo del consumidor; no usar threads()/parallelProcessing() aquí.
        manual.commit();
    }
}
```

## src/main/java/com/open_f/fu/workspace/service/ProjectAccessService.java

[Abrir archivo del ejemplo](D:/Dev/JavaOpenF/examples/kafka-websocket/src/main/java/com/open_f/fu/workspace/service/ProjectAccessService.java)

```java
package com.open_f.fu.workspace.service;

import java.util.List;
import com.open_f.fu.shared.persistence.PersistenceManager;
import com.open_f.fu.workspace.persistence.entity.ProjectColumnEntity;
import jakarta.ws.rs.ForbiddenException;

/** La aplicación todavía no define miembros: el ejemplo solo permite al propietario. */
public final class ProjectAccessService {
    public Long ownerOf(Long projectId) {
        return PersistenceManager.getEntityManagerFactory().callInTransaction(em ->
                em.createQuery("select p.user.id from ProjectEntity p where p.id = :id", Long.class)
                        .setParameter("id", projectId).getResultStream().findFirst().orElse(null));
    }

    public void requireOwner(Long userId, Long projectId) {
        if (userId == null || !userId.equals(ownerOf(projectId))) {
            throw new ForbiddenException("Sin acceso al proyecto");
        }
    }

    public List<ColumnView> columns(Long userId, Long projectId) {
        requireOwner(userId, projectId);
        return PersistenceManager.getEntityManagerFactory().callInTransaction(em ->
                em.createQuery("select c from ProjectColumnEntity c where c.project.id = :id "
                        + "order by c.positionY", ProjectColumnEntity.class)
                        .setParameter("id", projectId).getResultStream()
                        .map(c -> new ColumnView(c.getId(), c.getName())).toList());
    }

    public record ColumnView(Long id, String name) {}
}
```

## src/main/java/com/open_f/fu/workspace/service/TaskService.java

[Abrir archivo del ejemplo](D:/Dev/JavaOpenF/examples/kafka-websocket/src/main/java/com/open_f/fu/workspace/service/TaskService.java)

```java
package com.open_f.fu.workspace.service;

import com.open_f.fu.account.service.UserService;
import com.open_f.fu.shared.persistence.TransactionManager;
import com.open_f.fu.shared.util.CodeGenerator;
import com.open_f.fu.workspace.domain.exception.ColumnNotFoundException;
import com.open_f.fu.workspace.domain.exception.ProjectNotFoundException;
import com.open_f.fu.workspace.domain.exception.TaskNotFoundException;
import com.open_f.fu.workspace.domain.interfaces.ProjectColumnRepository;
import com.open_f.fu.workspace.domain.interfaces.ProjectRepository;
import com.open_f.fu.workspace.domain.interfaces.TaskRepository;
import com.open_f.fu.workspace.domain.interfaces.TaskEvents;
import com.open_f.fu.workspace.domain.model.Project;
import com.open_f.fu.workspace.domain.model.ProjectColumn;
import com.open_f.fu.workspace.domain.model.Task;
import com.open_f.fu.workspace.domain.model.TaskPositioning;
import com.open_f.fu.workspace.domain.model.TaskPriority;
import com.open_f.fu.workspace.service.command.TaskCreateCommand;
import com.open_f.fu.workspace.service.command.TaskUpdateCommand;

import com.open_f.fu.workspace.service.command.TaskUpdatePositionCommand;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.RecursiveTask;

public class TaskService {

    private final TaskRepository tasks;
    private final ProjectColumnRepository columns;
    private final ProjectRepository projects;
    private final UserService userService; // otro módulo: solo por su servicio
    private final TransactionManager tx;
    private final TaskEvents events;

    @Inject
    public TaskService(TaskRepository tasks, ProjectColumnRepository columns, ProjectRepository projects, UserService userService, TransactionManager tx, TaskEvents events) {
        this.tasks = tasks;
        this.columns = columns;
        this.projects = projects;
        this.userService = userService;
        this.tx = tx;
        this.events = events;
    }

    public List<Task> getAll(Long projectId) {
        return tx.inTransaction(() -> {
            Project project = projects.findById(projectId).orElseThrow(() -> new ProjectNotFoundException(projectId));

            return tasks.findTasksByProjectId(project.getId());
        });
    }

    public Task create(TaskCreateCommand cmd) {
        TaskPriority priority = TaskPriority.from(cmd.priority());

        return tx.inTransaction(() -> {
            ProjectColumn column = columns.findByIdAndProjectId(cmd.columnId(), cmd.projectId()).orElseThrow(() -> new ColumnNotFoundException(cmd.columnId(), cmd.projectId()));
            Project project = projects.findById(cmd.projectId()).orElseThrow(() -> new ProjectNotFoundException(cmd.projectId()));

            // Si el usuario no existe, el autor es el propietario del proyecto
            Long userId = userService.exists(cmd.userId()) ? cmd.userId() : project.getUserId();

            double positionY = TaskPositioning.positionAt(tasks.findPositionsByColumnId(column.getId()), cmd.positionOrder());

            Task task = Task.create(CodeGenerator.generate(), cmd.title(), cmd.description(), priority, positionY, cmd.projectId(), column.getId(), userId);

            Task created = tasks.save(task);
            events.created(created);
            return created;
        });
    }

    public Task update(TaskUpdateCommand cmd) {
        return tx.inTransaction(() -> {
            Task task = tasks.findById(cmd.id()).orElseThrow(() -> new TaskNotFoundException(cmd.id()));

            task.edit(cmd.title(), cmd.description());

            return tasks.save(task);
        });
    }

    public Task updatePosition(TaskUpdatePositionCommand cmd) {
        return tx.inTransaction(() -> {
            Task task = tasks.findById(cmd.id()).orElseThrow(() -> new TaskNotFoundException(cmd.id()));

            task.edit(cmd.title(), cmd.description());
            task.moveTo(TaskPositioning.positionAt(tasks.findPositionsByColumnId(task.getColumnId()), cmd.positionOrder()));

            return tasks.save(task);
        });
    }

    public boolean taskExistsOnProject(Long taskId, Long projectId) {
        return this.tx.inTransaction(() -> {
            Optional<Task> task = tasks.findById(taskId);
            return task.filter(value -> tasks.existsByProjectId(value, projectId)).isPresent();
        });
    }
}
```

## src/main/webapp/realtime-demo.html

[Abrir archivo del ejemplo](D:/Dev/JavaOpenF/examples/kafka-websocket/src/main/webapp/realtime-demo.html)

```html
<!doctype html>
<html lang="es">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>JavaOpenF · Kafka y WebSocket</title>
  <style>
    :root { font-family: system-ui, sans-serif; color: #e2e8f0; background: #0f172a; }
    body { max-width: 960px; margin: 2rem auto; padding: 0 1rem; }
    h1 { font-size: 1.7rem; margin-bottom: .4rem; }
    p { color: #cbd5e1; line-height: 1.5; }
    section { background: #1e293b; padding: 1.2rem; margin: 1rem 0; border-radius: .7rem; }
    h2 { margin-top: 0; font-size: 1.1rem; }
    label { display: inline-flex; flex-direction: column; gap: .3rem; margin: .4rem .6rem .4rem 0; }
    input, select, button { font: inherit; padding: .6rem; border-radius: .35rem; border: 1px solid #64748b; }
    input, select { background: #0f172a; color: inherit; max-width: 100%; }
    button { background: #38bdf8; color: #082f49; cursor: pointer; margin: .3rem; }
    button:disabled { opacity: .4; cursor: default; }
    #status { font-weight: 600; color: #7dd3fc; }
    li { padding: .4rem 0; }
    pre { white-space: pre-wrap; overflow-wrap: anywhere; max-height: 380px; overflow: auto; font-size: .85rem; }
  </style>
</head>
<body>
  <h1>Crear una tarea y recibir su evento</h1>
  <p>Abre esta página en dos pestañas e inicia sesión con la misma cuenta.
    Conecta ambas al mismo proyecto y crea una tarea en una de ellas.</p>
  <p id="status" role="status">Sin sesión</p>

  <section>
    <h2>1. Iniciar sesión</h2>
    <label>Email <input id="email" type="email" autocomplete="username"></label>
    <label>Contraseña <input id="password" type="password" autocomplete="current-password"></label>
    <button id="login">Entrar</button>
    <button id="register">Registrar cuenta de prueba</button>
    <button id="logout">Salir</button>
    <p>El registro usa el nombre «Demo Kafka». El token solo se conserva en memoria en esta pestaña.</p>
  </section>

  <section>
    <h2>2. Elegir un proyecto propio</h2>
    <label>Nombre de proyecto nuevo <input id="projectTitle" value="Demo Kafka" maxlength="15"></label>
    <button id="createProject" disabled>Crear proyecto</button>
    <br>
    <label>ID de proyecto <input id="projectId" type="number" min="1" step="1"></label>
    <button id="connect" disabled>Conectar WebSocket</button>
    <button id="disconnect">Desconectar</button>
  </section>

  <section>
    <h2>3. Crear tarea</h2>
    <label>Columna <select id="columnId"></select></label>
    <label>Título <input id="taskTitle" value="Tarea desde Kafka" maxlength="240"></label>
    <button id="createTask" disabled>Crear tarea</button>
    <button id="refresh" disabled>Actualizar listado</button>
    <ul id="tasks"></ul>
  </section>

  <section>
    <h2>Eventos recibidos</h2>
    <pre id="events" aria-live="polite"></pre>
  </section>

  <script>
    const $ = id => document.getElementById(id);
    const apiBase = new URL('api/', location.href);
    let token = null;
    let socket = null;
    let ready = false;
    let heartbeat = null;
    let reconnect = null;
    let reconnectAllowed = false;
    let attempts = 0;
    let snapshotBusy = false;
    let snapshotRequested = false;
    const seen = new Set();

    function log(label, data) {
      const line = `${new Date().toLocaleTimeString()} ${label}`
        + (data === undefined ? '' : ' ' + JSON.stringify(data));
      $('events').textContent = (line + '\n' + $('events').textContent)
        .split('\n').slice(0, 200).join('\n');
    }

    function projectId() {
      const id = Number($('projectId').value);
      if (!Number.isSafeInteger(id) || id <= 0) throw new Error('Indica un ID de proyecto válido');
      return id;
    }

    function buttons() {
      $('createProject').disabled = !token;
      $('connect').disabled = !token;
      $('createTask').disabled = !ready || !$('columnId').value;
      $('refresh').disabled = !ready;
    }

    async function api(path, method = 'GET', body, authenticated = true) {
      const headers = { 'Content-Type': 'application/json' };
      if (authenticated && token) headers.Authorization = 'Bearer ' + token;
      const response = await fetch(new URL(path, apiBase), {
        method, headers, body: body === undefined ? undefined : JSON.stringify(body)
      });
      const text = await response.text();
      let data;
      try { data = text ? JSON.parse(text) : null; } catch { data = text; }
      if (!response.ok) throw new Error(`HTTP ${response.status}: ${text}`);
      return data;
    }

    function disconnect() {
      reconnectAllowed = false;
      clearTimeout(reconnect);
      clearInterval(heartbeat);
      const old = socket;
      socket = null;
      ready = false;
      if (old) old.close(1000, 'Desconexión solicitada');
      buttons();
    }

    async function login() {
      disconnect();
      token = null;
      buttons();
      const result = await api('users/login', 'POST', {
        email: $('email').value, password: $('password').value
      }, false);
      token = result.token;
      seen.clear();
      $('status').textContent = 'Sesión iniciada. Elige un proyecto y conecta.';
      log('Sesión iniciada');
      buttons();
    }

    async function refreshSnapshot() {
      if (!ready || !socket) return;
      if (snapshotBusy) { snapshotRequested = true; return; }
      snapshotBusy = true;
      const current = socket;
      const id = projectId();
      try {
        const snapshot = await api(`realtime/projects/${id}`);
        if (socket !== current || !ready) return;
        const previousColumn = $('columnId').value;
        $('columnId').replaceChildren(...snapshot.columns.map(column => {
          const option = document.createElement('option');
          option.value = column.id;
          option.textContent = column.name;
          return option;
        }));
        if ([...$('columnId').options].some(option => option.value === previousColumn)) {
          $('columnId').value = previousColumn;
        }
        $('tasks').replaceChildren(...snapshot.tasks.map(task => {
          const item = document.createElement('li');
          item.textContent = `#${task.id} · ${task.title} · ${task.priority}`;
          return item;
        }));
        buttons();
      } catch (error) {
        log('No se pudo actualizar el listado', error.message);
      } finally {
        snapshotBusy = false;
        if (snapshotRequested) {
          snapshotRequested = false;
          void refreshSnapshot();
        }
      }
    }

    function openSocket() {
      const id = projectId();
      const url = new URL('ws/events', location.href);
      url.protocol = location.protocol === 'https:' ? 'wss:' : 'ws:';
      const current = new WebSocket(url);
      socket = current;
      ready = false;
      buttons();
      $('status').textContent = 'Conectando…';

      current.onopen = () => {
        if (socket !== current) return;
        current.send(JSON.stringify({ type: 'authenticate', token }));
      };

      current.onmessage = event => {
        if (socket !== current) return;
        const message = JSON.parse(event.data);
        log('WS', message);
        if (message.type === 'authenticated') {
          current.send(JSON.stringify({ type: 'subscribe', projectId: id }));
        } else if (message.type === 'subscribed') {
          ready = true;
          attempts = 0;
          $('status').textContent = `Conectado al proyecto ${id}`;
          clearInterval(heartbeat);
          heartbeat = setInterval(() => {
            if (current.readyState === WebSocket.OPEN) {
              current.send(JSON.stringify({ type: 'ping' }));
            }
          }, 25_000);
          buttons();
          void refreshSnapshot();
        } else if (message.type === 'task.created' && message.projectId === id) {
          if (seen.has(message.eventId)) return;
          seen.add(message.eventId);
          if (seen.size > 1000) seen.delete(seen.values().next().value);
          // Leemos el estado actual: evita duplicados y datos viejos tras un replay.
          void refreshSnapshot();
        } else if (message.type === 'error') {
          $('status').textContent = message.code;
        }
      };

      current.onclose = event => {
        if (socket !== current) return;
        socket = null;
        ready = false;
        clearInterval(heartbeat);
        buttons();
        $('status').textContent = `Desconectado (${event.code}): ${event.reason}`;
        log('WS cerrado', { code: event.code, reason: event.reason });
        if (reconnectAllowed && token && event.code !== 1008) {
          const delay = Math.min(30_000, 1000 * 2 ** Math.min(attempts++, 5));
          reconnect = setTimeout(() => {
            try { openSocket(); } catch (error) { log('Error', error.message); }
          }, delay);
        }
      };
      current.onerror = () => log('Error de conexión WebSocket');
    }

    function action(id, callback) {
      $(id).onclick = async () => {
        try { await callback(); }
        catch (error) { log('Error', error.message); $('status').textContent = error.message; }
      };
    }

    action('login', login);
    action('register', async () => {
      await api('users/register', 'POST', {
        email: $('email').value, password: $('password').value,
        name: 'Demo', last_name: 'Kafka'
      }, false);
      await login();
    });
    action('logout', () => {
      disconnect(); token = null; seen.clear();
      $('tasks').replaceChildren();
      $('status').textContent = 'Sin sesión'; buttons();
    });
    action('createProject', async () => {
      const project = await api('project', 'POST', {
        title: $('projectTitle').value, description: 'Proyecto de prueba Kafka'
      });
      disconnect();
      $('projectId').value = project.id;
      $('status').textContent = `Proyecto ${project.id} creado. Conecta el WebSocket.`;
      log('Proyecto creado', project);
    });
    action('connect', () => {
      projectId(); disconnect(); reconnectAllowed = true; openSocket();
    });
    action('disconnect', () => { disconnect(); $('status').textContent = 'Desconectado'; });
    action('refresh', refreshSnapshot);
    action('createTask', async () => {
      const task = await api(`realtime/projects/${projectId()}/columns/${$('columnId').value}/tasks`,
        'POST', { title: $('taskTitle').value, description: '', priority: 'MEDIUM' });
      log('HTTP: tarea guardada, esperando el evento Kafka', { taskId: task.id });
    });
    $('projectId').addEventListener('change', disconnect);
    window.addEventListener('beforeunload', disconnect);
    buttons();
  </script>
</body>
</html>
```

## src/main/webapp/WEB-INF/web.xml

[Abrir archivo del ejemplo](D:/Dev/JavaOpenF/examples/kafka-websocket/src/main/webapp/WEB-INF/web.xml)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="https://jakarta.ee/xml/ns/jakartaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="
           https://jakarta.ee/xml/ns/jakartaee
           https://jakarta.ee/xml/ns/jakartaee/web-app_6_0.xsd"
         version="6.0">

    <display-name>open_f</display-name>

    <listener>
        <listener-class>
            com.open_f.fu.config.PersistenceListener
        </listener-class>
    </listener>

    <!-- Se registra después de persistencia y se detiene antes que ella. -->
    <listener>
        <listener-class>com.open_f.fu.config.RealtimeBootstrapListener</listener-class>
    </listener>

</web-app>
```

