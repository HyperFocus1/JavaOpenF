package com.open_f.fu.chat.domain.model;

import com.open_f.fu.shared.domain.DomainException;

/**
 * Canal de chat. Solo reglas, sin JPA.
 */
public class Channel {

    public static final int MAX_TITLE = 255;

    private final Long id;
    private String title;

    private Channel(Long id, String title) {
        this.id = id;
        rename(title);
    }

    // Canal nuevo (sin taskId todavía)
    public static Channel create(String title) {
        return new Channel(null, title);
    }

    // Canal que ya existe en la BD
    public static Channel restore(Long id, String title) {
        if (id == null) {
            throw new IllegalArgumentException("El taskId es obligatorio al restaurar");
        }
        return new Channel(id, title);
    }

    public void rename(String newTitle) {
        if (newTitle == null || newTitle.isBlank()) {
            throw new DomainException("El título del canal es obligatorio");
        }
        String clean = newTitle.trim();
        if (clean.length() > MAX_TITLE) {
            throw new DomainException("El título no puede pasar de " + MAX_TITLE + " caracteres");
        }
        this.title = clean;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
}
