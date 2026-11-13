package com.open_f.fu.workspace.domain.model;

import java.util.Locale;

import com.open_f.fu.shared.domain.DomainException;

public enum TaskPriority {
    HIGH,
    MEDIUM,
    LOW;

    // Acepta "high", " Low ", etc.
    public static TaskPriority from(String value) {
        if (value == null || value.isBlank()) {
            throw new DomainException("La prioridad es obligatoria");
        }
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new DomainException("Prioridad no válida: " + value + ". Usa HIGH, MEDIUM o LOW");
        }
    }
}
