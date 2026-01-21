package codes.matheus.core;

import codes.matheus.cli.CommandHandler;
import codes.matheus.datastructures.tree.NaryTree;
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
        this.handler = new CommandHandler();
        this.build = new BuildTree();
        this.running = false;
    }

    public @Nullable NaryTree.Node<FileMetadata> getCurrent() {
        return current;
    }

    public void setCurrent(@Nullable NaryTree.Node<FileMetadata> current) {
        this.current = current;
    }

    public void run() {
        @NotNull String path = System.getProperty("user.home");
        System.out.println("Initializing system at: " + path);

        build.load(path);
        System.out.println("Success loading tree...");

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

            if (input.equalsIgnoreCase("exit")) {
                running = false;
            }

            handler.execute(input);
        }
        scanner.close();
    }
}
