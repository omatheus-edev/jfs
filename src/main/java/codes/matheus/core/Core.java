package codes.matheus.core;

import codes.matheus.cli.CommandHandler;
import codes.matheus.datastructures.tree.NaryTree;
import codes.matheus.util.Colors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Scanner;

public final class Core {
    private final @NotNull CommandHandler handler;
    private final @NotNull BuildTree build;
    private @Nullable NaryTree.Node<FileMetadata> current;
    private boolean running;

    public Core() {
        this.build = new BuildTree();
        this.handler = new CommandHandler(this, build);
        this.running = false;
    }

    public @Nullable NaryTree.Node<FileMetadata> getCurrent() {
        return current;
    }

    public void setCurrent(@Nullable NaryTree.Node<FileMetadata> current) {
        this.current = current;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public void run() {
        @NotNull String path = System.getProperty("user.home");
        System.out.println(Colors.format("Initializing system at: " + path, Colors.GREEN));

        build.load(path);
        System.out.println(Colors.format("Success loading tree...", Colors.GREEN));

        if (build.getTree() != null) {
            @NotNull FileMetadata rootTarget = new FileMetadata(new File(path));
            this.current = build.getTree().search(rootTarget);
        }

        final @NotNull Scanner scanner = new Scanner(System.in);
        running = true;

        while (running) {
            @NotNull String prompt = (current != null) ? current.getValue().getName() : "shell";
            System.out.print(prompt + " > ");
            @NotNull String input = scanner.nextLine();
            handler.execute(input);
        }
        scanner.close();
    }
}
