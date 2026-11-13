package com.open_f.fu.workspace.domain.model;

import java.util.List;

import com.open_f.fu.shared.domain.DomainException;

/**
 * Columna del tablero. El estado visible de una tarjeta es su columna.
 */
public class ProjectColumn {

    public static final String DEFAULT_COLOR = "#64748b";
    public static final int MAX_COLOR = 10;

    private final Long id;
    private final Long projectId;
    private String name;
    private String color;
    private int position;

    private ProjectColumn(Long id, Long projectId, String name, String color, int position) {
        if (projectId == null) {
            throw new DomainException("La columna necesita un proyecto");
        }
        this.id = id;
        this.projectId = projectId;
        rename(name);
        changeColor(color);
        moveTo(position);
    }

    public static ProjectColumn create(Long projectId, String name, String color, int position) {
        return new ProjectColumn(null, projectId, name, color, position);
    }

    public static ProjectColumn restore(Long id, Long projectId, String name, String color, int position) {
        if (id == null) {
            throw new IllegalArgumentException("El taskId es obligatorio al restaurar");
        }
        return new ProjectColumn(id, projectId, name, color, position);
    }

    // Columnas con las que nace todo proyecto
    public static List<ProjectColumn> defaultsFor(Long projectId) {
        return List.of(
                create(projectId, "Por hacer", "#64748b", 0),
                create(projectId, "En progreso", "#3b82f6", 1),
                create(projectId, "En revisión", "#f59e0b", 2),
                create(projectId, "Finalizado", "#22c55e", 3));
    }

    public void rename(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new DomainException("El nombre de la columna es obligatorio");
        }
        this.name = newName.trim();
    }

    public void changeColor(String newColor) {
        String clean = (newColor == null || newColor.isBlank()) ? DEFAULT_COLOR : newColor.trim();
        if (clean.length() > MAX_COLOR) {
            throw new DomainException("El color no puede pasar de " + MAX_COLOR + " caracteres");
        }
        this.color = clean;
    }

    public void moveTo(int newPosition) {
        if (newPosition < 0) {
            throw new DomainException("La posición de la columna no puede ser negativa");
        }
        this.position = newPosition;
    }

    public Long getId() { return id; }
    public Long getProjectId() { return projectId; }
    public String getName() { return name; }
    public String getColor() { return color; }
    public int getPosition() { return position; }
}
