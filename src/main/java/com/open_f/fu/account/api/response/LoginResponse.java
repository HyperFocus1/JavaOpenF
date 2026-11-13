package com.open_f.fu.account.api.response;

public record LoginResponse(String token, String type) {

    private static final String BEARER = "Bearer";

    public static LoginResponse bearer(String token) {
        return new LoginResponse(token, BEARER);
    }
}
