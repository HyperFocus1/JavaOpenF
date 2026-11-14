# Módulo `taskcontent` — Comentarios y archivos de una tarjeta

Módulo nuevo del Kanban. Cuelga de la tarjeta (`task_id`) y guarda lo que la gente deja sobre ella: **comentarios** y **archivos**. Cubre la HU-18 de [HISTORIAS_USUARIO.md](HISTORIAS_USUARIO.md) y añade la HU-24 (archivos), que no estaba en el alcance original.

Responsable: Jose Carlos. Sigue las reglas del [README](../README.md): mismas cuatro capas, `api → service → domain ← persistence`, ids entre módulos y uso de otros módulos solo por su `service`.

## Qué es y qué no es

| Es | No es |
| --- | --- |
| Comentarios de una tarjeta, con autor y fechas del servidor. | Un canal de chat. No crea canales ni mensajes del módulo `chat`. |
| Archivos ligados a una tarjeta: nombre, tamaño, tipo y URL. | Un almacén de binarios. El backend guarda metadatos; el binario vive fuera (ver decisión D-1). |
| Reglas de autoría: solo el autor edita o borra lo suyo. | Permisos de proyecto (miembros, roles). Eso es de `workspace`; cuando exista, se consulta por su `service`. |

## Relación con los demás módulos

```text
workspace.TaskEntity (project_task)  1 ─── 0..*  task_comments   (task_id, author_id)
                                     1 ─── 0..*  task_files      (task_id, author_id)
account.UserEntity   (users)         1 ─── 0..*  task_comments   (author_id)
                                     1 ─── 0..*  task_files      (author_id)
```

- `task_id` y `author_id` se guardan como `Long`. **No** hay `@ManyToOne` hacia `TaskEntity` ni `UserEntity`.
- Para validar que la tarjeta existe, `taskcontent.service` llama a `workspace.service.TaskService.taskExistsOnProject(taskId, projectId)`. Si hace falta un `TaskService.exists(taskId)` sin proyecto, se acuerda con Jair y lo agrega él en `workspace`.
- El `author_id` sale siempre del token: `CurrentUser.id(securityContext)`. El cliente nunca lo manda.
- `shared` sigue sin importar ningún módulo. `taskcontent` no importa `chat`.

## Historias de usuario

### HU-18 — Conversar en el contexto de una tarjeta

**Como** integrante, **quiero** comentar una tarjeta, **para** dejar información vinculada al trabajo.

- [ ] Puedo agregar comentarios no vacíos con autor y fechas del servidor; el historial se consulta ordenado y paginado.
- [ ] Solo el autor puede editar/eliminar su comentario mientras conserve acceso al proyecto; los demás pueden leerlo.
- [ ] Los comentarios se asocian a la tarjeta y funcionan sin crear un canal de chat general.

Criterios técnicos derivados:

- [ ] Un comentario vacío, solo espacios o mayor a 4000 caracteres devuelve 400 y no se guarda.
- [ ] Un `task_id` inexistente devuelve 404 antes de guardar nada.
- [ ] Editar o borrar un comentario ajeno devuelve 403 (`NotCommentAuthorException`).
- [ ] El listado devuelve los comentarios de más antiguo a más nuevo, con `page` y `size` (máximo 100); `page` fuera de rango devuelve lista vacía.
- [ ] Al editar, `updated_at` cambia y `created_at` se conserva; la respuesta expone ambos.
- [ ] Borrar es físico en esta etapa (sin `deleted_at`). Cuando llegue HU-20 (archivo) se revisa.

### HU-24 — Adjuntar archivos a una tarjeta

**Como** integrante, **quiero** adjuntar archivos a una tarjeta, **para** dejar evidencias y material del trabajo junto a ella.

- [ ] Puedo registrar un archivo con nombre, tamaño, tipo MIME y URL; el autor y las fechas los pone el servidor.
- [ ] Puedo listar los archivos de una tarjeta, ordenados del más nuevo al más antiguo.
- [ ] Solo el autor puede eliminar su archivo mientras conserve acceso al proyecto; los demás pueden verlo.
- [ ] Los archivos se asocian a la tarjeta por `task_id` y no dependen de comentarios ni de chat.

Criterios técnicos derivados:

- [ ] Nombre vacío, tamaño ≤ 0 o mayor a 25 MB, o URL inválida devuelven 400 y no se guarda.
- [ ] Un `task_id` inexistente devuelve 404.
- [ ] Eliminar un archivo ajeno devuelve 403 (`NotFileAuthorException`).
- [ ] El backend no recibe ni devuelve el binario; solo metadatos (decisión D-1).

## Modelo de datos

### `task_comments`

| Columna | Tipo | Regla |
| --- | --- | --- |
| `id` | BIGINT, PK, identity | — |
| `task_id` | BIGINT, not null, índice | id de `project_task`; sin FK JPA |
| `author_id` | BIGINT, not null | id de `users`; sin FK JPA |
| `content` | TEXT, not null | 1 a 4000 caracteres, sin espacios al borde |
| `created_at` | DATETIME, not null | de `BaseEntity` |
| `updated_at` | DATETIME, not null | de `BaseEntity` |

### `task_files`

| Columna | Tipo | Regla |
| --- | --- | --- |
| `id` | BIGINT, PK, identity | — |
| `task_id` | BIGINT, not null, índice | id de `project_task`; sin FK JPA |
| `author_id` | BIGINT, not null | id de `users`; sin FK JPA |
| `name` | VARCHAR(255), not null | nombre original del archivo |
| `size_bytes` | BIGINT, not null | > 0 y ≤ 26 214 400 (25 MB) |
| `mime_type` | VARCHAR(100), not null | ej. `image/png`, `application/pdf` |
| `url` | VARCHAR(2048), not null | dónde está el binario |
| `created_at` | DATETIME, not null | de `BaseEntity` |
| `updated_at` | DATETIME, not null | de `BaseEntity` |

Hibernate crea las tablas con `hbm2ddl.auto=update`. No se toca `persistence.xml`.

## Contrato HTTP

Todas las rutas bajo `/api` y con `Authorization: Bearer <token>`.

### Comentarios

| Método | Ruta | Body | Respuesta |
| --- | --- | --- | --- |
| POST | `/tasks/{taskId}/comments` | `{ "content": "..." }` | 201 + `CommentResponse` |
| GET | `/tasks/{taskId}/comments?page=0&size=20` | — | 200 + `[CommentResponse]` |
| PUT | `/comments/{id}` | `{ "content": "..." }` | 200 + `CommentResponse` |
| DELETE | `/comments/{id}` | — | 204 |

`CommentResponse`:

```json
{
  "id": 12,
  "task_id": 7,
  "author_id": 3,
  "content": "Falta el test del mapper",
  "created_at": "2026-11-14T22:10:05Z",
  "updated_at": "2026-11-14T22:10:05Z"
}
```

### Archivos

| Método | Ruta | Body | Respuesta |
| --- | --- | --- | --- |
| POST | `/tasks/{taskId}/files` | `{ "name", "size_bytes", "mime_type", "url" }` | 201 + `FileResponse` |
| GET | `/tasks/{taskId}/files` | — | 200 + `[FileResponse]` |
| DELETE | `/files/{id}` | — | 204 |

`FileResponse`:

```json
{
  "id": 4,
  "task_id": 7,
  "author_id": 3,
  "name": "diagrama.png",
  "size_bytes": 184320,
  "mime_type": "image/png",
  "url": "https://storage.example.com/open_f/7/diagrama.png",
  "created_at": "2026-11-14T22:12:40Z",
  "updated_at": "2026-11-14T22:12:40Z"
}
```

### Errores

Todos pasan por `shared/error/ApiExceptionMapper` y salen como `ApiError`.

| Caso | Excepción | HTTP |
| --- | --- | --- |
| Contenido o metadatos inválidos | `DomainException` | 400 |
| Tarjeta, comentario o archivo inexistente | `TaskNotFoundException`, `CommentNotFoundException`, `FileNotFoundException` | 404 |
| No es el autor | `NotCommentAuthorException`, `NotFileAuthorException` | 403 (hereda de `UnauthorizedException` o se agrega `ForbiddenException` en `shared/domain`) |
| Sin token | filtro JWT | 401 |

## Estructura de archivos

```text
src/main/java/com/open_f/fu/taskcontent/
  TaskContentBinder.java
  api/
    TaskCommentResource.java          @Path("/tasks/{taskId}/comments") y /comments/{id}
    TaskFileResource.java             @Path("/tasks/{taskId}/files") y /files/{id}
    request/
      CommentCreateRequest.java
      CommentUpdateRequest.java
      FileCreateRequest.java
    response/
      CommentResponse.java
      FileResponse.java
  service/
    TaskCommentService.java
    TaskFileService.java
    command/
      FileCreateCommand.java          4 valores o más → command
  domain/
    model/
      TaskComment.java
      TaskFile.java
    interfaces/
      TaskCommentRepository.java
      TaskFileRepository.java
    exception/
      CommentNotFoundException.java
      FileNotFoundException.java
      NotCommentAuthorException.java
      NotFileAuthorException.java
  persistence/
    entity/
      TaskCommentEntity.java
      TaskFileEntity.java
    mapper/
      TaskCommentMapper.java
      TaskFileMapper.java
    repository/
      JpaTaskCommentRepository.java
      JpaTaskFileRepository.java
```

Cambios fuera del módulo:

- `RestConfig.java`: agregar `"com.open_f.fu.taskcontent.api"` a `packages(...)` y `register(new TaskContentBinder())`.
- `workspace/service/TaskService.java` (Jair): `exists(Long taskId)` si `taskExistsOnProject` no alcanza.

## Flujo de un comentario

```text
POST /tasks/7/comments  { "content": "Falta el test" }
  │
  ▼
TaskCommentResource.create(taskId=7, securityContext, req)
  │  authorId = CurrentUser.id(securityContext)        ← del token, nunca del body
  ▼
TaskCommentService.create(taskId=7, authorId=3, content)
  │  tx.inTransaction:
  │    taskService.exists(7)        → false → TaskNotFoundException (404)
  │    TaskComment.create(7, 3, content)  → valida: no vacío, ≤ 4000
  │    comments.save(comment)
  ▼
JpaTaskCommentRepository.save
  │  TaskCommentMapper.toNewEntity → em.persist → TaskCommentMapper.toDomain
  ▼
201 + CommentResponse.from(comment)
```

Edición y borrado:

```text
PUT /comments/12  { "content": "..." }
  → service.update(id=12, actorId=3, content)
     → comments.findById(12)  → vacío → CommentNotFoundException (404)
     → comment.edit(content, actorId)  → actorId != authorId → NotCommentAuthorException (403)
     → comments.save(comment)
```

La regla "solo el autor" vive en `TaskComment.edit(...)` y `TaskComment.assertAuthor(actorId)`, no en el Resource ni en el Service.

## Decisiones

| # | Decisión | Por qué |
| --- | --- | --- |
| D-1 | El backend guarda **metadatos** del archivo, no bytes. El cliente sube el binario a un almacén (S3, disco del servidor, etc.) y registra la URL. | MySQL no es para binarios; mantiene el WAR simple; el almacén se decide en P2 sin tocar este módulo. |
| D-2 | Un solo módulo `taskcontent` con dos agregados, no dos módulos. | Comparten `task_id`, reglas de autoría y el mismo Binder. Dos módulos duplicarían todo. |
| D-3 | Nada de `@ManyToOne` a `TaskEntity`. | Regla del README: entre módulos solo ids. |
| D-4 | Borrado físico por ahora. | HU-20 (archivo/baja lógica) todavía no está; se agrega `deleted_at` cuando aplique a todo el Kanban. |
| D-5 | Rutas de edición y borrado por id plano (`/comments/{id}`), no anidadas. | El id ya es único; evita validar dos veces el `taskId`. |

## Orden de implementación

Comentarios primero, de adentro hacia afuera. Cada paso compila solo con `mvn -q compile`. Archivos repite el molde.

| # | Archivo | Molde a copiar | Java que se aprende |
| --- | --- | --- | --- |
| 1 | `domain/model/TaskComment.java` | `chat/domain/model/Channel.java` | clase, `final`, `static` factory, `throw` |
| 2 | `domain/exception/*.java` | `chat/domain/exception/ChannelNotFoundException.java` | herencia, `super(...)` |
| 3 | `domain/interfaces/TaskCommentRepository.java` | `chat/domain/interfaces/ChannelRepository.java` | `interface`, `Optional`, `List` |
| 4 | `persistence/entity/TaskCommentEntity.java` | `chat/persistence/entity/ChannelEntity.java` | `@Entity`, `@Table`, `extends BaseEntity` |
| 5 | `persistence/mapper/TaskCommentMapper.java` | `chat/persistence/mapper/ChannelMapper.java` | métodos `static` |
| 6 | `persistence/repository/JpaTaskCommentRepository.java` | `chat/persistence/repository/JpaChannelRepository.java` | JPQL, `setFirstResult`, `setMaxResults` |
| 7 | `service/TaskCommentService.java` | `chat/service/ChannelService.java` | `@Inject`, lambdas, `tx.inTransaction` |
| 8 | `api/request/*`, `api/response/*` | `chat/api/request/ChannelCreateRequest.java` | `record` |
| 9 | `api/TaskCommentResource.java` | `chat/api/ChannelResource.java` | `@Path`, `@QueryParam`, `@Context SecurityContext` |
| 10 | `TaskContentBinder.java` + `RestConfig.java` | `chat/ChatsBinder.java` | HK2 `bind().to()` |
| 11 | `TaskFile` completo (pasos 1 a 9 otra vez) | lo hecho en comentarios | repaso |
| 12 | Tests de `TaskComment`, `TaskFile` y los Resource | `account/api/SessionResourceTest.java` | JUnit |

## Cómo verificar

```sh
cd /d C:\Users\jose_\Desktop\JavaOpenF
docker compose up -d data_db
mvn clean package
```

Desplegar `target\open_f.war` en Tomcat, hacer login y:

```sh
curl -X POST http://localhost:8080/open_f/api/tasks/1/comments -H "Authorization: Bearer <token>" -H "Content-Type: application/json" -d "{\"content\":\"Primer comentario\"}"
curl http://localhost:8080/open_f/api/tasks/1/comments?page=0^&size=20 -H "Authorization: Bearer <token>"
curl -X PUT http://localhost:8080/open_f/api/comments/1 -H "Authorization: Bearer <token>" -H "Content-Type: application/json" -d "{\"content\":\"Editado\"}"
curl -X DELETE http://localhost:8080/open_f/api/comments/1 -H "Authorization: Bearer <token>"
curl -X POST http://localhost:8080/open_f/api/tasks/1/files -H "Authorization: Bearer <token>" -H "Content-Type: application/json" -d "{\"name\":\"diagrama.png\",\"size_bytes\":184320,\"mime_type\":\"image/png\",\"url\":\"https://example.com/diagrama.png\"}"
curl http://localhost:8080/open_f/api/tasks/1/files -H "Authorization: Bearer <token>"
```

Casos que deben fallar:

| Petición | Esperado |
| --- | --- |
| POST comentario con `"content": "   "` | 400 |
| POST comentario en `/tasks/9999/comments` | 404 |
| PUT comentario de otro usuario | 403 |
| DELETE archivo de otro usuario | 403 |
| POST archivo con `size_bytes: 0` | 400 |
| Cualquier ruta sin token | 401 |

## Pendientes con el equipo

- [ ] Jair: confirmar `TaskService.exists(taskId)` o acordar usar `taskExistsOnProject` con `projectId` en la ruta.
- [ ] Equipo: agregar `ForbiddenException` (403) a `shared/domain` si no se reutiliza `UnauthorizedException`.
- [ ] Equipo: agregar `TaskCommentEntity` ya está en el UML; agregar `TaskFileEntity/task_files` a `diagram_db.puml`.
- [ ] Equipo: incluir HU-24 en `HISTORIAS_USUARIO.md` cuando se apruebe.
