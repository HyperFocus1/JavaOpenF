package com.open_f.fu.workspace.domain.model;

import com.open_f.fu.shared.domain.DomainException;

/**
 * Tarjeta del tablero. Solo reglas, sin JPA.
 */
public class Task {

    public static final double DEFAULT_POSITION = 100.0;

    private final Long id;
    private final String code;
    private final Long projectId;
    private final Long authorId;
    private final Long columnId;
    private String title;
    private String description;
    private TaskPriority priority;
    private double positionY;

    private Task(Long id, String code, String title, String description, TaskPriority priority,
                 double positionY, Long projectId, Long columnId, Long authorId) {
        if (code == null || code.isBlank()) throw new DomainException("El código es obligatorio");
        if (projectId == null) throw new DomainException("La tarea necesita un proyecto");
        if (columnId == null) throw new DomainException("La tarea necesita una columna");
        if (authorId == null) throw new DomainException("La tarea necesita un autor");
        this.id = id;
        this.code = code;
        this.projectId = projectId;
        this.columnId = columnId;
        this.authorId = authorId;
        edit(title, description);
        changePriority(priority);
        this.positionY = positionY;
    }

    public static Task create(String code, String title, String description, TaskPriority priority,
                              double positionY, Long projectId, Long columnId, Long authorId) {
        return new Task(null, code, title, description, priority, positionY, projectId, columnId, authorId);
    }

    public static Task restore(Long id, String code, String title, String description, TaskPriority priority,
                               double positionY, Long projectId, Long columnId, Long authorId) {
        if (id == null) {
            throw new IllegalArgumentException("El taskId es obligatorio al restaurar");
        }
        return new Task(id, code, title, description, priority, positionY, projectId, columnId, authorId);
    }

    // Tarjeta de bienvenida de todo proyecto nuevo
    public static Task welcome(String code, Long projectId, Long firstColumnId, Long authorId) {
        return create(code,
                "Get started with your first task",
                "Welcome to your project! Edit this task, assign a priority, or drag it across the board to track its progress.",
                TaskPriority.LOW, DEFAULT_POSITION, projectId, firstColumnId, authorId);
    }

    public void edit(String newTitle, String newDescription) {
        if (newTitle == null || newTitle.isBlank()) {
            throw new DomainException("El título de la tarea es obligatorio");
        }
        this.title = newTitle.trim();
        this.description = newDescription == null ? null : newDescription.trim();
    }

    public void changePriority(TaskPriority newPriority) {
        if (newPriority == null) {
            throw new DomainException("La prioridad es obligatoria");
        }
        this.priority = newPriority;
    }

    public void moveTo(double newPositionY) {
        this.positionY = newPositionY;
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public TaskPriority getPriority() { return priority; }
    public double getPositionY() { return positionY; }
    public Long getProjectId() { return projectId; }
    public Long getColumnId() { return columnId; }
    public Long getAuthorId() { return authorId; }
}
