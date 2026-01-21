package codes.matheus.cli;

import org.jetbrains.annotations.NotNull;

public final class CommandHandler {
    public void execute(@NotNull String input) {
        @NotNull Command command = Command.create(input);
    }
}
