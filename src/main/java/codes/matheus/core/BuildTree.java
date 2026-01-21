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
            fillTree(root, rootNode);
        }
    }

    private void fillTree(@NotNull File parent, @NotNull NaryTree.Node<FileMetadata> parentNode) {
        @NotNull File[] children = parent.listFiles();
        if (children == null || tree ==  null) return;

        for (@NotNull File child : children) {
            @NotNull FileMetadata meta = new FileMetadata(child);
            @NotNull NaryTree.Node<FileMetadata> node = tree.insert(parentNode, meta);

            if (child.isDirectory()) {
                fillTree(child, node);
            }
        }
    }
}
