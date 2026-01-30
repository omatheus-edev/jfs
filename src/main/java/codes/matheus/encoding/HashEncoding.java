package codes.matheus.encoding;

import codes.matheus.exceptions.EncodingException;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class HashEncoding implements Encoding<String> {
    @Override
    public @NotNull String encode(@NotNull File file) {
        if (file.isDirectory()) {
            throw new EncodingException("Cannot generate hash for a directory.");
        }
        try (FileInputStream input = new FileInputStream(file)) {
            @NotNull MessageDigest instance = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = input.read(buffer)) != -1) {
                instance.update(buffer, 0, bytesRead);
            }

            byte[] hashBytes = instance.digest();
            return bytesToHex(hashBytes);
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hashing: " + e.getMessage());
        }
    }

    private @NotNull String bytesToHex(byte[] bytes) {
        @NotNull StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            @NotNull String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                result.append('0');
            }
            result.append(hex);
        }
        return result.toString();
    }
}
