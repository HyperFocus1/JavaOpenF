# JavaOpenF

Backend Kanban con Java 21, Jakarta EE y SmallRye. Se empaqueta como `open_f.war` y se ejecuta en WildFly 42.

WildFly es el servidor elegido para este proyecto y reemplaza a Tomcat. Proporciona CDI, JTA, JPA y Jakarta REST; además, habilitamos SmallRye para integrar Kafka. En el entorno Docker, WildFly arranca y despliega el WAR automáticamente. Para trabajar desde IntelliJ se utiliza una configuración de servidor JBoss/WildFly.

## Levantar todo con Docker

Para un nuevo integrante basta con Docker Desktop iniciado y contenedores Linux. Java, Maven y WildFly se ejecutan dentro de la construcción y del contenedor.

Desde la raíz del repositorio:

```powershell
docker compose up --build -d --wait
docker compose ps -a
```

La primera ejecución descarga las imágenes y las dependencias Maven. La construcción ejecuta el único test de ejemplo, genera el WAR, instala el driver MySQL y configura SmallRye. Después Compose espera a que MySQL y Kafka estén disponibles, crea el topic `tasks.events` y arranca la API.

Comprueba la aplicación:

```powershell
Invoke-RestMethod http://localhost:8080/open_f/api/health
```

Respuesta esperada: `{"status":"UP"}`. El servicio `api` debe aparecer como `healthy` y `kafka_init` como finalizado con código `0`.

| Servicio | Acceso desde tu equipo |
| --- | --- |
| API REST | `http://localhost:8080/open_f/api` |
| WebSocket | `ws://localhost:8080/open_f/ws/projects/{projectId}/tasks` |
| MySQL | `localhost:3306`, base `open_f`, usuario `root`, contraseña de desarrollo `1234` |
| Kafka | `localhost:9092`, topic `tasks.events` |

Los puertos se publican en la interfaz local del equipo. Dentro de Docker, la API usa `data_db:3306` y `kafka:9094`; esas direcciones se configuran automáticamente en Compose.

Para seguir los logs:

```powershell
docker compose logs -f api
```

Después de cambiar el código, repite `docker compose up --build -d --wait` para reconstruir y desplegar la aplicación.

Para detener los servicios conservando sus datos:

```powershell
docker compose stop
```

Los volúmenes `data_db`, `kafka_data` y `jwt_data` conservan la base de datos, los eventos y el secreto JWT, respectivamente.

## Configuración opcional de Docker

El arranque funciona sin un archivo `.env`. Para personalizarlo, crea uno tomando `.env.example` como referencia:

| Variable | Valor predeterminado | Uso |
| --- | --- | --- |
| `OPENF_HTTP_PORT` | `8080` | Puerto de la API en tu equipo. |
| `OPENF_MYSQL_PORT` | `3306` | Puerto MySQL en tu equipo. |
| `OPENF_KAFKA_PORT` | `9092` | Puerto Kafka en tu equipo y dirección anunciada al cliente local. |
| `OPENF_DB_PASSWORD` | `1234` | Contraseña de desarrollo de MySQL y de la conexión de la API. |
| `JWT_SECRET` | Vacío | Si se define, debe contener al menos 32 caracteres. |
| `WEBSOCKET_ALLOWED_ORIGINS` | Origen del puerto HTTP elegido y `http://localhost:5173` | Orígenes que pueden abrir WebSocket. |

Cuando `JWT_SECRET` está vacío, el contenedor genera un valor aleatorio y lo conserva en `jwt_data`. Los reinicios y reconstrucciones mantienen ese valor. Si defines `JWT_SECRET`, se usa el valor que proporcionaste.

La contraseña de MySQL se establece al inicializar un volumen nuevo. Si ya tienes `data_db`, conserva la contraseña que utiliza esa base; cambiar una variable no cambia la contraseña de una cuenta existente.

## Tecnologías

| Tecnología | Uso |
| --- | --- |
| Java 21 y Maven | Compilación, dependencias y generación del WAR. |
| WildFly 42 / Jakarta EE 11 | CDI, JTA, JPA, REST y WebSocket. |
| CDI y `@Inject` | Descubrimiento de beans e inyección de dependencias. |
| JTA y `@Transactional` | Transacciones administradas por el servidor. |
| Jakarta Persistence e Hibernate | Persistencia mediante el datasource `OpenFDS`. |
| MySQL 8.4 | Base de datos. |
| SmallRye Reactive Messaging y Kafka | Publicación y consumo de eventos de tareas. |
| Jakarta WebSocket | Notificaciones a conexiones autorizadas por proyecto. |
| Jackson | JSON de los DTO mediante el proveedor de RESTEasy. |
| JWT y BCrypt | Autenticación y hash de contraseñas. |
| SLF4J | Logs administrados por WildFly. |
| Reactor Core y Reactor Netty | Dependencias disponibles para los ejercicios de reactividad. |
| JUnit Jupiter y Mockito | Un único test unitario de ejemplo de `TaskService`. |

## Datasource dentro del WAR

La definición está en [src/main/webapp/META-INF/openf-ds.xml](src/main/webapp/META-INF/openf-ds.xml). Maven lo empaqueta como `META-INF/openf-ds.xml`, en la raíz del WAR.

Ese descriptor configura la conexión, el pool y la participación en JTA. [persistence.xml](src/main/resources/META-INF/persistence.xml) utiliza la referencia `java:jboss/datasources/OpenFDS`. WildFly crea el datasource al desplegar el WAR y lo retira al desinstalar la aplicación.

Las variables `OPENF_DB_URL`, `OPENF_DB_USER` y `OPENF_DB_PASSWORD` permiten sobrescribir la conexión. El script de configuración enlaza esos valores con las propiedades JVM `openf.db.url`, `openf.db.user` y `openf.db.password`, que utiliza el parser del descriptor. Si quedan vacías, se aplican los valores del propio descriptor: `jdbc:mysql://localhost:3306/open_f`, `root` y `1234`.

El driver se registra como `mysql`, con la clase `com.mysql.cj.jdbc.Driver`. El script retira la antigua definición global de `OpenFDS` en instalaciones anteriores para que la aplicación gestione una sola definición.

WildFly 42 muestra `WFLYJCA0091` porque el formato desplegable `-ds.xml` está marcado como obsoleto. Este aviso no impide el despliegue.

Hibernate actualiza el esquema al desplegar (`hibernate.hbm2ddl.auto=update`), para este entorno de aprendizaje.

## Ejecutar WildFly desde IntelliJ o desde el equipo

Esta opción requiere JDK 21 con `JAVA_HOME`, Maven y la distribución estándar de [WildFly 42](https://www.wildfly.org/downloads/) descomprimida. Docker sigue proporcionando MySQL y Kafka.

### 1. Iniciar solamente la infraestructura

Si ya ejecutaste el entorno completo, detén su API para liberar el puerto 8080:

```powershell
docker compose stop api
docker compose up -d --wait data_db kafka kafka_init
```

Kafka anuncia `localhost:9092` a los clientes del equipo y `kafka:9094` a los contenedores. Ambos modos pueden usar la misma infraestructura.

### 2. Compilar y configurar WildFly

```powershell
mvn clean package
$env:WILDFLY_HOME = 'D:\Tools\wildfly-42.0.0.Final'
.\server\configure.ps1 -WildFlyHome $env:WILDFLY_HOME
```

Ajusta la ruta a tu instalación y ejecuta el configurador con WildFly detenido. El build genera `target/open_f.war` y `target/drivers/mysql-connector-j.jar`.

El script instala el módulo MySQL, habilita SmallRye y configura el enlace de las variables de conexión; el datasource permanece en el WAR. También instala los módulos de usuario `jackson-core` y `jackson-databind` 2.21.8 para corregir [CVE-2026-110879](https://github.com/FasterXML/jackson-databind/security/advisories/GHSA-m8xf-2cr7-77ph). Tienen prioridad sobre las copias 2.21.7 incluidas en WildFly 42. El BOM de Jackson en Maven mantiene las dependencias del WAR en la misma versión. Docker aplica este parche durante la construcción de la imagen.

Si ya configuraste una instalación local de WildFly, detén el servidor y vuelve a ejecutar `mvn clean package` y `server/configure.ps1` después de esta actualización. Desplegar solamente el WAR no actualiza los módulos del servidor.

### 3. Definir el entorno y arrancar

Define un secreto JWT de al menos 32 caracteres y conserva el mismo valor entre reinicios. Para generar uno inicialmente:

```powershell
$env:JWT_SECRET = [Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(48))
```

Las variables de conexión son opcionales cuando usas los valores de desarrollo predeterminados:

```powershell
$env:OPENF_DB_URL = 'jdbc:mysql://localhost:3306/open_f'
$env:OPENF_DB_USER = 'root'
$env:OPENF_DB_PASSWORD = '1234'
$env:KAFKA_BOOTSTRAP_SERVERS = 'localhost:9092'
```

Si cambiaste puertos o contraseña en Compose, utiliza esos mismos valores en el proceso de WildFly. El archivo `.env` lo lee Docker Compose; al ejecutar WildFly desde IntelliJ o PowerShell debes configurar sus variables en ese proceso.

En la misma terminal:

```powershell
Copy-Item -LiteralPath .\target\open_f.war -Destination (Join-Path $env:WILDFLY_HOME 'standalone/deployments/open_f.war') -Force
& "$env:WILDFLY_HOME\bin\standalone.bat" -c standalone-microprofile.xml
```

En IntelliJ utiliza **JBoss/WildFly Server → Local**, JDK 21, configuración `standalone-microprofile.xml`, contexto `/open_f` y las variables anteriores en **Environment variables**. Espera a que la consola confirme el despliegue.

Detén WildFly con `Ctrl+C` o desde IntelliJ. Para detener la infraestructura conservando los volúmenes, ejecuta `docker compose stop`.

## Crear una tarea y recibir el evento

Las rutas de autenticación existentes son `POST /open_f/api/users/register` y `POST /open_f/api/users/login`. Las rutas protegidas aceptan `Authorization: Bearer <token>`.

`/open_f/api/session` conserva las sesiones de navegador y la protección CSRF; sus cookies están configuradas como seguras.

Para comprobar el flujo con un usuario autenticado:

1. Crea un proyecto mediante `POST /open_f/api/project`, con `title` y `description`.
2. Usa el ID del proyecto y el de una de sus columnas. En el entorno predeterminado puedes consultarlas con:

   ```powershell
   docker compose exec data_db mysql -uroot -p1234 open_f -e 'SELECT id, project_id, name FROM project_column;'
   ```

3. Abre `ws://localhost:8080/open_f/ws/projects/{projectId}/tasks` desde un origen permitido.
4. Envía primero `{"type":"authenticate","token":"JWT_DEL_LOGIN"}`.
5. Espera `{"type":"ready"}` antes de crear la tarea. Envía `{"type":"ping"}` cada 25 segundos para mantener la conexión.
6. Crea la tarea:

   ```http
   POST /open_f/api/project/{projectId}/columns/{columnId}/tasks
   Authorization: Bearer <token>
   Content-Type: application/json

   {"title":"Aprender Kafka","description":"Evento de ejemplo","priority":"HIGH"}
   ```

El flujo es `TaskService → TaskEvents → commit JTA → TaskEventKafkaPublisher → tasks.events → TaskEventKafkaListener → WebSocket`.

Crear, editar y mover una tarea publican en el mismo topic y llegan al mismo WebSocket:

| Operación | Ruta REST (bajo `/open_f/api`) | Evento Java | Tipo en Kafka y WebSocket |
| --- | --- | --- | --- |
| Crear | `POST /project/{projectId}/columns/{columnId}/tasks` | `TaskCreated` | `task.created` |
| Editar | `PUT /project/{projectId}/tasks` | `TaskUpdated` | `task.updated` |
| Mover | `POST /project/{projectId}/tasks/move` | `TaskMoved` | `task.moved` |

`TaskService` recibe una sola dependencia `TaskEvents` para emitir eventos y llama a `created(saved)`, `updated(saved)` o `moved(saved)`. `TaskEvents` construye el record correspondiente y lo dispara mediante `Event<TaskCreated>`, `Event<TaskUpdated>` o `Event<TaskMoved>` de CDI. El publicador tiene los observadores `publishCreated()`, `publishUpdated()` y `publishMoved()`, todos con `AFTER_SUCCESS`; comparten la conversión a JSON y el envío.

El destino de `emitter.send(...)` está en `META-INF/microprofile-config.properties`: el canal interno `tasks-out` publica en el topic Kafka `tasks.events`. El canal `tasks-in` lee ese mismo topic y SmallRye llama a `TaskEventKafkaListener.listenFromKafka()`. El listener ejecuta `routeByEventType()` y selecciona `onTaskCreated()`, `onTaskUpdated()` u `onTaskMoved()` según el campo `type` del JSON. Cada método recibe su record Java y envía el mensaje al WebSocket del proyecto. La confirmación del envío a Kafka no confirma la recepción en el navegador.

Los eventos contienen `eventId`, `type`, `projectId`, `taskId`, `columnId`, `title`, `description` y `positionY`, con los valores guardados. `description` puede ser `null`. El cliente distingue las operaciones mediante `type` y utiliza `positionY` para ordenar las tareas. El movimiento actual cambia la posición dentro de la misma columna.

Para editar envía `{"taskId":123,"title":"Título actualizado","description":"Nuevo detalle"}`. Para mover utiliza la misma estructura con `positionOrder`, por ejemplo `{"taskId":123,"title":"Título actualizado","description":"Nuevo detalle","positionOrder":2}`. Sustituye los IDs por los de tu proyecto y tarea. La ruta de movimiento también conserva la edición de título y descripción que ya realizaba el servicio.

El propietario del proyecto es el usuario autorizado, dado que todavía no hay miembros por proyecto. El token y el permiso se revisan antes de entregar eventos. `GET /open_f/api/project/{projectId}/tasks` obtiene el estado actual después de conectar o reconectar.

Al ejecutar WildFly fuera de Docker, las variables opcionales son:

| Variable | Valor predeterminado |
| --- | --- |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` |
| `WEBSOCKET_ALLOWED_ORIGINS` | `http://localhost:8080,http://localhost:5173` |
| `WEBSOCKET_CONSUMER_GROUP` | `openf-websocket-node-1` |

Compose configura una sola instancia de API. Para varias instancias WebSocket, cada una necesita un grupo distinto para recibir todos los eventos y distribuirlos a sus conexiones locales. En HTTPS usa `wss://` y agrega el origen a la lista permitida.

## Alcance de la entrega

`AFTER_SUCCESS` evita publicar una creación, edición o movimiento que hizo rollback. Esta base todavía no implementa outbox: una caída entre el commit de MySQL y la publicación en Kafka puede perder la notificación. WebSocket tampoco confirma que el navegador haya aplicado el evento; al reconectar consulta REST y utiliza `eventId` para detectar duplicados si los necesitas.

El envío es secuencial, con una espera máxima de cinco segundos por conexión. Está pensado como base de aprendizaje para pocas conexiones. Un error de procesamiento del consumidor detiene su canal (`failure-strategy=fail`) y debe revisarse en los logs.

## Test de ejemplo

Docker ejecuta el test durante la construcción. Para ejecutarlo con Maven instalado en el equipo:

```powershell
mvn -Dtest=TaskServiceTest test
```

El único test utiliza JUnit y Mockito para editar una tarea, verificar su guardado y comprobar que delega únicamente la notificación de edición en un mock de `TaskEvents`. No inicia CDI ni verifica transacciones JTA reales; esas comprobaciones requieren el servidor.
