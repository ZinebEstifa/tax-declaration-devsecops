package com.example.Declarations.fiscaux.security;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class Argon2idHasher {

    private static final int ITERATIONS = 3;
    private static final int MEMORY_KB = 16384; // 16 MB
    private static final int PARALLELISM = 4;
    private static final int HASH_LENGTH = 32;

    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String hash(String password, String saltBase64, String pepper) {
        byte[] salt = Base64.getDecoder().decode(saltBase64);
        byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
        byte[] pepperBytes = pepper != null ? pepper.getBytes(StandardCharsets.UTF_8) : new byte[0];

        Argon2BytesGenerator generator = new Argon2BytesGenerator();
        Argon2Parameters.Builder builder = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withVersion(Argon2Parameters.ARGON2_VERSION_13)
                .withIterations(ITERATIONS)
                .withMemoryAsKB(MEMORY_KB)
                .withParallelism(PARALLELISM)
                .withSalt(salt);

        if (pepperBytes.length > 0) {
            builder.withSecret(pepperBytes);
        }

        generator.init(builder.build());
        byte[] hash = new byte[HASH_LENGTH];
        generator.generateBytes(passwordBytes, hash);

        return Base64.getEncoder().encodeToString(hash);
    }
}
