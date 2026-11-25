package com.open_f.fu.workspace.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.open_f.fu.workspace.domain.interfaces.ProjectColumnRepository;
import com.open_f.fu.workspace.domain.interfaces.TaskRepository;
import com.open_f.fu.workspace.domain.model.Task;
import com.open_f.fu.workspace.domain.model.TaskPriority;
import com.open_f.fu.workspace.service.command.TaskUpdateCommand;

/**
 * Ejemplo unitario: prueba el servicio sin MySQL, Kafka ni WildFly.
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    // Los mocks sustituyen las dependencias que recibe el constructor del servicio.
    @Mock private TaskRepository tasks;
    @Mock private ProjectColumnRepository columns;
    @Mock private ProjectAccessService access;
    @Mock private TaskEvents events;

    @Test
    @DisplayName("Editar una tarea guarda los cambios y delega el evento en TaskEvents")
    void actualizaTituloYDescripcionDeUnaTareaExistente() {
        // 1. Preparar: el dominio es real; el repositorio devuelve una tarea conocida.
        Task existing = Task.restore(7L, "TASK-007", "Título anterior", "Descripción anterior",
                TaskPriority.MEDIUM, 100.0, 1L, 2L, 3L);
        var command = new TaskUpdateCommand(7L, "Corregir login", "Validar las credenciales");

        when(tasks.findById(7L)).thenReturn(Optional.of(existing));
        when(tasks.save(existing)).thenReturn(existing);

        // 2. Actuar: llamamos al método que queremos aprender a probar.
        // El test unitario ejecuta Java directamente; no activa el interceptor JTA.
        // Un solo mock representa la emisión de todos los eventos de tarea.
        var service = new TaskService(tasks, columns, access, events);
        Task updated = service.update(command, 1L, 3L);

        // 3. Comprobar: JUnit valida el resultado y Mockito verifica el guardado.
        assertEquals("Corregir login", updated.getTitle());
        assertEquals("Validar las credenciales", updated.getDescription());
        assertEquals(TaskPriority.MEDIUM, updated.getPriority());
        assertEquals(3L, updated.getUserId());
        verify(access).requireAccess(3L, 1L);
        verify(tasks).save(existing);

        verify(events).updated(updated);
        verifyNoMoreInteractions(events);
    }
}
