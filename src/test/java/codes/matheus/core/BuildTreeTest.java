package codes.matheus.core;

import codes.matheus.datastructures.tree.NaryTree;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public final class BuildTreeTest {
    private final @NotNull BuildTree buildTree = new BuildTree();
    @TempDir
    private @NotNull Path dir;

    @Test
    void testLoadTree() throws IOException {
        Files.createFile(dir.resolve("arquivo.txt"));
        @NotNull File rootFile = dir.toFile();
        @NotNull FileMetadata rootMeta = new FileMetadata(rootFile);

        buildTree.load(dir.toString());
        @Nullable NaryTree<FileMetadata> tree = buildTree.getTree();
        assertNotNull(tree);

        @Nullable NaryTree.Node<FileMetadata> rootNode = tree.search(rootMeta);
        assertNotNull(rootNode);
        assertEquals(rootFile.getName(), rootNode.getValue().getName());
    }

    @Test
    void testFetchChildren() throws IOException {
        Path subFolderPath = Files.createDirectory(dir.resolve("Downloads"));
        Files.createFile(subFolderPath.resolve("test.txt"));

        buildTree.load(dir.toString());

        @Nullable NaryTree<FileMetadata> tree = buildTree.getTree();
        @NotNull FileMetadata rootMeta = new FileMetadata(dir.toFile());
        @Nullable NaryTree.Node<FileMetadata> rootNode = tree.search(rootMeta);

        @Nullable NaryTree.Node<FileMetadata> downloadsNode = rootNode.getChildren().get(0);
        assertEquals("Downloads", downloadsNode.getValue().getName());

        assertTrue(downloadsNode.getChildren().isEmpty());

        buildTree.fetchChildren(downloadsNode);

        assertFalse(downloadsNode.getChildren().isEmpty());
        assertEquals("test.txt", downloadsNode.getChildren().get(0).getValue().getName());
    }

    @Test
    void testIgnoreHidden() throws IOException {
        Files.createFile(dir.resolve(".config"));
        Files.createFile(dir.resolve("readme.md"));

        buildTree.load(dir.toString());

        @Nullable NaryTree<FileMetadata> tree = buildTree.getTree();
        @Nullable NaryTree.Node<FileMetadata> rootNode = tree.search(new FileMetadata(dir.toFile()));

        assertEquals(1, rootNode.getChildren().size());
        assertEquals("readme.md", rootNode.getChildren().get(0).getValue().getName());
    }
}