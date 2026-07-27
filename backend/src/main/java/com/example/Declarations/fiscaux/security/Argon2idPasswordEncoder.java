package com.example.Declarations.fiscaux.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class Argon2idPasswordEncoder implements PasswordEncoder {

    @Value("${app.security.pepper:dgi_maroc_default_pepper_secret_2026}")
    private String pepper;

    @Override
    public String encode(CharSequence rawPassword) {
        String salt = Argon2idHasher.generateSalt();
        String hash = Argon2idHasher.hash(rawPassword.toString(), salt, pepper);
        return salt + ":" + hash;
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (encodedPassword == null || !encodedPassword.contains(":")) {
            return false;
        }
        String[] parts = encodedPassword.split(":");
        if (parts.length != 2) {
            return false;
        }
        String salt = parts[0];
        String expectedHash = parts[1];
        String actualHash = Argon2idHasher.hash(rawPassword.toString(), salt, pepper);
        return expectedHash.equals(actualHash);
    }
}
