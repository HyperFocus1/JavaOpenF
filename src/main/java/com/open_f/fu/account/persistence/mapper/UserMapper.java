package com.open_f.fu.account.persistence.mapper;

import com.open_f.fu.account.domain.model.User;
import com.open_f.fu.account.persistence.entity.UserEntity;

public final class UserMapper {

    private UserMapper() {
    }

    public static User toDomain(UserEntity e) {
        return User.restore(e.getId(), e.getEmail(), e.getPassword(), e.getName(), e.getLastName());
    }

    public static UserEntity toNewEntity(User u) {
        return new UserEntity(u.getEmail(), u.getPasswordHash(), u.getName(), u.getLastName());
    }

    public static void copy(User from, UserEntity to) {
        to.setName(from.getName());
        to.setLastName(from.getLastName());
    }
}
