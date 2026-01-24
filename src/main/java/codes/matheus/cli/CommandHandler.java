package codes.matheus.cli;

import codes.matheus.core.BuildTree;
import codes.matheus.core.Core;
import codes.matheus.core.FileOperations;
import org.jetbrains.annotations.NotNull;

public final class CommandHandler {
    private final @NotNull FileOperations operations;
    private final @NotNull Core core;

    public CommandHandler(@NotNull Core core, @NotNull BuildTree build) {
        this.core = core;
        this.operations = new FileOperations(core, build);
    }

    public void execute(@NotNull String input) {
        @NotNull Command command = Command.create(input);
        if (command.getType().equals(Command.Type.UNKNOWN)) return;

        operations.execute(command);
    }
}
