package com.open_f.fu.shared.security;

import java.time.Instant;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

public class JwtService {
    private static final String ISSUER = "open-f";
    private static final long TTL_SECONDS = 3600;
    private final Algorithm algorithm;
    private final JWTVerifier verifier;

    public JwtService() {
        String secret = System.getenv("JWT_SECRET");
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("Configura JWT_SECRET con al menos 32 caracteres");
        }
        algorithm = Algorithm.HMAC256(secret);
        verifier = JWT.require(algorithm).withIssuer(ISSUER).build();
    }

    public String generateToken(Long userId, String username) {
        Instant now = Instant.now();
        return JWT.create().withIssuer(ISSUER).withSubject(userId.toString())
                .withClaim("username", username).withIssuedAt(now)
                .withExpiresAt(now.plusSeconds(TTL_SECONDS)).sign(algorithm);
    }

    public DecodedJWT verifyToken(String token) { return verifier.verify(token); }
}
