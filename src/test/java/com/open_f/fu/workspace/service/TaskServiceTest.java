package com.open_f.fu.workspace.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.function.Supplier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.open_f.fu.account.service.UserService;
import com.open_f.fu.shared.persistence.TransactionManager;
import com.open_f.fu.workspace.domain.interfaces.ProjectColumnRepository;
import com.open_f.fu.workspace.domain.interfaces.ProjectRepository;
import com.open_f.fu.workspace.domain.interfaces.TaskRepository;
import com.open_f.fu.workspace.domain.model.Task;
import com.open_f.fu.workspace.domain.model.TaskPriority;
import com.open_f.fu.workspace.service.command.TaskUpdateCommand;

/**
 * Ejemplo unitario: prueba el servicio sin abrir conexiones a MySQL ni usar Tomcat.
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    // Los mocks sustituyen las dependencias que recibe el constructor del servicio.
    @Mock private TaskRepository tasks;
    @Mock private ProjectColumnRepository columns;
    @Mock private ProjectRepository projects;
    @Mock private UserService users;
    @Mock private TransactionManager transactions;

    // Mockito construye un TaskService real y le inyecta los mocks anteriores.
    @InjectMocks private TaskService service;

    @Test
    @DisplayName("Editar una tarea existente actualiza su título y descripción y la guarda")
    void actualizaTituloYDescripcionDeUnaTareaExistente() {
        // 1. Preparar: el dominio es real; el repositorio devuelve una tarea conocida.
        Task existing = Task.restore(7L, "TASK-007", "Título anterior", "Descripción anterior",
                TaskPriority.MEDIUM, 100.0, 1L, 2L, 3L);
        var command = new TaskUpdateCommand(7L, "Corregir login", "Validar las credenciales");

        when(tasks.findById(7L)).thenReturn(Optional.of(existing));
        when(tasks.save(existing)).thenReturn(existing);

        // Ejecutamos el trabajo recibido por el mock, sin abrir una transacción real.
        when(transactions.inTransaction(any())).thenAnswer(invocation -> {
            Supplier<?> work = invocation.getArgument(0);
            return work.get();
        });

        // 2. Actuar: llamamos al método que queremos aprender a probar.
        Task updated = service.update(command);

        // 3. Comprobar: JUnit valida el resultado y Mockito verifica el guardado.
        assertEquals("Corregir login", updated.getTitle());
        assertEquals("Validar las credenciales", updated.getDescription());
        assertEquals(TaskPriority.MEDIUM, updated.getPriority());
        verify(tasks).save(existing);
    }
}
