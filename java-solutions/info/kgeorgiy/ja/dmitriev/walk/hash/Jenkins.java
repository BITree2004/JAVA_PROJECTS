package info.kgeorgiy.ja.dmitriev.walk.hash;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public class Jenkins extends HashAlgorithm {
    public Jenkins() {
        super("%08x");
    }

    @Override
    public String getHash(final Path file, final InputStream byteReader) throws IOException {
        int hash = 0;
        int length;
        while ((length = byteReader.read(buffer)) != -1) {
            for (int i = 0; i < length; i++) {
                hash += buffer[i] & 0xff;
                hash += hash << 10;
                hash ^= hash >>> 6;
            }
        }
        hash += hash << 3;
        hash ^= hash >>> 11;
        hash += hash << 15;
        return HEX_FORMAT.toHexDigits(hash);
    }
}