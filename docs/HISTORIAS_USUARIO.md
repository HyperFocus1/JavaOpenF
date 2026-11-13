# Historias de usuario — Sistema Kanban

Alcance de la primera etapa: completar el Kanban para una plataforma que acompañará a personas desde su idea hasta un proyecto para portfolio, aprendizaje u otros objetivos propios. El [README](../README.md) describe el producto completo, con la entrada futura por agente, especificaciones Markdown, repositorio GitHub/GitLab y planificación asistida.

**Objetivo de aprendizaje:** estas historias definen qué construir y qué debe poder hacer el usuario. El desarrollo de las funcionalidades, las decisiones técnicas y su validación corresponden a los desarrolladores del equipo. La guía acompaña ese aprendizaje sin sustituir la implementación que realizarán.

Un proyecto representa un tablero. El estado de una tarjeta se obtiene de su columna; el autor es quien la crea, mientras que el responsable es quien la atiende. P0 entrega la base operativa; P1 también debe completarse para cerrar el Kanban. El chat general y las ampliaciones P2 tienen seguimiento separado.

`HU-00` a `HU-23` son las historias del Kanban. La primera entrega permite organizar el trabajo manualmente y conserva su historial operativo; no exige disponer de agente, repositorio conectado ni bitácora ampliada.

## Reglas comunes de aceptación

Todas las historias con datos privados requieren identidad autenticada y permiso en el proyecto. Cada operación debe validar entradas, devolver errores consistentes, persistir sin cambios parciales y mantener aislamiento entre proyectos. Lectura, modificación y borrado por ID también aplican esas reglas. El cliente no decide la identidad del actor.

El [UML del Kanban y chats](diagram_db.puml) reúne las clases existentes y las nuevas de miembros, invitaciones, etiquetas, checklist, comentarios e historial, con atributos básicos. El equipo completará los datos que requiera cada funcionalidad; el dibujo no reduce los criterios de aceptación ni declara implementadas las propuestas. La identidad y la infraestructura técnica se concretan durante el desarrollo.

Los criterios describen resultados verificables; no afirman que ya existan endpoints. Los contratos HTTP se documentarán conforme se implementen.

## P0 — Tablero operativo

### HU-00 — Acceder con una cuenta

**Como** usuario, **quiero** registrarme, iniciar/cerrar sesión y mantener mi perfil, **para** gestionar mis tableros con una identidad confiable.

- [ ] Un registro válido crea una cuenta; correo duplicado normalizado, formato incorrecto o campos vacíos producen un error sin crear cuentas parciales.
- [ ] La contraseña se almacena como hash; ninguna respuesta devuelve contraseña ni hash.
- [ ] Credenciales incorrectas, sesión expirada o cerrada impiden usar recursos protegidos.
- [ ] Solo puedo editar/desactivar mi perfil según las reglas acordadas; desactivar no deja proyectos sin propietario ni destruye autoría histórica.

### HU-01 — Crear un tablero listo para trabajar

**Como** usuario autenticado, **quiero** crear un proyecto con columnas iniciales, **para** empezar a organizar tarjetas sin configurar todo desde cero.

- [ ] Un título válido crea el proyecto y me asigna como propietario desde la sesión.
- [ ] Aparecen Por hacer, En progreso, En revisión y Finalizado, en ese orden y con sus colores iniciales.
- [ ] La política sobre la tarjeta de bienvenida es explícita y consistente con la respuesta.
- [ ] Si falla la creación de una columna o tarjeta inicial, no queda un proyecto parcialmente creado.

### HU-02 — Administrar mis proyectos

**Como** propietario, **quiero** listar, buscar y editar mis proyectos, **para** mantener organizado el trabajo.

- [ ] El listado paginado y la búsqueda muestran solo proyectos accesibles; una búsqueda sin coincidencias devuelve una lista vacía.
- [ ] Puedo consultar detalle y modificar título/descripción; los valores inválidos no se guardan.
- [ ] El borrado requiere permiso de propietario y confirmación en el cliente, informa su alcance y aplica baja lógica (`deleted_at`), oculta proyecto e hijos y conserva auditoría/métricas; la purga física es un proceso administrativo separado.
- [ ] Un ID inexistente o ajeno no permite consultar, modificar ni borrar recursos privados.

### HU-03 — Ver el tablero y su orden real

**Como** usuario con acceso, **quiero** consultar columnas y tarjetas ordenadas, **para** conocer el estado actual del trabajo.

- [ ] La respuesta identifica proyecto, columnas y tarjetas, con código, título, descripción, prioridad, autor y posición necesarios para el cliente.
- [ ] Las columnas y tarjetas tienen orden estable, incluso cuando dos posiciones coinciden; recargar conserva el resultado.
- [ ] Una columna vacía se representa sin error y no desaparece del tablero.
- [ ] La respuesta no expone credenciales ni falla por serializar relaciones lazy fuera de su contexto.

### HU-04 — Adaptar las columnas al flujo

**Como** propietario, **quiero** crear, editar, reordenar y eliminar columnas, **para** representar cómo trabaja mi equipo.

- [ ] Puedo definir nombre/color válidos y cambiar el orden; los cambios sobreviven a la recarga.
- [ ] No puedo modificar columnas de otro proyecto ni guardar posiciones o colores inválidos.
- [ ] Puedo borrar una columna vacía; si contiene tarjetas debo indicar una columna destino del mismo proyecto.
- [ ] Trasladar tarjetas y borrar su columna es atómico; no se permite dejar un tablero activo sin columnas.

### HU-05 — Gestionar las tarjetas

**Como** usuario autorizado, **quiero** crear, consultar, editar y eliminar tarjetas, **para** describir y mantener cada unidad de trabajo.

- [ ] Al crear, se guardan título, descripción y prioridad recibidos; no se reemplaza mi descripción con un texto fijo.
- [ ] Obtengo ID, código único dentro del proyecto, columna, autor y posición. Una colisión de código se resuelve sin duplicados.
- [ ] Puedo editar título, descripción y prioridad; el autor no cambia por un dato enviado desde el cliente.
- [ ] Solo puedo usar columnas del proyecto y prioridades válidas; puedo eliminar según permisos mediante baja lógica, sin datos huérfanos y conservando el historial para las métricas.

### HU-06 — Mover y ordenar tarjetas

**Como** usuario autorizado, **quiero** mover tarjetas dentro de una columna o a otra del tablero, **para** reflejar el avance y la prioridad de atención.

- [ ] Mover al inicio, entre tarjetas, al final o a una columna vacía persiste el orden esperado.
- [ ] La tarjeta movida se excluye del cálculo de vecinos y no aparece duplicada ni se pierde al recargar.
- [ ] Índices inválidos y columnas de otro proyecto producen error sin modificar el tablero.
- [ ] Cambiar columna y posición ocurre en una transacción; se resuelven empates o agotamiento de precisión sin desordenar tarjetas.

### HU-07 — Trabajar sin sobrescribir cambios ajenos

**Como** integrante del tablero, **quiero** que los cambios simultáneos se controlen, **para** evitar pérdida de trabajo.

- [ ] Si dos peticiones cambian la misma tarjeta o el mismo orden desde una versión anterior, se serializan de forma segura o se informa conflicto recuperable.
- [ ] El cliente puede recargar el estado vigente y reintentar; no quedan tarjetas duplicadas, perdidas ni posiciones incoherentes.
- [ ] El control cubre reordenamiento de columnas y de tarjetas; más adelante también protege el límite WIP.

## P1 — Colaboración y flujo completo

### HU-08 — Compartir el tablero con roles

**Como** propietario o administrador autorizado, **quiero** incorporar colaboradores y gestionar sus permisos, **para** trabajar en un tablero compartido.

- [ ] Puedo agregar un usuario existente una sola vez, listar colaboradores y retirar accesos según la matriz de permisos.
- [ ] El propietario administra todo; un administrador gestiona columnas, etiquetas y colaboradores de rol miembro, además de tarjetas. No elimina el proyecto, transfiere propiedad ni promueve administradores.
- [ ] Un miembro consulta y gestiona tarjetas/comentarios, sin administrar proyecto, columnas, etiquetas ni membresías. Solo el propietario promueve/degrada administradores.
- [ ] Un usuario retirado deja de aparecer como autorizado en listados y accesos por ID; no se borra su autoría histórica.

### HU-09 — Transferir la propiedad y salir

**Como** propietario, **quiero** transferir mi proyecto a otro usuario elegible, **para** poder dejarlo sin interrumpir al equipo.

- [ ] La transferencia exige un destinatario activo que ya sea colaborador y permiso del propietario actual.
- [ ] La operación actualiza propiedad y membresías en una transacción; el propietario anterior queda como administrador hasta que decida salir.
- [ ] Un colaborador puede salir; el propietario debe transferir o eliminar el proyecto antes de salir/desactivarse.
- [ ] Ante fallo o peticiones simultáneas, el proyecto conserva exactamente un propietario válido.

### HU-10 — Invitar a colaborar

**Como** usuario autorizado para administrar miembros, **quiero** invitar a otra persona, **para** que pueda incorporarse al tablero.

- [ ] La invitación registra proyecto, correo destinatario, rol permitido, estado y expiración; evita invitaciones pendientes duplicadas y a colaboradores actuales.
- [ ] Solo una cuenta autenticada con el correo destinatario puede aceptar/rechazar; el token debe ser válido y estar vigente.
- [ ] Aceptar crea la membresía una sola vez; rechazar, cancelar o expirar impide reutilizar el token.
- [ ] El remitente autorizado puede consultar y cancelar pendientes; no asigna un rol superior al que está autorizado a conceder.

### HU-11 — Saber quién atiende una tarjeta

**Como** integrante, **quiero** asignar o quitar un responsable, **para** hacer visible quién atiende el trabajo.

- [ ] Una tarjeta admite un responsable opcional, que debe ser propietario o colaborador activo del proyecto.
- [ ] Cambiar responsable no cambia autor y queda registrado en el historial.
- [ ] Retirar/desactivar al responsable desasigna sus tarjetas; no se aceptan responsables de proyectos ajenos.

### HU-12 — Clasificar con etiquetas

**Como** administrador autorizado, **quiero** crear etiquetas del proyecto y aplicarlas a tarjetas, **para** agrupar trabajo relacionado.

- [ ] Puedo crear/editar nombre y color; el mismo nombre normalizado no se duplica dentro del proyecto, pero puede existir en otro.
- [ ] Un integrante puede aplicar/quitar etiquetas existentes en tarjetas del mismo proyecto; una tarjeta admite varias.
- [ ] Eliminar una etiqueta quita sus asociaciones y conserva las tarjetas; no puedo asociar etiquetas ajenas.

### HU-13 — Encontrar trabajo relevante

**Como** integrante, **quiero** buscar y combinar filtros, **para** concentrarme en las tarjetas que necesito atender.

- [ ] Puedo buscar por código/título y combinar columna, prioridad, responsable, etiqueta, bloqueo y vencimiento.
- [ ] Filtrar conserva el orden del tablero, no modifica tarjetas y distingue lista vacía de un error.
- [ ] Quitar filtros recupera la vista completa accesible; la búsqueda de proyectos admite título/etiquetas y paginación.
- [ ] Ningún filtro permite descubrir datos de proyectos sin acceso.

### HU-14 — Limitar el trabajo en curso (WIP)

**Como** administrador autorizado, **quiero** fijar capacidad por columna, **para** evitar empezar más trabajo del que podemos atender.

- [ ] Puedo definir un entero positivo o dejar la columna sin límite; no puedo reducir el límite por debajo de su ocupación activa.
- [ ] Crear, mover o restaurar en una columna llena devuelve un conflicto sin cambios parciales; mover dentro de esa misma columna no suma ocupación.
- [ ] Tarjetas bloqueadas cuentan para WIP; archivadas no cuentan. La respuesta muestra ocupación y límite.
- [ ] Dos peticiones concurrentes por el último cupo no pueden superar la capacidad.

### HU-15 — Hacer visibles los bloqueos

**Como** integrante, **quiero** marcar una tarjeta bloqueada con un motivo, **para** mostrar impedimentos sin cambiar su estado de trabajo.

- [ ] Bloquear requiere motivo no vacío y registra actor/fecha; la columna permanece igual.
- [ ] Desbloquear retira el bloqueo actual y conserva el evento histórico.
- [ ] El bloqueo aparece en el tablero y filtros, y sigue consumiendo WIP.

### HU-16 — Gestionar vencimientos

**Como** integrante, **quiero** establecer una fecha límite, **para** identificar trabajo atrasado.

- [ ] Puedo agregar, cambiar y quitar vencimiento sin perder otros datos de la tarjeta.
- [ ] Se guardan instantes UTC y el cliente presenta la zona correspondiente; entradas inválidas se rechazan.
- [ ] Una tarjeta activa fuera de DONE, no archivada y con fecha pasada aparece vencida; al finalizarla deja de figurar como pendiente vencida.

### HU-17 — Desglosar una tarjeta en pasos

**Como** integrante, **quiero** mantener un checklist, **para** seguir el progreso interno de una tarea.

- [ ] Puedo crear, editar, reordenar, completar y borrar elementos con texto válido.
- [ ] La tarjeta muestra completados/total y conserva el orden tras recargar.
- [ ] Completar el checklist no mueve automáticamente la tarjeta: el avance de columna es una acción explícita.

### HU-18 — Conversar en el contexto de una tarjeta

**Como** integrante, **quiero** comentar una tarjeta, **para** dejar información vinculada al trabajo.

- [ ] Puedo agregar comentarios no vacíos con autor y fechas del servidor; el historial se consulta ordenado y paginado.
- [ ] Solo el autor puede editar/eliminar su comentario mientras conserve acceso al proyecto; los demás pueden leerlo.
- [ ] Los comentarios se asocian a la tarjeta y funcionan sin crear un canal de chat general.

### HU-19 — Consultar lo que cambió

**Como** integrante, **quiero** un historial de actividad, **para** entender cómo evolucionó una tarjeta.

- [ ] Creación, edición, cambios de prioridad/responsable/etiquetas, movimiento, bloqueo y archivo registran actor, fecha y valores relevantes en la misma transacción.
- [ ] El historial se consulta ordenado/paginado y no ofrece edición/borrado directo de eventos.
- [ ] Cada transición registra categorías de flujo de origen/destino; renombrar o reclasificar una columna no reescribe el pasado.
- [ ] Datos previos sin historial se identifican como incompletos; no se inventan eventos ni fechas.

### HU-20 — Archivar y recuperar trabajo

**Como** usuario autorizado, **quiero** archivar proyectos y tarjetas, **para** limpiar la vista conservando la información.

- [ ] El propietario archiva/restaura proyectos; los integrantes autorizados archivan/restauran tarjetas de proyectos activos.
- [ ] Lo archivado no aparece en la vista activa, pero puede consultarse con un filtro explícito. Un proyecto archivado queda en lectura hasta restaurarse.
- [ ] Una tarjeta archivada no consume WIP; restaurarla exige columna existente del mismo proyecto y capacidad disponible.
- [ ] El archivo conserva comentarios, checklist e historial. Restaurar aplica al archivo; una baja lógica no se restaura mediante este flujo de usuario.

### HU-21 — Medir el flujo de trabajo

**Como** propietario o integrante, **quiero** consultar métricas con definiciones claras, **para** detectar acumulación y tiempos de entrega.

- [ ] Las columnas tienen categoría BACKLOG, ACTIVE o DONE. Las cuatro iniciales se clasifican así: Por hacer = BACKLOG, En progreso y En revisión = ACTIVE, Finalizado = DONE.
- [ ] WIP muestra tarjetas no archivadas por columna. Throughput cuenta tarjetas distintas cuya primera entrada a DONE cae dentro del rango consultado.
- [ ] Lead time mide creación → primera entrada a DONE; cycle time mide la primera entrada a ACTIVE previa a esa primera entrada a DONE → primera entrada a DONE. Si no hubo ACTIVE antes de la primera entrega, no hay cycle time válido.
- [ ] Reabrir/mover repetidamente una tarjeta no suma nuevas entregas ni cambia los tiempos de su primera finalización. El archivo no equivale a finalización.
- [ ] Se explicitan rango UTC, población, unidad y cantidad de muestras válidas. Sin eventos suficientes se muestra “sin datos”, no duración cero.

## Entrega transversal

### HU-22 — Usar el Kanban desde un cliente

**Como** usuario, **quiero** operar un tablero visual conectado al backend, **para** usar el sistema completo.

**Prioridad:** P1.

- [ ] En un cliente acordado puedo crear proyecto, gestionar columnas/tarjetas, arrastrar, filtrar y usar las funciones P1 con mis permisos.
- [ ] Recargar y volver a entrar conserva el orden y los datos del servidor.
- [ ] El cliente muestra carga, tablero vacío, validaciones y conflictos con opción de recargar/reintentar.
- [ ] Se registra qué cliente/repositorio se integró y quién valida el flujo; este repositorio no se presenta como si ya incluyera frontend.

### HU-23 — Integrar y ejecutar una API predecible

**Como** desarrollador de un cliente, **quiero** contratos y errores documentados, **para** consumir el Kanban y reproducir su entorno.

**Prioridad:** P0 para la base, P1 para la cobertura completa.

- [ ] OpenAPI y ejemplos coinciden con los endpoints implementados, DTOs, autenticación, filtros, paginación y errores 400/401/403/404/409.
- [ ] Puedo preparar el entorno, ejecutar la aplicación y conservar los datos entre versiones siguiendo la documentación que el equipo elabore con su implementación.
- [ ] Las pruebas verifican persistencia, permisos, integridad entre proyectos, rollback, orden, concurrencia y límites WIP.
- [ ] La entrega distingue funciones implementadas, pruebas ejecutadas y limitaciones pendientes; un JSON fijo no se presenta como listado funcional.
