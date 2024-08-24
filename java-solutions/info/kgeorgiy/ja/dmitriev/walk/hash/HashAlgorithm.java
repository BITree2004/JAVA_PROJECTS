package info.kgeorgiy.ja.dmitriev.walk.hash;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HexFormat;

public abstract class HashAlgorithm {
    private static final int BUFFER_LENGTH = 1 << 5;
    protected static final HexFormat HEX_FORMAT = HexFormat.of();
    protected final byte[] buffer;
    private final String format;

    protected HashAlgorithm(final String format) {
        this.format = format;
        buffer = new byte[BUFFER_LENGTH];
    }

    public abstract String getHash(Path file, InputStream reader) throws IOException;

    public String zeroAnswer() {
        return String.format(format, 0);
    }
}