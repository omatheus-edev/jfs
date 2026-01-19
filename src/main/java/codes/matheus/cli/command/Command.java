package codes.matheus.cli.command;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Command {

    // static initializers
    public static @NotNull Command create(@NotNull String input) {
        @NotNull CommandParsed cmd = Parser.parse(input);
        return new Command(input, cmd.action, cmd.args, cmd.flags);
    }

    // Objects

    private final @NotNull String input;
    private final @NotNull String action;
    private final @Nullable List<String> args;
    private final @Nullable Map<String, String> flags;

    // Constructor

    Command(@NotNull String input, @NotNull String action, @Nullable List<String> args, @Nullable Map<String, String> flags) {
        this.input = input;
        this.action = action;
        this.args = args;
        this.flags = flags;
    }


    // Getters


    public @NotNull String getInput() {
        return input;
    }

    public @NotNull String getAction() {
        return action;
    }

    public @NotNull String getArg(int index) {
        return (args != null && index >= 0 && index < args.size()) ? args.get(index) : "";
    }

    public @NotNull String getFlag(@NotNull String key) {
        return (flags != null) ? flags.get(key) : "";
    }

    public boolean hasFlag(@NotNull String key) {
        return flags != null && flags.containsKey(key);
    }

//    public abstract void execute();

    @Override
    public @NotNull String toString() {
        return "Command=" + input +
                "args=" + args +
                "flags=" + flags;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Command command = (Command) o;
        return Objects.equals(input, command.input);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(input);
    }

    // Classes

    private static final class CommandParsed {
        private final @NotNull String action;
        private final @Nullable List<String> args;
        private final @Nullable Map<String, String> flags;

        private CommandParsed(@NotNull String action, @Nullable List<String> args, @Nullable Map<String, String> flags) {
            this.action = action;
            this.args = args;
            this.flags = flags;
        }
    }

    private static final class Parser {
        private static final @NotNull Pattern PATTERN = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");

        private Parser() {
            throw new UnsupportedOperationException();
        }

        private static @NotNull CommandParsed parse(@NotNull String input) {
            @NotNull List<String> tokens = new ArrayList<>();
            @NotNull Matcher matcher = PATTERN.matcher(input);

            while (matcher.find()) {
                if (matcher.group(1) != null) {
                    tokens.add(matcher.group(1));
                } else if (matcher.group(2) != null) {
                    tokens.add(matcher.group(2));
                } else {
                        tokens.add(matcher.group());
                }
            }

            @NotNull String action = tokens.remove(0).toLowerCase();
            @NotNull List<String> args = new ArrayList<>();
            @NotNull Map<String, String> flags = new HashMap<>();

            for (int i = 0; i < tokens.size(); i++) {
                @NotNull String t = tokens.get(i);
                if (t.startsWith("--")) {
                    if (i + 1 < tokens.size() && !tokens.get(i + 1).startsWith("--")) {
                        flags.put(t, tokens.get(i + 1));
                        i++;
                    } else {
                        flags.put(t, "true");
                    }
                } else {
                    args.add(t);
                }
            }
            return new CommandParsed(action, args, flags);
        }
    }

    enum Type {
        SYSTEM,
        NAVIGATION,
        IO,
        ENCODING,
        ANALYSIS
    }
}
