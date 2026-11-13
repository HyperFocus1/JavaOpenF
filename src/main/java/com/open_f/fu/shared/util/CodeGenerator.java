package com.open_f.fu.shared.util;

import java.security.SecureRandom;

public final class CodeGenerator {

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private CodeGenerator() {
    }

    public static String generate() {
        char[] code = new char[6];
        for (int i = 0; i < code.length; i++) {
            code[i] = CHARS.charAt(RANDOM.nextInt(CHARS.length()));
        }
        return new String(code);
    }
}
