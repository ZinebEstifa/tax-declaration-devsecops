package com.example.Declarations.fiscaux.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtils {

    @Value("${app.security.jwt.secret:dgi_maroc_super_secret_jwt_key_2026_dgi_maroc_super_secret_jwt_key_2026}")
    private String jwtSecret;

    @Value("${app.security.jwt.expirationMs:86400000}") // 24 hours
    private long jwtExpirationMs;

    private static final String ISSUER = "dgi-simpl";

    public String generateJwtToken(String numeroFiscal) {
        Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject(numeroFiscal)
                .withClaim("role", "CONTRIBUABLE")
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .sign(algorithm);
    }

    public String getNumeroFiscalFromJwtToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer(ISSUER)
                    .build();
            DecodedJWT jwt = verifier.verify(token);
            return jwt.getSubject();
        } catch (JWTVerificationException e) {
            return null;
        }
    }

    public boolean validateJwtToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer(ISSUER)
                    .build();
            verifier.verify(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }
}
