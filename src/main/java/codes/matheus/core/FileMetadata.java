package codes.matheus.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.io.File;
import java.util.Objects;

public final class FileMetadata {
    private final @NotNull String name;
    private final @NotNull String absolutePath;
    private final boolean isDirectory;
    @Range(from = 0, to = Long.MAX_VALUE)
    private final long size;

    public FileMetadata(@NotNull File file) {
        this.name = file.getName();
        this.absolutePath = file.getAbsolutePath();
        this.isDirectory = file.isDirectory();
        this.size = file.length();
    }

    public @NotNull String getName() {
        return name;
    }

    public @NotNull String getAbsolutePath() {
        return absolutePath;
    }

    public @NotNull String getSize() {
        return formatSize(size);
    }

    private @NotNull String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        @NotNull String pre = "KMGTPE".charAt(exp-1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    @Override
    public String toString() {
        return (isDirectory ? "[DIR] " : "[FILE] ") + name;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        FileMetadata that = (FileMetadata) o;
        return Objects.equals(absolutePath, that.absolutePath);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(absolutePath);
    }
}
