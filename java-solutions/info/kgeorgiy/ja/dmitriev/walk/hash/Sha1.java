package info.kgeorgiy.ja.dmitriev.walk.hash;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Sha1 extends HashAlgorithm {
    private final MessageDigest md;

    public Sha1() {
        super("%040x");
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (final NoSuchAlgorithmException e) {
            throw new AssertionError("Not supported hash algorithm SHA-1", e);
        }
    }

    @Override
    public String getHash(final Path file, final InputStream byteReader) throws IOException {
        try (final var digestStream = new DigestInputStream(byteReader, md)) {
            while (digestStream.read(buffer) != -1) {
            }
            return HEX_FORMAT.formatHex(md.digest());
        }
    }
}