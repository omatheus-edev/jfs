package codes.matheus.util;

import org.jetbrains.annotations.NotNull;

public final class Colors {
    private Colors() {
        throw new UnsupportedOperationException("this class cannot be instantiated");
    }

    public static final @NotNull String RESET = "\033[0m";
    public static final String BLACK   = "\033[1;90m";
    public static final String RED     = "\033[1;91m";
    public static final String GREEN   = "\033[1;92m";
    public static final String YELLOW  = "\033[1;93m";
    public static final String BLUE    = "\033[1;94m";
    public static final String PURPLE  = "\033[1;95m";
    public static final String CYAN    = "\033[1;96m";
    public static final String WHITE   = "\033[1;97m";

    public static @NotNull String format(String text, String colorCode) {
        return colorCode + text + RESET;
    }
}
