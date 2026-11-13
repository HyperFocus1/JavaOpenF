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
import com.open_f.fu.workspace.domain.model.Project;
import com.open_f.fu.workspace.domain.model.ProjectColumn;
import com.open_f.fu.workspace.domain.model.Task;
import com.open_f.fu.workspace.domain.model.TaskPositioning;
import com.open_f.fu.workspace.domain.model.TaskPriority;
import com.open_f.fu.workspace.service.command.TaskCreateCommand;
import com.open_f.fu.workspace.service.command.TaskUpdateCommand;

import com.open_f.fu.workspace.service.command.TaskUpdatePositionCommand;
import jakarta.inject.Inject;

import java.util.Optional;

public class TaskService {

    private final TaskRepository tasks;
    private final ProjectColumnRepository columns;
    private final ProjectRepository projects;
    private final UserService userService; // otro módulo: solo por su servicio
    private final TransactionManager tx;

    @Inject
    public TaskService(TaskRepository tasks, ProjectColumnRepository columns, ProjectRepository projects, UserService userService, TransactionManager tx) {
        this.tasks = tasks;
        this.columns = columns;
        this.projects = projects;
        this.userService = userService;
        this.tx = tx;
    }

    public Task create(TaskCreateCommand cmd) {
        TaskPriority priority = TaskPriority.from(cmd.priority());

        return tx.inTransaction(() -> {
            ProjectColumn column = columns.findByIdAndProjectId(cmd.columnId(), cmd.projectId()).orElseThrow(() -> new ColumnNotFoundException(cmd.columnId(), cmd.projectId()));
            Project project = projects.findById(cmd.projectId()).orElseThrow(() -> new ProjectNotFoundException(cmd.projectId()));

            // Si el usuario no existe, el autor es el propietario del proyecto
            Long authorId = userService.exists(cmd.userId()) ? cmd.userId() : project.getOwnerId();

            double positionY = TaskPositioning.positionAt(tasks.findPositionsByColumnId(column.getId()), cmd.positionOrder());

            Task task = Task.create(CodeGenerator.generate(), cmd.title(), cmd.description(), priority, positionY, cmd.projectId(), column.getId(), authorId);

            return tasks.save(task);
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
