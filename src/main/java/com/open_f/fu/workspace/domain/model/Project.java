package com.open_f.fu.workspace.domain.model;

import com.open_f.fu.shared.domain.DomainException;

/**
 * Proyecto (= un tablero Kanban). Solo reglas, sin JPA.
 */
public class Project {

    public static final int MAX_TITLE = 15; // largo de la columna projects.title
    public static final int MIN_DESCRIPTION = 8;
    public static final int MAX_DESCRIPTION = 400;

    private final Long id;
    private final Long userId;
    private String title;
    private String description;

    private Project(Long id, Long userId, String title, String description) {
        if (userId == null) {
            throw new DomainException("El proyecto necesita un propietario");
        }
        this.id = id;
        this.userId = userId;
        rename(title);
        changeDescription(description);
  }

    // Proyecto nuevo
    public static Project create(Long userId, String title, String description) {
        return new Project(null, userId, title, description);
    }

    // Proyecto que ya existe en la BD
    public static Project restore(Long id, Long userId, String title, String description) {
        if (id == null) {
            throw new IllegalArgumentException("El taskId es obligatorio al restaurar");
        }
        return new Project(id, userId, title, description);
    }

    public void rename(String newTitle) {
        if (newTitle == null || newTitle.isBlank()) {
            throw new DomainException("El título no puede estar vacío");
        }
        String clean = newTitle.trim();
        if (clean.length() > MAX_TITLE) {
            throw new DomainException("El título no puede pasar de " + MAX_TITLE + " caracteres");
        }
        this.title = clean;
    }

    public void changeDescription(String newDescription) {
        if (newDescription == null) {
            throw new DomainException("La descripción es obligatoria");
        }
        String clean = newDescription.trim();
        if (clean.length() < MIN_DESCRIPTION || clean.length() > MAX_DESCRIPTION) {
            throw new DomainException(
                    "La descripción debe tener entre " + MIN_DESCRIPTION + " y " + MAX_DESCRIPTION + " caracteres");
        }
        this.description = clean;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
}
