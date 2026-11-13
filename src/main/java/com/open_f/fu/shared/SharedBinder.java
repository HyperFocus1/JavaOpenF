package com.open_f.fu.shared;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

import com.open_f.fu.shared.persistence.JpaTransactionManager;
import com.open_f.fu.shared.persistence.TransactionManager;
import com.open_f.fu.shared.persistence.TransactionalEntityManager;
import com.open_f.fu.shared.security.JwtService;

import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;

public class SharedBinder extends AbstractBinder {

    @Override
    protected void configure() {
        bind(JpaTransactionManager.class).to(TransactionManager.class).in(Singleton.class);
        bind(TransactionalEntityManager.create()).to(EntityManager.class);
        bindAsContract(JwtService.class).in(Singleton.class);
    }
}
