package codes.matheus.core;

import codes.matheus.cli.CommandHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Scanner;

public final class Core {
    private final @NotNull CommandHandler handler;
    private boolean running;

    public Core() {
        this.handler = new CommandHandler();
        this.running = false;
    }

    public void run() {
        final @NotNull Scanner scanner = new Scanner(System.in);
        running = true;

        while (running) {
            System.out.print("\n> ");
            @NotNull String input = scanner.nextLine();

            if (input.equalsIgnoreCase("exit")) {
                running = false;
            }

            try {
                handler.execute();
            } catch (Exception e) {
                throw new RuntimeException("Critical error: " + e.getMessage());
            }
        }
        scanner.close();
    }
}
