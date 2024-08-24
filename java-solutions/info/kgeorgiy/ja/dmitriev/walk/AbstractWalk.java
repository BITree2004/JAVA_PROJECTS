package info.kgeorgiy.ja.dmitriev.walk;

import info.kgeorgiy.ja.dmitriev.walk.hash.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Predicate;

/*package-private*/ class AbstractWalk {
    private final Predicate<Path> isApplied;

    /*package-private*/ AbstractWalk(final Predicate<Path> isApplied) {
        this.isApplied = isApplied;
    }

    private Path getPath(final String file, final String name) throws WalkException {
        try {
            return Path.of(file);
        } catch (final InvalidPathException e) {
            // :NOTE: input or output? | fixed
            throw new WalkException("It isn't correct " + name + " file : " + e.getMessage(), e);
        }
    }

    /*package-private*/ void walk(
            final String input,
            final String output,
            final HashAlgorithm hash
    ) throws WalkException {
        final var inputPath = getPath(input, "input");
        final var outputPath = getPath(output, "output");

        final Path outputDir = outputPath.getParent();
        try {
            if (outputDir != null) {
                Files.createDirectories(outputDir);
            }
        } catch (final IOException e) {
            System.err.println("Warning: can't create the folder of output file!");
        }

        try (final var reader = Files.newBufferedReader(inputPath)) {
            try (final var writer = new HashWriter(outputPath)) {
                walk(reader, writer, hash);
            } catch (final IOException e) {
                throw new WalkException("Error, when open output file: " + e.getMessage(), e);
            }
        } catch (final IOException e) {
            throw new WalkException("Error, when open input file: " + e.getMessage(), e);
        }
    }

    private void walk(
            final BufferedReader reader,
            final HashWriter writer,
            final HashAlgorithm hash
    ) throws WalkException {
        try {
            final var visitor = new FileVisitor(writer, hash);
            String file;
            while ((file = reader.readLine()) != null) {
                try {
                    try {
                        final Path path = Path.of(file);
                        if (isApplied.test(path)) {
                            Files.walkFileTree(path, visitor);
                        } else {
                            writer.write(hash.zeroAnswer(), file);
                        }
                    } catch (final InvalidPathException e) {
                        writer.write(hash.zeroAnswer(), file);
                    }
                } catch (final IOException e) {
                    throw new WalkException("Error, when write to output file: " + e.getMessage(), e);
                } catch (final SecurityException e) {
                    throw new WalkException(e.getMessage(), e);
                }
            }
        } catch (final IOException e) {
            throw new WalkException("Error, when read from input file: " + e.getMessage(), e);
        } catch (final SecurityException e) {
            throw new WalkException(e.getMessage(), e);
        }
    }


    private static final Map<String, HashAlgorithm> ALGORITHMS_MAP = Map.of(
            "jenkins", new Jenkins(),
            "sha-1", new Sha1()
    );

    private static HashAlgorithm chooseAlgorithm(final String arg) throws WalkException {
        final var res = ALGORITHMS_MAP.get(arg);
        if (res == null) {
            throw new WalkException("Not supported hash algorithm: " + arg);
        }
        return res;
    }


    /*package-private*/ void walk(final String[] args) throws WalkException {
        if (args == null || 3 < args.length || args.length < 2) {
            throw new WalkException("Expected arguments: input_file output_file [jenkins/sha-1]");
        }
        for (int i = 0; i < args.length; i++) {
            if (args[i] == null) {
                throw new WalkException("Arg number " + (i + 1) + " is null!");
            }
        }
        walk(args[0], args[1], chooseAlgorithm(args.length == 2 ? "jenkins" : args[2]));
    }

    /*package-private*/
    void walkWithLog(final String[] args) {
        try {
            walk(args);
        } catch (final WalkException e) {
            System.err.println("Program end with error: " + e.getMessage());
        }
    }
}