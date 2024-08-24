package info.kgeorgiy.ja.dmitriev.walk;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/*package-private*/ class HashWriter extends BufferedWriter {
    public HashWriter(final Path file) throws IOException {
        super(Files.newBufferedWriter(file));
    }

    public void write(final String hash, final String file) throws IOException {
        super.write(hash + " " + file);
        super.newLine();
    }
}