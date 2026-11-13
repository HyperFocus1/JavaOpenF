package com.open_f.fu.account.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.open_f.fu.account.service.command.RegisterCommand;

public record RegisterRequest(
        String email,
        String name,
        @JsonProperty("last_name") String lastName,
        String password) {

    public RegisterCommand toCommand() {
        return new RegisterCommand(email, password, name, lastName);
    }
}
