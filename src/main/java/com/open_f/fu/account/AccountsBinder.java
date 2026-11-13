package com.open_f.fu.account;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

import com.open_f.fu.account.domain.interfaces.PasswordHasher;
import com.open_f.fu.account.domain.interfaces.UserRepository;
import com.open_f.fu.account.persistence.repository.JpaUserRepository;
import com.open_f.fu.account.security.BCryptPasswordHasher;
import com.open_f.fu.account.service.UserService;

import jakarta.inject.Singleton;

public class AccountsBinder extends AbstractBinder {

    @Override
    protected void configure() {
        bind(JpaUserRepository.class).to(UserRepository.class).in(Singleton.class);
        bind(BCryptPasswordHasher.class).to(PasswordHasher.class).in(Singleton.class);
        bindAsContract(UserService.class).in(Singleton.class);
    }
}
