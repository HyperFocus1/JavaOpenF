package com.open_f.fu.account.service.command;

public record RegisterCommand(String email, String password, String name, String lastName) {
}
