package codes.matheus.encoding;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public sealed interface Encoding<T> permits CryptoEncoding, HashEncoding, ZipEncoding{
    @NotNull T encode(@NotNull File file);
}
