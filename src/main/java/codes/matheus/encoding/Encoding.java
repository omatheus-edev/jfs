package codes.matheus.encoding;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public interface Encoding<T> {
    @NotNull T encode(@NotNull File file);
}
