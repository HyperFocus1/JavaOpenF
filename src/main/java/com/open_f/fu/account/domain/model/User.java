package com.open_f.fu.account.domain.model;

import java.util.Locale;

import com.open_f.fu.shared.domain.DomainException;

/**
 * Usuario del sistema. Solo reglas, sin JPA.
 */
public class User {

    public static final int MAX_EMAIL = 255;

    private final Long id;
    private final String email;
    private final String passwordHash;
    private String name;
    private String lastName;

    private User(Long id, String email, String passwordHash, String name, String lastName) {
        this.id = id;
        this.email = normalizeEmail(email);
        this.passwordHash = requireText(passwordHash, "La contraseña es obligatoria");
        rename(name, lastName);
    }

    // Usuario nuevo: la contraseña ya llega cifrada
    public static User create(String email, String passwordHash, String name, String lastName) {
        return new User(null, email, passwordHash, name, lastName);
    }

    // Usuario que ya existe en la BD
    public static User restore(Long id, String email, String passwordHash, String name, String lastName) {
        if (id == null) {
            throw new IllegalArgumentException("El taskId es obligatorio al restaurar");
        }
        return new User(id, email, passwordHash, name, lastName);
    }

    public static String normalizeEmail(String email) {
        String clean = requireText(email, "El correo es obligatorio").toLowerCase(Locale.ROOT);
        if (!clean.contains("@") || clean.length() > MAX_EMAIL) {
            throw new DomainException("El correo no es válido");
        }
        return clean;
    }

    public static void validateRawPassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new DomainException("La contraseña es obligatoria");
        }
    }

    public void rename(String newName, String newLastName) {
        this.name = requireText(newName, "El nombre es obligatorio");
        this.lastName = requireText(newLastName, "El apellido es obligatorio");
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new DomainException(message);
        }
        return value.trim();
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getName() { return name; }
    public String getLastName() { return lastName; }
}
