package codes.matheus.encoding;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public final class EncodingTest {
    @TempDir
    private Path tempDir;
    private File testFile;
    private final @NotNull String content = "Test content for encryption and zip";

    @BeforeEach
    void setUp() throws IOException {
        @NotNull Path filePath = tempDir.resolve("teste.txt");
        Files.writeString(filePath, content);
        testFile = filePath.toFile();
    }

    @Test
    void testHashEncoding() {
        @NotNull HashEncoding hasher = new HashEncoding();
        @NotNull String hash1 = hasher.encode(testFile);
        @NotNull String hash2 = hasher.encode(testFile);

        assertNotNull(hash1);
        assertEquals(64, hash1.length());
        assertEquals(hash1, hash2);
    }

    @Test
    void testZipAndUnzip() throws IOException {
        @NotNull ZipEncoding zipper = new ZipEncoding();

        @NotNull File zipFile = zipper.encode(testFile);
        assertTrue(zipFile.exists());

        @NotNull File unzippedFolder = zipper.decode(zipFile);
        assertTrue(unzippedFolder.isDirectory());

        @NotNull File recoveredFile = new File(unzippedFolder, testFile.getName());

        assertTrue(recoveredFile.exists());
        assertEquals(content, Files.readString(recoveredFile.toPath()));
    }

    @Test
    void testCryptoAES() throws IOException {
        @NotNull String password = "secret-password-123";
        @NotNull CryptoEncoding crypto = new CryptoEncoding(password);

        @NotNull File encrypted = crypto.encode(testFile);
        assertTrue(encrypted.exists());

        byte[] originalBytes = Files.readAllBytes(testFile.toPath());
        byte[] encryptedBytes = Files.readAllBytes(encrypted.toPath());
        assertFalse(Arrays.equals(originalBytes, encryptedBytes));

        @NotNull File decrypted = crypto.decode(encrypted);
        assertTrue(decrypted.exists());
        assertEquals(content, Files.readString(decrypted.toPath()));
    }

    @Test
    void testCryptoWrongPassword() {
        @NotNull CryptoEncoding encoder = new CryptoEncoding("correct-password");
        @NotNull File encrypted = encoder.encode(testFile);

        @NotNull CryptoEncoding decoderWithWrongPass = new CryptoEncoding("incorrect-password");

        assertThrows(RuntimeException.class, () -> {
            decoderWithWrongPass.decode(encrypted);
        });
    }
}