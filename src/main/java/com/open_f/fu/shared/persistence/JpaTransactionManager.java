package com.open_f.fu.shared.persistence;

import java.util.function.Supplier;

import jakarta.persistence.EntityManager;

/**
 * Abre la transacción y deja el EntityManager del hilo listo para los repositorios.
 * Si ya hay una transacción abierta, el trabajo se une a ella.
 */
public class JpaTransactionManager implements TransactionManager {

    private static final ThreadLocal<EntityManager> CURRENT = new ThreadLocal<>();

    @Override
    public <T> T inTransaction(Supplier<T> work) {
        if (CURRENT.get() != null) {
            return work.get();
        }
        return PersistenceManager.getEntityManagerFactory().callInTransaction(em -> {
            CURRENT.set(em);
            try {
                return work.get();
            } finally {
                CURRENT.remove();
            }
        });
    }

    public static EntityManager currentEntityManager() {
        EntityManager em = CURRENT.get();
        if (em == null) {
            throw new IllegalStateException("Repositorio usado fuera de una transacción");
        }
        return em;
    }
}
