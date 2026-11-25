package com.open_f.fu.workspace.service;

import com.open_f.fu.shared.util.CodeGenerator;
import com.open_f.fu.workspace.domain.exception.ColumnNotFoundException;
import com.open_f.fu.workspace.domain.exception.TaskNotFoundException;
import com.open_f.fu.workspace.domain.interfaces.ProjectColumnRepository;
import com.open_f.fu.workspace.domain.interfaces.TaskRepository;
import com.open_f.fu.workspace.domain.model.Task;
import com.open_f.fu.workspace.domain.model.TaskPositioning;
import com.open_f.fu.workspace.domain.model.TaskPriority;
import com.open_f.fu.workspace.service.command.TaskCreateCommand;
import com.open_f.fu.workspace.service.command.TaskUpdateCommand;
import com.open_f.fu.workspace.service.command.TaskUpdatePositionCommand;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@Dependent
@Transactional
public class TaskService {

    private final TaskRepository tasks;
    private final ProjectColumnRepository columns;
    private final ProjectAccessService access;
    private final TaskEvents events;

    @Inject
    public TaskService(TaskRepository tasks, ProjectColumnRepository columns,
                       ProjectAccessService access, TaskEvents events) {
        this.tasks = tasks;
        this.columns = columns;
        this.access = access;
        this.events = events;
    }

    public List<Task> getAll(Long projectId, Long userId) {
        access.requireAccess(userId, projectId);
        return tasks.findTasksByProjectId(projectId);
    }

    public Task create(TaskCreateCommand command) {
        access.requireAccess(command.userId(), command.projectId());
        var column = columns.findByIdAndProjectId(command.columnId(), command.projectId())
                .orElseThrow(() -> new ColumnNotFoundException(command.columnId(), command.projectId()));

        double position = TaskPositioning.positionAt(
                tasks.findPositionsByColumnId(column.getId()), command.positionOrder());

        Task task = Task.create(CodeGenerator.generate(), command.title(), command.description(),
                TaskPriority.from(command.priority()), position,
                command.projectId(), column.getId(), command.userId());
        Task saved = tasks.save(task);

        // CDI entrega el evento al publisher solamente después de un commit correcto.
        events.created(saved);
        return saved;
    }

    public Task update(TaskUpdateCommand command, Long projectId, Long userId) {
        Task task = findTask(command.id(), projectId, userId);
        task.edit(command.title(), command.description());
        Task saved = tasks.save(task);
        events.updated(saved);
        return saved;
    }

    public Task updatePosition(TaskUpdatePositionCommand command, Long projectId, Long userId) {
        Task task = findTask(command.id(), projectId, userId);
        task.edit(command.title(), command.description());
        task.moveTo(TaskPositioning.positionAt(
                tasks.findPositionsByColumnId(task.getColumnId()), command.positionOrder()));
        Task saved = tasks.save(task);
        events.moved(saved);
        return saved;
    }

    private Task findTask(Long taskId, Long projectId, Long userId) {
        access.requireAccess(userId, projectId);
        return tasks.findById(taskId)
                .filter(task -> task.getProjectId().equals(projectId))
                .orElseThrow(() -> new TaskNotFoundException(taskId));
    }
}
