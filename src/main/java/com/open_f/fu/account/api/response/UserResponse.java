package com.open_f.fu.account.api.response;

import com.open_f.fu.account.domain.model.User;

public record UserResponse(Long id, String email, String name, String lastName) {

    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getEmail(), u.getName(), u.getLastName());
    }
}
