package codes.matheus.core;

import codes.matheus.datastructures.tree.NaryTree;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public final class BuildTree {
    private @Nullable NaryTree<FileMetadata> tree;

    public BuildTree() {
    }

    public @Nullable NaryTree<FileMetadata> getTree() {
        return tree;
    }

    public void load(@NotNull String rootPath) {
        @NotNull File root = new File(rootPath);
        if (!root.exists()) {
            return;
        }

        @NotNull FileMetadata rootMeta = new FileMetadata(root);
        this.tree = new NaryTree<>(rootMeta);
        @Nullable NaryTree.Node<FileMetadata> rootNode = tree.search(rootMeta);

        if (rootNode != null && root.isDirectory()) {
            fetchChildren(rootNode);
        }
    }

    public void fetchChildren(@NotNull NaryTree.Node<FileMetadata> parentNode) {
        if (!parentNode.getChildren().isEmpty() || tree == null) {
            return;
        }

        @NotNull String path = parentNode.getValue().getAbsolutePath();
        @NotNull File parentFile = new File(path);
        @NotNull File[] children = parentFile.listFiles();

        if (children == null) {
            return;
        }

        for (@NotNull File child : children) {
            if (child.getName().startsWith(".")) {
                continue;
            }

            @NotNull FileMetadata meta = new FileMetadata(child);
            tree.insert(parentNode, meta);
        }
    }
}
