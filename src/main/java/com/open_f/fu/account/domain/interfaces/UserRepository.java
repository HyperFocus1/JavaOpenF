package com.open_f.fu.account.domain.interfaces;

import java.util.Optional;

import com.open_f.fu.account.domain.model.User;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsById(Long id);
}
