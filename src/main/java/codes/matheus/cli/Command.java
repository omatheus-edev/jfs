package codes.matheus.cli;

import codes.matheus.util.Colors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Command {

    // static initializers

    public static @NotNull Command create(@NotNull String input) {
        @NotNull CommandParsed cmd = Parser.parse(input);
        @NotNull Type type = Type.fromAction(cmd.action);
        return new Command(input, type, cmd.action, cmd.args, cmd.flags);
    }

    private static final @NotNull Map<Command.Type, List<String>> commands = new HashMap<>();

    public static @NotNull Map<Type, List<String>> getCommands() {
        return commands;
    }

    static {
        commands.put(Type.SYSTEM,     List.of("exit", "clear"));
        commands.put(Type.NAVIGATION, List.of("cd", "ls", "find", "pwd"));
        commands.put(Type.IO,         List.of("mkdir", "mv", "rm", "print", "rename"));
        commands.put(Type.ENCODING,   List.of("crypto", "zip", "unzip"));
        commands.put(Type.ANALYSIS,   List.of("analyze", "stats"));
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

    public int getArgsSize() {
        return args != null ? args.size() : -1;
    }

    public boolean hasAnyArg() {
        return args != null && !args.isEmpty();
    }

    public boolean hasAnyFlag() {
        return flags != null && !flags.isEmpty();
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
            if (input.trim().isEmpty()) {
                return new CommandParsed("", new ArrayList<>(), new HashMap<>());
            }

            @NotNull List<String> tokens = new ArrayList<>();
            @NotNull Matcher matcher = PATTERN.matcher(input);

            while (matcher.find()) {
                @NotNull String match = matcher.group(1) != null ? matcher.group(1) :
                        (matcher.group(2) != null ? matcher.group(2) : matcher.group());
                tokens.add(match);
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
        ANALYSIS,
        UNKNOWN;

        public static @NotNull Type fromAction(@NotNull String action) {
            for (@NotNull Map.Entry<Type, List<String>> entry : getCommands().entrySet()) {
                if (entry.getValue().contains(action.toLowerCase())) {
                    return entry.getKey();
                }
            }
            System.out.println(Colors.format("Type of command unknown: " + action, Colors.RED));
            return UNKNOWN;
        }
    }
}
