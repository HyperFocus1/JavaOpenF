package com.open_f.fu.shared.persistence;

import java.util.Optional;

import jakarta.persistence.EntityManager;

/**
 * Operaciones comunes de los repositorios JPA. No guarda el EntityManager:
 * cada repositorio tiene el suyo ({@code this.em}) y lo pasa.
 */
public abstract class JpaRepository<E> {

    private final Class<E> entityClass;

    protected JpaRepository(Class<E> entityClass) {
        this.entityClass = entityClass;
    }

    protected Optional<E> findEntity(EntityManager em, Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(em.find(entityClass, id));
    }

    protected E persist(EntityManager em, E entity) {
        em.persist(entity);
        em.flush(); // genera el taskId
        return entity;
    }
}
