package codes.matheus.cli;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public final class CommandParsed {
    private final @NotNull String input;
    private final @NotNull List<String> args;
    private final @NotNull Map<String, String> flags;

    public CommandParsed(@NotNull String input, @NotNull List<String> args, @NotNull Map<String, String> flags) {
        this.input = input;
        this.args = args;
        this.flags = flags;
    }

    public @NotNull String getInput() {
        return input;
    }

    public @Nullable String getArg(int index) {
        return (index >= 0 && index < args.size()) ? args.get(index) : null;
    }

    public @NotNull String getFlag(@NotNull String key) {
        return flags.get(key);
    }

    public boolean hasFlag(@NotNull String key) {
        return flags.containsKey(key);
    }

    @Override
    public String toString() {
        return "Command=" + input +
                "args=" + args +
                "flags=" + flags;
    }
}
