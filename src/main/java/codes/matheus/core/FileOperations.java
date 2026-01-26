package codes.matheus.core;

import codes.matheus.cli.Command;
import codes.matheus.datastructures.tree.NaryTree;
import codes.matheus.util.Colors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public final class FileOperations {
    private final @NotNull Core core;
    private final @NotNull BuildTree build;
    private final @NotNull Map<String, Consumer<Command>> actions = new HashMap<>();

    public FileOperations(@NotNull Core core, @NotNull BuildTree build) {
        this.core = core;
        this.build = build;
        registerActions();
    }

    private void registerActions() {
        // Navigations
        actions.put("ls", this::ls);
        actions.put("cd", this::cd);
        actions.put("pwd", this::pwd);
        actions.put("find", this::find);

        // analysis
        actions.put("stats", this::stats);
    }

    public void execute(@NotNull Command command) {
        @Nullable Consumer<Command> action = actions.get(command.getAction());

        if (action != null) {
            action.accept(command);
        }
    }

    private void ls(@NotNull Command command) {
        if (command.hasAnyFlag()) {
            System.out.print(Colors.format("The command don't needs arguments and flags", Colors.RED));
            return;
        }

        @NotNull String arg = command.getArg(0);
        @Nullable NaryTree.Node<FileMetadata> targetNode = arg.isEmpty()
                ? core.getCurrent()
                : searchPath(arg);

        if (targetNode != null) {
            if (!targetNode.getValue().isDirectory()) {
                System.out.println(Colors.format("Error: Path " + command.getArg(0) + " is not a directory", Colors.RED));
                return;
            }

            build.fetchChildren(targetNode);
            if (targetNode.getChildren().isEmpty()) {
                System.out.print("Directory is empty.");
            }

            for (@NotNull NaryTree.Node<FileMetadata> child : targetNode.getChildren()) {
                if (child.getValue().isDirectory()) {
                    System.out.print(Colors.format(child.getValue().getName() + "/ ", Colors.WHITE));
                }
            }
        } else {
            System.out.print(Colors.format("Error: Path " + arg + " not found", Colors.RED));
        }
        System.out.println();
    }

    private void cd(@NotNull Command command) {
        if (command.hasAnyFlag()) {
            System.out.print(Colors.format("The command don't needs arguments and flags", Colors.RED));
            return;
        }

        if (command.hasAnyArg() && core.getCurrent() != null)  {
            @Nullable NaryTree.Node<FileMetadata> targetNode = searchPath(command.getArg(0));

            if (targetNode != null && targetNode.getValue().isDirectory()) {
                core.setCurrent(targetNode);
            } else {
                System.out.println(Colors.format("Error: Path " + command.getArg(0) + " not found or is not a directory", Colors.RED));
            }
        } else {
            if (build.getTree() != null) {
                @NotNull String rootPath = System.getProperty("user.home");
                @NotNull NaryTree.Node<FileMetadata> root = Objects.requireNonNull(build.getTree().search(new FileMetadata(new File(rootPath))));
                core.setCurrent(root);
            }
        }
    }

    private void pwd(@NotNull Command command) {
        if (command.hasAnyFlag()) {
            System.out.print(Colors.format("The command don't needs arguments and flags", Colors.RED));
            return;
        }

        @Nullable NaryTree.Node<FileMetadata> target = core.getCurrent();
        if (target != null) {
            System.out.println(Colors.format(target.getValue().getAbsolutePath(), Colors.WHITE));
        }
    }

    private void find(@NotNull Command command) {
        if (!command.hasAnyArg() && !command.hasAnyFlag()) {
            System.out.print(Colors.format("The command needs arguments and flags", Colors.RED));
            return;
        }

        @Nullable NaryTree.Node<FileMetadata> node = core.getCurrent();
        if (node == null) return;

        @NotNull String query = command.hasFlag("--name") ? command.getFlag("--name") : command.getArg(0);

        if (query.isEmpty()) {
            System.out.print(Colors.format("Search query is missing.", Colors.RED));
            return;
        }

        boolean onlyDirs = command.hasFlag("--type") && command.getFlag("--type").equalsIgnoreCase("d");
        boolean onlyFiles = command.hasFlag("--type") && command.getFlag("--type").equalsIgnoreCase("f");
        int maxDepth = Integer.MAX_VALUE;

        if (command.hasFlag("--maxdepth")) {
            try {
                maxDepth = Integer.parseInt(command.getFlag("--maxdepth"));
            } catch (NumberFormatException e) {
                System.out.println(Colors.format("Invalid max depth, using default.", Colors.YELLOW));
            }
        }

        System.out.println(Colors.format("Searching for: " + query, Colors.WHITE));
        searchRec(node, query, onlyDirs, onlyFiles, 0, maxDepth);
    }

    private void stats(@NotNull Command command) {
        if (core.getCurrent() == null) return;
        if (command.hasAnyFlag()) {
            System.out.print(Colors.format("The command don't needs flags", Colors.RED));
            return;
        }

        @Nullable NaryTree.Node<FileMetadata> target = command.hasAnyArg()
                ? searchPath(command.getArg(0))
                : core.getCurrent();

        if (target == null) {
            System.out.println(Colors.format("Error: path not found", Colors.RED));
            return;
        }

        @NotNull FileMetadata meta = target.getValue();
        System.out.println(Colors.format("--- Statistics: " + meta.getName() + " ---", Colors.CYAN));
        System.out.println("Type: " + (meta.isDirectory() ? "Directory" : "File"));
        System.out.println("Path: " + meta.getAbsolutePath());

        if (meta.isDirectory()) {
            build.fetchChildren(target);
            System.out.println("Children count: " + target.getChildren().size());
        } else {
            System.out.println("Size: " + meta.getSize());
        }
    }

    private void searchRec(@NotNull NaryTree.Node<FileMetadata> current, @NotNull String query, boolean onlyDirs, boolean onlyFiles, @Range(from = 0, to = Integer.MAX_VALUE) int currentDepth, @Range(from = 0, to = Integer.MAX_VALUE) int maxDepth) {
        if (currentDepth > maxDepth) return;

        build.fetchChildren(current);
        for (@NotNull NaryTree.Node<FileMetadata> child : current.getChildren()) {
            @NotNull FileMetadata meta = child.getValue();
            @NotNull String name = meta.getName();

            if (name.toLowerCase().contains(query)) {
                boolean matchesType = (!onlyDirs && !onlyFiles) ||
                        (onlyDirs && meta.isDirectory()) ||
                        (onlyFiles && !meta.isDirectory());

                if (matchesType) {
                    System.out.println(Colors.format("Found: " + meta.getAbsolutePath(), Colors.GREEN));
                }
            }

            if (meta.isDirectory()) {
                searchRec(child, query, onlyDirs, onlyFiles, currentDepth + 1, maxDepth);
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
        @NotNull String[] parts = path.split("/");

        for (int i = 0; i < parts.length; i++) {
            @NotNull String part = parts[i];
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

            if (next == null) {
                return null;
            }

            if (i < parts.length - 1 && !next.getValue().isDirectory()) {
                return null;
            }

            target = next;
        }

        return target;
    }
}
