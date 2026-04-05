package com.mhsa.backend.ai.util;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class AesEncryptor implements AttributeConverter<String, String> {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int IV_LENGTH_BYTES = 12;

    // Replace this fallback with a secure 32-byte secret from env/config in production.
    private static final String FALLBACK_SECRET_KEY = "change-this-32-byte-secret-key!!";

    private static final SecretKey SECRET_KEY = new SecretKeySpec(resolveKeyBytes(), "AES");

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }

        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, SECRET_KEY, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            byte[] encrypted = cipher.doFinal(attribute.getBytes(StandardCharsets.UTF_8));

            byte[] payload = ByteBuffer.allocate(iv.length + encrypted.length)
                    .put(iv)
                    .put(encrypted)
                    .array();

            return Base64.getEncoder().encodeToString(payload);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Failed to encrypt chat message content", ex);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }

        try {
            byte[] payload = Base64.getDecoder().decode(dbData);
            if (payload.length <= IV_LENGTH_BYTES) {
                throw new IllegalStateException("Encrypted payload is invalid");
            }

            ByteBuffer byteBuffer = ByteBuffer.wrap(payload);
            byte[] iv = new byte[IV_LENGTH_BYTES];
            byteBuffer.get(iv);

            byte[] cipherText = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherText);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, SECRET_KEY, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            byte[] decrypted = cipher.doFinal(cipherText);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException | GeneralSecurityException ex) {
            throw new IllegalStateException(
                    "Failed to decrypt chat message content. Existing plaintext rows must be migrated before enabling converter.",
                    ex
            );
        }
    }

    private static byte[] resolveKeyBytes() {
        String configuredKey = System.getenv("MHSA_CHAT_AES_KEY");
        if (configuredKey == null || configuredKey.isBlank()) {
            configuredKey = System.getProperty("mhsa.chat.aes.key");
        }
        if (configuredKey == null || configuredKey.isBlank()) {
            configuredKey = loadKeyFromDotEnv();
        }
        if (configuredKey == null || configuredKey.isBlank()) {
            configuredKey = FALLBACK_SECRET_KEY;
        }

        byte[] keyBytes = configuredKey.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length != 32) {
            throw new IllegalStateException("AES key must be exactly 32 bytes for AES-256");
        }

        return keyBytes;
    }

    private static String loadKeyFromDotEnv() {
        Path envPath = Path.of(".env");
        if (!Files.exists(envPath)) {
            return null;
        }

        try {
            List<String> lines = Files.readAllLines(envPath, StandardCharsets.UTF_8);
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#") || !trimmed.contains("=")) {
                    continue;
                }

                String[] parts = trimmed.split("=", 2);
                if (parts.length == 2 && "MHSA_CHAT_AES_KEY".equals(parts[0].trim())) {
                    return parts[1].trim();
                }
            }
            return null;
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read MHSA_CHAT_AES_KEY from .env", ex);
        }
    }
}
