package codes.matheus.core;

import codes.matheus.cli.Command;
import codes.matheus.datastructures.tree.NaryTree;
import codes.matheus.util.Colors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public final class FileAnalyzer {
    public static boolean validate(@Nullable NaryTree.Node<FileMetadata> node, @NotNull Command command) {
        if (node == null) return false;
        if (command.hasAnyFlag()) {
            System.out.print(Colors.format("The command don't needs flags", Colors.RED));
            return false;
        }
        return true;
    }

    public FileAnalyzer() {
    }

    public @NotNull String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        @NotNull String pre = "KMGTPE".charAt(exp-1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    public static final class Result {
        private final @NotNull AtomicLong totalSize = new AtomicLong(0);
        private final @NotNull AtomicInteger fileCount = new AtomicInteger(0);
        private final @NotNull AtomicInteger dirCount = new AtomicInteger(0);
        private  final @NotNull Map<String, Integer> extensions = new HashMap<>();

        public long getTotalSize() {
            return totalSize.get();
        }

        public int getFileCount() {
            return fileCount.get();
        }

        public int getDirCount() {
            return dirCount.get();
        }

        public @NotNull Map<String, Integer> getExtensionMap() {
            return extensions;
        }

        public void addFile(long size, @Nullable String string) {
            fileCount.incrementAndGet();
            totalSize.addAndGet(size);
            if (string != null) {
                extensions.put(string, extensions.getOrDefault(string, 0) + 1);
            }
        }

        public void addDirectory() {
            dirCount.incrementAndGet();
        }
    }
}
