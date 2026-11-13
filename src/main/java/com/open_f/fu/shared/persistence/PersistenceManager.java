package com.open_f.fu.shared.persistence;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public final class PersistenceManager {

    private static final String UNIT = "open-fu";
    private static volatile EntityManagerFactory emf;

    private PersistenceManager() {
    }

    public static synchronized void init() {
        if (emf == null) {
            emf = Persistence.createEntityManagerFactory(UNIT);
        }
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        EntityManagerFactory current = emf;
        if (current == null || !current.isOpen()) {
            throw new IllegalStateException("PersistenceManager no inicializado");
        }
        return current;
    }

    public static synchronized void close() {
        if (emf != null) {
            try {
                if (emf.isOpen()) {
                    emf.close();
                }
            } finally {
                emf = null;
            }
        }
    }
}
