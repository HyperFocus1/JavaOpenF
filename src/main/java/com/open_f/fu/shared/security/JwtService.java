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
        String secret = "SECRET-JWT"; // TODO: leer de variable de entorno
        this.algorithm = Algorithm.HMAC256(secret);
        this.verifier = JWT.require(algorithm).withIssuer(ISSUER).build();
    }

    public String generateToken(Long userId, String username) {
        Instant now = Instant.now();
        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject(userId.toString())
                .withClaim("username", username)
                .withIssuedAt(now)
                .withExpiresAt(now.plusSeconds(TTL_SECONDS))
                .sign(algorithm);
    }

    public DecodedJWT verifyToken(String token) {
        return verifier.verify(token);
    }
}
