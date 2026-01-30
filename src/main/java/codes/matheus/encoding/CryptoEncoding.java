package codes.matheus.encoding;

import codes.matheus.exceptions.EncodingException;
import org.jetbrains.annotations.NotNull;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

public final class CryptoEncoding implements Encoding<File> {
    private static final @NotNull String algorithm = "AES/CBC/PKCS5Padding";
    private final @NotNull String password;

    public CryptoEncoding(@NotNull String password) {
        this.password = password;
    }

    @Override
    public @NotNull File encode(@NotNull File file) {
        @NotNull File encryptedFile = new File(file.getAbsolutePath() + ".enc");
        try {
            byte[] keyByte = generateKey(password);
            @NotNull SecretKeySpec key = new SecretKeySpec(keyByte, "AES");

            byte[] iv = new byte[16];
            new SecureRandom().nextBytes(iv);
            @NotNull IvParameterSpec ivSpec = new IvParameterSpec(iv);
            @NotNull Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);

            try (@NotNull FileInputStream input = new FileInputStream(file)) {
                @NotNull FileOutputStream output = new FileOutputStream(encryptedFile);
                output.write(iv);
                processStream(cipher, input, output);
                output.close();
            }
            return encryptedFile;
        } catch (Exception e) {
            throw new EncodingException("Encryption failed: " + e.getMessage());
        }
    }

    public @NotNull File decode(@NotNull File file) {
        @NotNull File decryptedFile = new File(file.getAbsolutePath().replace(".enc", ".dec"));
        try {
            byte[] keyBytes = generateKey(password);
            @NotNull  SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

            try (@NotNull FileInputStream inputStream = new FileInputStream(file)) {
                @NotNull FileOutputStream outputStream = new FileOutputStream(decryptedFile);
                byte[] iv = new byte[16];
                inputStream.read(iv);
                @NotNull IvParameterSpec ivSpec = new IvParameterSpec(iv);
                @NotNull Cipher cipher = Cipher.getInstance(algorithm);

                cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
                processStream(cipher, inputStream, outputStream);
                outputStream.close();
                return decryptedFile;
            }
        } catch (Exception e) {
            throw new EncodingException("Decryption failed: " + e.getMessage());
        }
    }

    private void processStream(@NotNull Cipher cipher, @NotNull InputStream is, @NotNull OutputStream os) throws Exception {
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            byte[] output = cipher.update(buffer, 0, bytesRead);
            if (output != null) os.write(output);
        }
        byte[] outputBytes = cipher.doFinal();
        if (outputBytes != null) os.write(outputBytes);
    }

    private byte[] generateKey(@NotNull String password) throws Exception {
        @NotNull MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(password.getBytes(StandardCharsets.UTF_8));
    }
}
