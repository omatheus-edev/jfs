package codes.matheus.core;

import codes.matheus.cli.Command;
import codes.matheus.datastructures.tree.NaryTree;
import codes.matheus.util.Colors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Objects;

public final class FileOperations {
    private final @NotNull Core core;
    private final @NotNull BuildTree build;

    public FileOperations(@NotNull Core core, @NotNull BuildTree build) {
        this.core = core;
        this.build = build;
    }

    public void execute(@NotNull Command command) {
        if (command.getType().equals(Command.Type.NAVIGATION)) {
            switch (command.getAction()) {
                case "ls" -> ls(command.getArg(0));
            }
        }
        System.out.println();
    }

    private void ls(@NotNull String arg) {
        @Nullable NaryTree.Node<FileMetadata> targetNode = arg.isEmpty()
                ? core.getCurrent()
                : searchPath(arg);

        if (targetNode != null) {
            handleLs(targetNode);
        } else {
            System.out.print(Colors.format("Error: Path " + arg + " not found", Colors.RED));
        }
    }

    private void handleLs(@NotNull NaryTree.Node<FileMetadata> node) {
        if (!node.getValue().isDirectory()) {
            return;
        }

        build.fetchChildren(node);
        if (node.getChildren().isEmpty()) {
            System.out.println("Directory is empty.");
        }

        for (@NotNull NaryTree.Node<FileMetadata> child : node.getChildren()) {
            if (child.getValue().isDirectory()) {
                System.out.print(Colors.format(child.getValue().getName() + "/ ", Colors.WHITE));
            }
        }
    }

    private @Nullable NaryTree.Node<FileMetadata> searchPath(@NotNull String path) {
        if (path.isEmpty()) return core.getCurrent();

        @Nullable NaryTree.Node<FileMetadata> target;
        if (path.startsWith("/")) {
            @NotNull String rootPath = System.getProperty("user.home");
            target = Objects.requireNonNull(build.getTree()).search(new FileMetadata(new File(rootPath)));
        } else {
            target = core.getCurrent();
        }

        if (target == null) return null;
        String[] parts = path.split("/");

        for (@NotNull String part : parts) {
            if (part.isEmpty() || part.equals(".")) continue;
            if (part.equals("..")) {
                continue;
            }

            build.fetchChildren(target);
            @Nullable NaryTree.Node<FileMetadata> next = null;
            for (@NotNull NaryTree.Node<FileMetadata> child : target.getChildren()) {
                if (child.getValue().getName().equalsIgnoreCase(part)) {
                    next = child;
                    break;
                }
            }

            if (next == null || !next.getValue().isDirectory()) {
                return null;
            }
            target = next;
        }

        return target;
    }
}
