package codes.matheus.cli;

import codes.matheus.exceptions.CommandException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Command {

    // static initializers

    static {
        getCommands().put(Type.SYSTEM,     List.of("login", "exit", "open", "help"));
        getCommands().put(Type.NAVIGATION, List.of("cd", "ls", "find", "pwd"));
        getCommands().put(Type.IO,         List.of("mkdir", "cp", "mv", "rm", "print", "rename", "bulk-rename"));
        getCommands().put(Type.ENCODING,   List.of("crypto", "zip", "unzip"));
        getCommands().put(Type.ANALYSIS,   List.of("analyze", "stats"));

    }

    public static @NotNull Command create(@NotNull String input) {
        @NotNull CommandParsed cmd = Parser.parse(input);
        @NotNull Type type = Type.fromAction(cmd.action);
        return new Command(input, type, cmd.action, cmd.args, cmd.flags);
    }

    private static final @NotNull Map<Command.Type, List<String>> commands = new HashMap<>();

    public static @NotNull Map<Type, List<String>> getCommands() {
        return commands;
    }

    // Objects

    private final @NotNull String input;
    private final @NotNull Type type;
    private final @NotNull String action;
    private final @Nullable List<String> args;
    private final @Nullable Map<String, String> flags;

    // Constructor

    Command(@NotNull String input, @NotNull Type type, @NotNull String action, @Nullable List<String> args, @Nullable Map<String, String> flags) {
        this.input = input;
        this.type = type;
        this.action = action;
        this.args = args;
        this.flags = flags;
    }

    // Getters

    public @NotNull String getInput() {
        return input;
    }

    public @NotNull Type getType() {
        return type;
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

    public enum Type {
        SYSTEM,
        NAVIGATION,
        IO,
        ENCODING,
        ANALYSIS;

        public static @NotNull Type fromAction(@NotNull String action) {
            for (@NotNull Map.Entry<Type, List<String>> entry : getCommands().entrySet()) {
                if (entry.getValue().contains(action.toLowerCase())) {
                    return entry.getKey();
                }
            }
            throw new CommandException("Type of command unknown: " + action);
        }
    }
}
