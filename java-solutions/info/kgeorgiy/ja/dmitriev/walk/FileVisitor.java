package info.kgeorgiy.ja.dmitriev.walk;

import info.kgeorgiy.ja.dmitriev.walk.hash.HashAlgorithm;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/*package-private*/ class FileVisitor extends SimpleFileVisitor<Path> {
    private final HashWriter writer;
    private final HashAlgorithm hash;

    public FileVisitor(final HashWriter writer, final HashAlgorithm hash) {
        this.writer = writer;
        this.hash = hash;
    }

    private FileVisitResult writeResult(final String hash, final Path file) throws IOException {
        writer.write(hash, file.toString());
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
        try (final var byteReader = new BufferedInputStream(Files.newInputStream(file))) {
            return writeResult(hash.getHash(file, byteReader), file);
        } catch (final IOException e) {
            return visitFileFailed(file, e);
        }
    }

    @Override
    public FileVisitResult visitFileFailed(final Path file, final IOException exc) throws IOException {
        return writeResult(hash.zeroAnswer(), file);
    }
}
