package com.open_f.fu.shared.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class PersistenceProducer {

    @Produces
    @PersistenceContext(unitName = "open-fu")
    EntityManager entityManager;
}
