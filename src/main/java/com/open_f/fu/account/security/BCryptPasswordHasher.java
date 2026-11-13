package com.open_f.fu.account.security;

import org.mindrot.jbcrypt.BCrypt;

import com.open_f.fu.account.domain.interfaces.PasswordHasher;

public class BCryptPasswordHasher implements PasswordHasher {

    @Override
    public String hash(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }

    @Override
    public boolean matches(String rawPassword, String passwordHash) {
        if (rawPassword == null || passwordHash == null || !passwordHash.startsWith("$2")) {
            return false; // contraseñas antiguas guardadas sin cifrar
        }
        return BCrypt.checkpw(rawPassword, passwordHash);
    }
}
