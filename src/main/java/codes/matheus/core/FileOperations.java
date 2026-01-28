package codes.matheus.core;

import codes.matheus.cli.Command;
import codes.matheus.datastructures.tree.NaryTree;
import codes.matheus.util.Colors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public final class FileOperations {
    private final @NotNull Core core;
    private final @NotNull BuildTree build;
    private final @NotNull FileAnalyzer analyzer;
    private final @NotNull Map<String, Consumer<Command>> actions = new HashMap<>();

    public FileOperations(@NotNull Core core, @NotNull BuildTree build) {
        this.core = core;
        this.build = build;
        this.analyzer = new FileAnalyzer();
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
        actions.put("analyze", this::analyze);

        // io
        actions.put("mkdir", this::mkdir);
        actions.put("rm", this::rm);
        actions.put("print", this::print);
        actions.put("rename", this::rename);
        actions.put("mv", this::mv);
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
        if (!FileAnalyzer.validate(core.getCurrent(), command)) return;

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
            System.out.println("Size: " + analyzer.formatSize(meta.getSize()));
        }
    }

    private void analyze(@NotNull Command command) {
        if (!FileAnalyzer.validate(core.getCurrent(), command)) return;

        @Nullable NaryTree.Node<FileMetadata> target = command.hasAnyArg()
                ? searchPath(command.getArg(0))
                : core.getCurrent();

        if (target == null) {
            System.out.println(Colors.format("Error: path not found", Colors.RED));
            return;
        }

        System.out.println(Colors.format("Analyzing: " + target.getValue().getAbsolutePath(), Colors.CYAN));
        @NotNull FileAnalyzer.Result result = new FileAnalyzer.Result();
        analysisRec(target, result);

        System.out.println(Colors.format("\n--- Analysis Results ---", Colors.YELLOW));
        System.out.println("Total Size:  " + analyzer.formatSize(result.getTotalSize()));
        System.out.println("Files:       " + result.getFileCount());
        System.out.println("Directories: " + result.getDirCount());
        System.out.println("\nExtensions:");
        result.getExtensionMap().forEach((ext, count) ->
                System.out.println(" ." + ext + ": " + count));
        System.out.println(Colors.format("-----------------------", Colors.YELLOW));
    }

    private void mkdir(@NotNull Command command) {
        if (core.getCurrent() == null) return;
        if (command.hasAnyFlag()) {
            System.out.print(Colors.format("The command don't needs flags", Colors.RED));
            return;
        } if (!command.hasAnyArg()) {
            System.out.print(Colors.format("The command needs arg", Colors.RED));
            return;
        }

        @NotNull String path = command.getArg(0);
        @NotNull File target = new File(core.getCurrent().getValue().getAbsolutePath(), path);

        if (target.exists()) {
            System.out.println(Colors.format("Error: path already exists", Colors.RED));
            return;
        }
        if (target.mkdirs()) {
            @NotNull String parent = target.getParent();
            @Nullable NaryTree.Node<FileMetadata> parentNode = searchPath(parent);

            if (parentNode != null) {
                parentNode.getChildren().clear();
                build.fetchChildren(parentNode);
            }
            System.out.println(Colors.format("Directory structure created: " + path, Colors.GREEN));
        } else {
            System.out.println(Colors.format("Error: failed to create structure", Colors.RED));
        }
    }

    private void rm(@NotNull Command command) {
        if (core.getCurrent() == null) return;
        if (command.hasAnyFlag()) {
            System.out.print(Colors.format("The command don't needs flags", Colors.RED));
            return;
        } if (!command.hasAnyArg()) {
            System.out.print(Colors.format("The command needs arg", Colors.RED));
            return;
        }

        @NotNull String path = command.getArg(0);
        @NotNull File target = new File(core.getCurrent().getValue().getAbsolutePath(), path);
        if (!target.exists()) {
            System.out.println(Colors.format("Error: path not found", Colors.RED));
            return;
        }

        @NotNull String parentPath = ".";
        if (path.contains("/")) {
            parentPath = path.substring(0, path.lastIndexOf("/"));
        }

        @Nullable NaryTree.Node<FileMetadata> parentNode = searchPath(parentPath);
        deleteRec(target);
        if (parentNode != null) {
            parentNode.clear();
            build.fetchChildren(parentNode);
            System.out.println(Colors.format("Removed: " + path, Colors.GREEN));
        } else {
            core.getCurrent().clear();
            build.fetchChildren(core.getCurrent());
        }
    }

    private void print(@NotNull Command command) {
        if (core.getCurrent() == null) return;
        if (command.hasAnyFlag()) {
            System.out.print(Colors.format("The command don't needs flags", Colors.RED));
            return;
        } if (!command.hasAnyArg()) {
            System.out.print(Colors.format("The command needs arg", Colors.RED));
            return;
        }

        @NotNull String path = command.getArg(0);
        @NotNull File target = new File(core.getCurrent().getValue().getAbsolutePath(), path);
        if (!target.exists()) {
            System.out.println(Colors.format("Error: path not found", Colors.RED));
            return;
        } if (target.isDirectory()) {
            System.out.println(Colors.format("Error: path " + path + "is directory", Colors.RED));
            return;
        }

        try (@NotNull BufferedReader reader = new BufferedReader(new FileReader(target))) {
            @NotNull String line;
            System.out.println(Colors.format("--- Content of: " + path + " ---", Colors.CYAN));
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            System.out.println(Colors.format("---------------------------", Colors.CYAN));
        } catch (IOException e) {
            System.out.println(Colors.format("Error reading the file: " + e.getLocalizedMessage(), Colors.RED));
        }
    }

    private void rename(@NotNull Command command) {
        if (core.getCurrent() == null) return;
        if (command.hasAnyFlag()) {
            System.out.println(Colors.format("The command don't needs flags", Colors.RED));
            return;
        } if (!command.hasAnyArg()) {
            System.out.println(Colors.format("The command needs arg", Colors.RED));
            return;
        }

        @NotNull String path = command.getArg(0);
        @NotNull String newName = command.getArg(1);
        @NotNull File origin = new File(core.getCurrent().getValue().getAbsolutePath(), path);
        if (!origin.exists()) {
            System.out.println(Colors.format("The file or folder doesn't exists", Colors.RED));
            return;
        }

        @NotNull String parentPath = origin.getParent();
        @NotNull File target = new File(parentPath, newName);
        @Nullable NaryTree.Node<FileMetadata> parentNode = searchPath(parentPath);

        if (origin.renameTo(target)) {
            if (parentNode != null) {
                parentNode.clear();
                build.fetchChildren(parentNode);
                System.out.println(Colors.format("Renamed successfully: " + path + " -> " + newName, Colors.GREEN));
            } else {
                core.getCurrent().clear();
                build.fetchChildren(core.getCurrent());
                System.out.println(Colors.format("Renamed successfully (view refreshed)", Colors.GREEN));
            }
        } else {
            System.out.println(Colors.format("Error: Could not rename. Check if target name already exists.", Colors.RED));
        }
    }

    private void mv(@NotNull Command command) {
        if (core.getCurrent() == null) return;
        if (command.hasAnyFlag()) {
            System.out.println(Colors.format("The command don't needs flags", Colors.RED));
            return;
        } if (!command.hasAnyArg() || command.getArgsSize() < 2) {
            System.out.println(Colors.format("Usage: mv <source> <target_directory>", Colors.RED));
            return;
        }

        @NotNull String originPath = command.getArg(0);
        @NotNull String targetPath = command.getArg(1);
        @NotNull File origin = new File(core.getCurrent().getValue().getAbsolutePath(), originPath);
        @NotNull File target = new File(core.getCurrent().getValue().getAbsolutePath(), targetPath);
        @NotNull File file = new File(target, origin.getName());

        if (!origin.exists()) {
            System.out.println(Colors.format("Source file not found", Colors.RED));
            return;
        }

        if (origin.renameTo(file)) {
            @NotNull String parentOriginPath = ".";
            if (originPath.contains("/")) {
                parentOriginPath = originPath.substring(0, originPath.lastIndexOf("/"));
            }

            @NotNull String parentTargetPath = targetPath;
            @Nullable NaryTree.Node<FileMetadata> nodeOrigin = searchPath(parentOriginPath);
            @Nullable NaryTree.Node<FileMetadata> nodeTarget = searchPath(parentTargetPath);

            if (nodeOrigin != null) {
                nodeOrigin.clear();
                build.fetchChildren(nodeOrigin);
            } else {
                core.getCurrent().clear();
                build.fetchChildren(core.getCurrent());
            }

            if (nodeTarget != null) {
                nodeTarget.clear();
                build.fetchChildren(nodeTarget);
            }
            System.out.println(Colors.format("Moved successfully", Colors.GREEN));
        }
    }

    private void deleteRec(@NotNull File file) {
        @NotNull File[] children = file.listFiles();
        if (children != null) {
            for (@NotNull File child : children) {
                deleteRec(child);
            }
        }
        file.delete();
    }

    private void analysisRec(@NotNull NaryTree.Node<FileMetadata> node, @NotNull FileAnalyzer.Result result) {
        build.fetchChildren(node);
        for (@NotNull NaryTree.Node<FileMetadata> child : node.getChildren()) {
            @NotNull FileMetadata meta = child.getValue();

            if (meta.isDirectory()) {
                result.addDirectory();
            } else {
                @NotNull String name = meta.getName();
                @NotNull String ext = name.contains(".")
                        ? name.substring(name.lastIndexOf(".") + 1).toLowerCase()
                        : "no-ext";
                result.addFile(meta.getSize(), ext);
            }
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
                if (child.getValue().getName().equals(part)) {
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
