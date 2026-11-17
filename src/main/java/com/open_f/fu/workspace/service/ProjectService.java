package com.open_f.fu.workspace.service;

import java.util.List;

import com.open_f.fu.account.domain.exception.UserNotFoundException;
import com.open_f.fu.account.service.UserService;
import com.open_f.fu.shared.util.CodeGenerator;
import com.open_f.fu.workspace.domain.exception.ProjectNotFoundException;
import com.open_f.fu.workspace.domain.interfaces.ProjectColumnRepository;
import com.open_f.fu.workspace.domain.interfaces.ProjectRepository;
import com.open_f.fu.workspace.domain.interfaces.TaskRepository;
import com.open_f.fu.workspace.domain.model.Project;
import com.open_f.fu.workspace.domain.model.ProjectColumn;
import com.open_f.fu.workspace.domain.model.Task;

import jakarta.inject.Inject;
import jakarta.enterprise.context.Dependent;
import jakarta.transaction.Transactional;

@Dependent
@Transactional
public class ProjectService {

    private final ProjectRepository projects;
    private final ProjectColumnRepository columns;
    private final TaskRepository tasks;
    private final UserService userService; // otro módulo: solo por su servicio

    @Inject
    public ProjectService(ProjectRepository projects, ProjectColumnRepository columns, TaskRepository tasks,
                          UserService userService) {
        this.projects = projects;
        this.columns = columns;
        this.tasks = tasks;
        this.userService = userService;
    }

    // Crea el proyecto con sus 4 columnas y la tarjeta de bienvenida, todo en una transacción
    public Project create(Long userId, String title, String description) {
        Project draft = Project.create(userId, title, description);

        if (!userService.exists(userId)) {
            throw new UserNotFoundException(userId);
        }
        Project project = projects.save(draft);

        List<ProjectColumn> saved = ProjectColumn.defaultsFor(project.getId()).stream()
                .map(columns::save)
                .toList();

        tasks.save(Task.welcome(CodeGenerator.generate(), project.getId(), saved.getFirst().getId(), userId));

        return project;
    }

    // title o description en null no se cambian
    public Project update(Long id, String title, String description) {
        Project project = projects.findById(id).orElseThrow(() -> new ProjectNotFoundException(id));
        if (title != null) {
            project.rename(title);
        }
        if (description != null) {
            project.changeDescription(description);
        }
        return projects.save(project);
    }
}
