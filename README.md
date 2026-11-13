# JavaOpenF

Backend Java de un sistema Kanban, empaquetado como WAR para ejecutarse en Apache Tomcat.

## Tecnologías

| Tecnología | Uso |
| --- | --- |
| Java 21 | Lenguaje y versión de compilación. |
| Maven | Dependencias, compilación, ejecución de pruebas y generación de `open_f.war`. |
| Apache Tomcat 11 | Servidor de aplicaciones web para el despliegue local. |
| Jersey 4 y Jakarta REST | Endpoints de la API bajo `/api`. |
| HK2 | Inyección de dependencias. |
| Jackson | Serialización y lectura de JSON. |
| Jakarta Persistence, Hibernate y HikariCP | Persistencia y conexiones a la base de datos. |
| MySQL 8.4 y Docker Compose | Base de datos local con almacenamiento persistente en un volumen. |
| JWT y BCrypt | Autenticación y hash de contraseñas. |
| Logback | Logs de la aplicación. |
| Reactor Core y Reactor Netty | Dependencias disponibles para los ejercicios de reactividad de `webflux.md`; el arranque actual usa Jersey y Tomcat. |
| JUnit Jupiter y Mockito | Prueba unitaria de ejemplo para aprender a probar `TaskService`. |

## Requisitos

- JDK 21, con `JAVA_HOME` configurado y `java` disponible en el PATH.
- Maven disponible como `mvn`.
- Docker con soporte de Docker Compose.
- Apache Tomcat 11 instalado.

Comprueba las herramientas antes de iniciar:

```sh
java -version
mvn -version
docker compose version
```

## Levantar el proyecto

### 1. Iniciar MySQL

Desde la raíz del repositorio:

```sh
docker compose up -d data_db
docker compose ps
```

La configuración de desarrollo de `docker-compose.yml` utiliza:

| Dato | Valor |
| --- | --- |
| Host | `localhost` |
| Puerto | `3306` |
| Base de datos | `open_f` |
| Usuario | `root` |
| Contraseña | `1234` |

Las credenciales y las URLs de conexión están en `src/main/resources/META-INF/persistence.xml`. La URL JDBC apunta a `[::1]:3306` y la de HikariCP a `localhost:3306`; ambas deben resolver a la misma instancia. Si cambias el host, el puerto o las credenciales, actualiza los valores correspondientes para que coincidan con Compose.

El volumen `data_db` conserva los datos entre reinicios. Al desplegar la aplicación, el listener inicializa Hibernate y `hbm2ddl.auto=update` crea o actualiza las tablas.

### 2. Compilar el WAR

```sh
mvn clean package
```

El archivo generado es `target/open_f.war`. Este comando también ejecuta el único test de ejemplo incluido en el repositorio.

### 3. Ejecutar en Tomcat desde IntelliJ IDEA

1. Abre `pom.xml` como proyecto Maven y carga sus dependencias.
2. Selecciona JDK 21 para el proyecto.
3. En **Run → Edit Configurations**, agrega una configuración **Tomcat Server → Local** y selecciona tu instalación de Tomcat 11.
4. Configura estos valores:

   | Campo | Valor |
   | --- | --- |
   | JRE | `21` |
   | HTTP port | `8080` |
   | Deployment → Artifact | `open_f:war exploded` |
   | Application context | `/open_f` |
   | URL | `http://localhost:8080/open_f/api/health` |

5. En **Before launch**, conserva **Build** y la construcción del artefacto `open_f:war exploded`.
6. Ejecuta con **Run** o **Debug** y espera a que la consola indique que el artefacto fue desplegado correctamente.

Como alternativa, copia `target/open_f.war` al directorio `webapps` de Tomcat. En Windows puedes iniciar el servidor desde su carpeta de instalación con:

```powershell
.\bin\catalina.bat run
```

### 4. Comprobar la API

```sh
curl -i http://localhost:8080/open_f/api/health
```

La respuesta esperada es `200 OK`, con este cuerpo JSON:

```json
{"status":"UP"}
```

Este endpoint confirma que la API responde; no consulta la base de datos. Las rutas de registro y login son `POST /open_f/api/users/register` y `POST /open_f/api/users/login`. Las rutas protegidas de la API admiten `Authorization: Bearer <token>`.

### 5. Detener el entorno

Detén Tomcat desde IntelliJ o con `Ctrl+C` si lo ejecutaste con `catalina.bat run`. Para detener MySQL conservando su volumen:

```sh
docker compose stop data_db
```

## Ejecutar el test de ejemplo

```sh
mvn -Dtest=TaskServiceTest test
```

[TaskServiceTest](src/test/java/com/open_f/fu/workspace/service/TaskServiceTest.java) contiene un solo caso comentado: editar el título y la descripción de una tarea existente y verificar que se guarde. JUnit comprueba el resultado y Mockito sustituye las dependencias; no requiere MySQL ni Tomcat. Las demás pruebas quedan como ejercicio del equipo.

El alcance funcional, los criterios de aceptación y el reparto de tareas están en [las historias de usuario](docs/HISTORIAS_USUARIO.md).
