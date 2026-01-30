package codes.matheus.cli;

import codes.matheus.core.BuildTree;
import codes.matheus.core.Core;
import codes.matheus.core.FileOperations;
import codes.matheus.util.Colors;
import org.jetbrains.annotations.NotNull;

public final class CommandHandler {
    private final @NotNull FileOperations operations;

    public CommandHandler(@NotNull Core core, @NotNull BuildTree build) {
        this.operations = new FileOperations(core, build);
    }

    public void execute(@NotNull String input) {
        if (input.isBlank()) return;

        try {
            @NotNull Command command = Command.create(input);
            if (command.getType().equals(Command.Type.UNKNOWN)) {
                return;
            }

            operations.execute(command);
        } catch (Exception e) {
            System.out.println(Colors.format("Error in execute command: " + e.getMessage(), Colors.RED));
        }
    }
}
