package com.open_f.fu.account.persistence.repository;

import java.util.Optional;

import com.open_f.fu.account.domain.exception.UserNotFoundException;
import com.open_f.fu.account.domain.interfaces.UserRepository;
import com.open_f.fu.account.domain.model.User;
import com.open_f.fu.account.persistence.entity.UserEntity;
import com.open_f.fu.account.persistence.mapper.UserMapper;
import com.open_f.fu.shared.persistence.JpaRepository;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

public class JpaUserRepository extends JpaRepository<UserEntity> implements UserRepository {

    private final EntityManager em;

    @Inject
    public JpaUserRepository(EntityManager em) {
        super(UserEntity.class);
        this.em = em;
    }

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            return UserMapper.toDomain(persist(this.em, UserMapper.toNewEntity(user)));
        }
        UserEntity e = findEntity(this.em, user.getId()).orElseThrow(() -> new UserNotFoundException(user.getId()));
        UserMapper.copy(user, e);
        return UserMapper.toDomain(e);
    }

    @Override
    public Optional<User> findById(Long id) {
        return findEntity(this.em, id).map(UserMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return this.em.createQuery("SELECT u FROM UserEntity u WHERE u.email = :email", UserEntity.class)
                .setParameter("email", email)
                .getResultStream()
                .findFirst()
                .map(UserMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return this.em.createQuery("SELECT COUNT(u) FROM UserEntity u WHERE u.email = :email", Long.class)
                .setParameter("email", email)
                .getSingleResult() > 0;
    }

    @Override
    public boolean existsById(Long id) {
        return findEntity(this.em, id).isPresent();
    }
}
