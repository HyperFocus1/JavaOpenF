# Reactividad en Open F: aprender las piezas detrás de WebFlux

## Finalidad

Aprender Reactor y Reactor Netty directamente para entender qué añade Spring WebFlux y qué configura Spring Boot. El laboratorio cubre flujos de datos, peticiones entrantes, eventos, consumo HTTP y adaptación de los servicios existentes.

Se agregan dependencias y esta guía; las tareas quedan por implementar como práctica. No se agrega `spring-webflux`, `spring-context`, `WebClient`, Spring Boot ni R2DBC. La aplicación conserva Jersey, HK2, Hibernate/JPA y el WAR para Tomcat.

## Qué hay debajo de WebFlux

| Pieza | Responsabilidad |
| --- | --- |
| Reactive Streams | Contratos entre productor y consumidor: suscripción, demanda, datos, errores y finalización. |
| Reactor Core | `Mono`, `Flux`, operadores, schedulers y composición de secuencias. |
| Netty | Infraestructura de red y event loops. |
| Reactor Netty | Cliente y servidor de red con una API basada en Reactor; uno de los transportes posibles de WebFlux. |
| Spring WebFlux | Capa superior con enrutamiento, controladores, filtros, codecs y adaptación de respuestas. No se instala en esta etapa. |
| Spring Boot | Automatiza la selección y configuración de componentes. No se instala en esta etapa. |

Con Reactor Netty puedes atender HTTP y consumir APIs sin Spring. No tendrás `@RestController`, `RouterFunction` ni `WebClient`: implementarás las rutas con `HttpServer` y las llamadas con `HttpClient`. Esto permite estudiar el trabajo de la capa de transporte antes de usar las abstracciones de WebFlux.

## Dependencias agregadas

| Dependencia | Para qué se usará |
| --- | --- |
| `io.projectreactor:reactor-core` | Flujos, operadores, demanda y schedulers. |
| `io.projectreactor.netty:reactor-netty-http` | Servidor HTTP, cliente HTTP, SSE y WebSocket. Trae Netty transitivamente. |
| `io.projectreactor:reactor-test` (test) | `StepVerifier`, tiempo virtual y comprobación de señales. |
| `org.junit.jupiter:junit-jupiter` (test) | Ejecutar los ejercicios con Maven y el IDE. |

El BOM `io.projectreactor:reactor-bom:2025.0.8` alinea las versiones de Reactor Core, Reactor Test y Reactor Netty. No se fijan versiones de Netty manualmente. Surefire ejecuta las pruebas JUnit Jupiter. La compilación del proyecto sigue dirigida a Java 21.

Agregar estas librerías no arranca otro servidor ni modifica el procesamiento de las rutas Jersey actuales.

## Organización y ejecución

Crear las prácticas en `src/test/java/com/open_f/fu/learning/` para que las clases del laboratorio no se incluyan en el WAR. Los nombres de archivos que siguen son propuestas para los ejercicios, no clases ya creadas.

1. Recargar el proyecto Maven en el IDE.
2. Ejecutar `mvn dependency:tree` para localizar Reactor Core, Reactor Netty y sus dependencias transitivas.
3. Crear y ejecutar las pruebas con `mvn test` o desde el IDE. Antes de escribirlas, Maven puede terminar sin pruebas.
4. Crear `ReactorNettyLab.java` en esas fuentes de test y ejecutar su `main` desde el IDE, con el classpath de test, para las prácticas HTTP manuales.
5. Usar un servidor independiente en `127.0.0.1:8081`. La API actual sigue ejecutándose de la forma descrita en el README.

Las tareas 1 a 6 no requieren MySQL ni GitHub. Los datos ficticios y un proveedor HTTP local permiten repetir los ejercicios sin depender de terceros.

## 1. Mono, Flux y ejecución diferida

**Usabilidad:** representar el resultado de una operación y una secuencia de cambios de tarjetas.

- [ ] Crear `ReactorBasicsTest`: partir de títulos de tareas, filtrar vacíos y convertir los restantes a mayúsculas.
- [ ] Comprobar valores, orden y finalización con `StepVerifier`.
- [ ] Crear un `Mono.fromCallable` con un contador: comprobar que el trabajo se ejecuta al suscribirse, no al construir el flujo.
- [ ] Comparar `Mono.just(calcular())` con `Mono.fromCallable(() -> calcular())`: identificar cuándo se llama a `calcular` y cómo se propagan sus errores.
- [ ] Suscribirse dos veces a una fuente fría y observar que puede ejecutar el trabajo dos veces.

**Cierre:** explicar por qué `Mono` no es el dato mismo y por qué envolver algo en `Mono` no lo hace automáticamente asíncrono o no bloqueante.

Primer ejercicio para `ReactorBasicsTest.java`:

```java
package com.open_f.fu.learning;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

class ReactorBasicsTest {
    @Test
    void transformaTitulos() {
        var titulos = Flux.just("Crear tablero", "", "Mover tarjeta")
                .filter(titulo -> !titulo.isBlank())
                .map(String::toUpperCase);

        StepVerifier.create(titulos)
                .expectNext("CREAR TABLERO", "MOVER TARJETA")
                .expectComplete()
                .verify(Duration.ofSeconds(3));
    }
}
```

## 2. Composición, errores y tiempo

**Usabilidad:** combinar información independiente para el resumen de un tablero.

- [ ] Simular dos lecturas con `Mono.delay` y combinarlas con `Mono.zip`.
- [ ] Usar `map` para transformar un valor y `flatMap` para encadenar una operación que devuelve otro publisher.
- [ ] Comparar concurrencia y orden con `flatMap` y `concatMap` cuando los resultados tardan distinto.
- [ ] Añadir un tiempo límite y una alternativa para un error esperado; no ocultar cualquier fallo con una lista vacía.
- [ ] Probar demoras y tiempos límite con `StepVerifier.withVirtualTime`. Crear la fuente temporal dentro de su supplier.

**Cierre:** comprobar resultados y fallos sin esperas reales largas, y explicar por qué dos pasos dependientes no se pueden iniciar a la vez.

## 3. Atender peticiones HTTP con Reactor Netty

**Usabilidad:** construir una API propia, no solo consumir APIs externas.

- [ ] Crear `ReactorNettyLab.java` y arrancar este servidor desde el IDE.
- [ ] Consultar `curl.exe http://127.0.0.1:8081/learning/ping`.
- [ ] Añadir una ruta con un parámetro y otra que devuelva un error de validación; definir códigos y cabeceras explícitamente.
- [ ] Comprobar con `HttpClient` y `StepVerifier` tanto el código HTTP como el cuerpo. Usar un puerto asignado por el sistema (`port(0)`) y cerrar el servidor al terminar cada prueba.
- [ ] Leer un body JSON acotado y convertirlo con el Jackson existente. Decidir el límite de tamaño antes de acumular datos en memoria.

```java
package com.open_f.fu.learning;

import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServer;

public class ReactorNettyLab {
    public static void main(String[] args) {
        var server = HttpServer.create()
                .host("127.0.0.1")
                .port(8081)
                .route(routes -> routes.get("/learning/ping", (req, res) ->
                        res.header("Content-Type", "text/plain; charset=UTF-8")
                                .sendString(Mono.just("pong"))))
                .bindNow();
        try {
            server.onDispose().block();
        } finally {
            server.disposeNow();
        }
    }
}
```

**Cierre:** obtener `pong`, comprobar el 404 de una ruta desconocida y distinguir el arranque bloqueante del proceso del procesamiento no bloqueante de las peticiones.

Este servidor no recibe automáticamente recursos, filtros JWT o inyección HK2 de Jersey. No se registra en `web.xml`; se ejecuta como un proceso de práctica independiente. Los recursos JAX-RS tampoco interpretan `Flux` por el simple hecho de agregar Reactor.

## 4. Streaming del Kanban con SSE

**Usabilidad:** comunicar cambios al navegador conforme ocurren, sin consultar periódicamente todo el tablero.

- [ ] Añadir `GET /learning/events` y responder con `text/event-stream` mediante el soporte SSE de Reactor Netty.
- [ ] Empezar con una secuencia ficticia de eventos de tarjetas usando `Flux.interval`.
- [ ] Serializar correctamente cada evento SSE: por ejemplo, `data: tarjeta-movida\n\n`. No confundir el framing de SSE con enviar JSON normal.
- [ ] Verificar la llegada progresiva con `curl.exe -N http://127.0.0.1:8081/learning/events`; comprobar que no se acumula todo hasta finalizar.
- [ ] Abrir dos clientes, desconectar uno y observar su cancelación con `doFinally` o `doOnCancel`.
- [ ] Probar tres eventos y cancelar con `StepVerifier`; no esperar el final de una secuencia infinita.

**Cierre:** los eventos llegan mientras la respuesta permanece abierta y desconectar un cliente libera los recursos de su suscripción.

SSE envía datos del servidor al cliente. Crear o mover tarjetas puede seguir llegando por REST. WebSocket es otra opción si se necesita comunicación bidireccional; no es requisito para esta tarea.

## 5. Demanda, eventos internos y clientes lentos

**Usabilidad:** distribuir eventos del tablero y decidir qué hacer cuando un consumidor no alcanza su ritmo.

- [ ] Usar una fuente controlable con `StepVerifier.create(flujo, 0)` y pedir elementos mediante `thenRequest`.
- [ ] Comparar una fuente fría con eventos compartidos mediante `Sinks.Many`.
- [ ] Publicar eventos ficticios por proyecto y comprobar que cada suscriptor recibe únicamente los de su proyecto.
- [ ] Elegir buffer acotado, descarte explícito o desconexión para clientes lentos; comprobar el resultado de `tryEmitNext`.
- [ ] Probar dos productores y definir cómo serializar emisiones si el sink rechaza emisiones concurrentes.

**Cierre:** demostrar que el buffer no crece indefinidamente, identificar qué eventos pueden perderse y explicar cómo recargar el estado del tablero.

Backpressure controla demanda entre componentes compatibles. No hace que cualquier productor se pueda frenar ni garantiza conocer la velocidad de renderizado del navegador mediante SSE. Un sink en memoria no ofrece persistencia, reenvío al reconectar ni distribución entre varias instancias.

## 6. Cliente HTTP y concurrencia limitada

**Usabilidad:** consultar repositorios de GitHub o reunir información de otros servicios.

- [ ] Crear un proveedor HTTP local con respuestas demoradas y casos 200, 404, 429 y 503, usando otro `HttpServer`.
- [ ] Consultarlo con `reactor.netty.http.client.HttpClient`. Leer estado y body con `responseSingle`, liberando/consumiendo correctamente la respuesta.
- [ ] Clasificar los códigos de error explícitamente: el cliente de transporte no define por ti las reglas de negocio.
- [ ] Procesar una lista con `flatMap(id -> consultar(id), 5)`. Medir llamadas activas con un contador que se incremente por suscripción y se reduzca también al fallar o cancelar.
- [ ] Configurar tiempos límite y reintentos acotados únicamente para fallos transitorios elegidos. Evitar duplicar escrituras al reintentar.
- [ ] Como extensión, reemplazar el proveedor local por GitHub y respetar su autenticación y cuotas.

**Cierre:** demostrar un máximo de cinco operaciones activas y explicar por qué eso no equivale a cinco peticiones por segundo. Una cuota temporal necesita otro control.

`WebClient` es una abstracción de Spring que puede usar este transporte por debajo. En esta etapa se aprende directamente el transporte con `HttpClient`.

## 7. Usar los servicios JPA existentes desde el servidor reactivo

**Usabilidad:** atender una petición con los casos de uso actuales sin migrar MySQL ni los repositorios.

Hibernate/JPA con JDBC sigue siendo bloqueante. Aislar una llamada completa al servicio con `fromCallable` y `boundedElastic`. Por ejemplo, suponiendo que `messageService` ya está construido:

```java
Mono.fromCallable(() -> messageService.allMessages(channelId))
        .subscribeOn(Schedulers.boundedElastic())
        .flatMapMany(Flux::fromIterable);
```

El servicio carga primero su lista con JPA. Convertirla después en `Flux` no transforma la consulta en un cursor reactivo ni elimina el bloqueo del hilo de persistencia.

- [ ] Empezar con un servicio bloqueante ficticio en una prueba y verificar que corre fuera del event loop.
- [ ] Construir explícitamente los servicios y repositorios de la práctica, o resolverlos mediante una instancia controlada de HK2. El servidor independiente no hereda el contenedor de Jersey.
- [ ] Inicializar `PersistenceManager.init()` y cerrarlo al terminar: en este proceso no se ejecuta `PersistenceListener` de `web.xml`.
- [ ] Envolver la llamada completa al servicio, incluidos el inicio y fin de la transacción. Devolver modelos de dominio, sin entidades con relaciones lazy pendientes.
- [ ] Comprobar un resultado y un fallo con datos locales de prueba.
- [ ] Relacionar la concurrencia admitida con el pool de conexiones y el scheduler. Más llamadas concurrentes no crean más conexiones disponibles.

**Cierre:** conservar los contratos síncronos y ejecutar toda la transacción en un mismo hilo de trabajo.

Tu `JpaTransactionManager` usa `ThreadLocal<EntityManager>`: no abrir la transacción fuera del supplier ni introducir saltos de scheduler dentro de ella. No compartir `EntityManager` entre operaciones concurrentes. `Mono.just(servicio.consultar())` ejecutaría el trabajo antes de aplicar el scheduler; usar `fromCallable`.

R2DBC es otra tecnología de persistencia con otro driver y modelo transaccional. No es JPA con retornos reactivos. No se agrega R2DBC ni `ReactiveCrudRepository`: los `Jpa*Repository` actuales son implementaciones propias, no interfaces de Spring Data JPA. Una migración sería una práctica separada y opcional.

## 8. Eventos reales del Kanban y autorización

**Usabilidad:** mostrar a los miembros autorizados un movimiento confirmado de una tarjeta.

- [ ] Definir un evento con proyecto, tarjeta, tipo de cambio e identificador o versión.
- [ ] Emitirlo únicamente después de que termine correctamente la transacción del caso de uso. Un rollback no debe anunciar un movimiento exitoso.
- [ ] Autorizar la suscripción por proyecto y obtener la identidad del token; no confiar en un `userId` enviado por el cliente.
- [ ] Reutilizar la verificación de `JwtService` adaptándola al nuevo transporte. `JwtAuthenticationFilter` de JAX-RS no protege rutas Netty automáticamente. Aislar cualquier operación bloqueante de autenticación.
- [ ] Probar dos clientes autorizados, un usuario de otro proyecto y una transacción fallida.
- [ ] Recargar el tablero al reconectar. Documentar la posible pérdida entre commit y emisión o al reiniciar el proceso.

**Cierre:** solo los clientes con permisos ven el cambio confirmado. Para aprender basta un sink en memoria y una instancia; entrega durable, outbox y broker son temas posteriores.

## 9. Comparar con WebFlux y decidir dónde usar reactividad

**Usabilidad:** entender qué problema resuelve cada capa y defender una elección técnica.

- [ ] Comparar un handler bloqueante con otro que use espera de red no bloqueante, manteniendo iguales respuesta, demora, concurrencia y duración de la medición.
- [ ] Registrar latencia, errores y cantidad de hilos; no concluir solo con una petición.
- [ ] Comparar también un cálculo intensivo de CPU y explicar por qué envolverlo en `Mono` no lo acelera.
- [ ] Identificar qué sigue bloqueando cuando se usa el puente JPA.
- [ ] Leer cómo WebFlux resuelve enrutamiento, codecs, validación y errores, y señalar qué has tenido que hacer manualmente en Netty.
- [ ] Como etapa posterior y separada, decidir si incorporar WebFlux para repetir una ruta y comparar el código. No hace falta instalarlo para completar las tareas anteriores.

**Cierre:** justificar qué mantendrías en Jersey/JPA y qué parte se beneficiaría de reactividad. En una arquitectura de microservicios cada servicio puede elegir según su trabajo; no todos necesitan WebFlux ni R2DBC.

## Reglas para los ejercicios

- Componer y devolver publishers en los handlers; evitar `block()`, `blockFirst()` y `subscribe()` manual para lanzar trabajo fuera del ciclo de la petición.
- Reactor Netty se suscribe al procesamiento HTTP; `StepVerifier` se suscribe en las pruebas. Los bloqueos controlados de arranque, espera y cierre del `main` tienen otro propósito.
- No usar `Thread.sleep` en el event loop. Para simular tiempo dentro de una cadena reactiva, usar operadores temporales.
- Limitar concurrencia, buffers y reintentos; probar error, cancelación y consumidor lento.
- Un timeout reactivo no garantiza detener inmediatamente una consulta JDBC ya iniciada.
- Consumir/liberar los datos de red y cerrar servidores, clientes y recursos propios de las pruebas. No retener `ByteBuf` fuera de su ciclo de vida.
- Mantener el dominio actual libre de Reactor; los adaptadores de aprendizaje envuelven sus servicios síncronos.

## Referencias oficiales

- [Reactor: configuración y BOM](https://projectreactor.io/docs/core/release/reference/gettingStarted.html).
- [Reactor: llamadas bloqueantes](https://projectreactor.io/docs/core/release/reference/faq.html#faq.wrap-blocking).
- [Reactor: pruebas y tiempo virtual](https://projectreactor.io/docs/core/release/reference/testing.html).
- [Reactor: Sinks](https://projectreactor.io/docs/core/release/reference/coreFeatures/sinks.html).
- [Reactor Netty: servidor HTTP y SSE](https://projectreactor.io/docs/netty/release/reference/http-server.html).
- [Reactor Netty: cliente HTTP](https://projectreactor.io/docs/netty/release/reference/http-client.html).
- [WebFlux: modelo y componentes para comparar después](https://docs.spring.io/spring-framework/reference/web/webflux/new-framework.html).
