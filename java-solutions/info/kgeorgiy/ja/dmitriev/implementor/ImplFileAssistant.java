package info.kgeorgiy.ja.dmitriev.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;

/**
 * This class provides an interface for working with files.
 * This class allows {@link BaseImplementor} and {@link Implementor} to generate code.
 *
 * @author Dmitriev Vladislav (bitree2004@yandex.ru)
 * @since 21
 */
/*package-private*/ class ImplFileAssistant {
    /**
     * char separator
     */
    private final static String DOT = ".";

    /**
     * File extension of source code.
     */
    private final static String EXTENSION_JAVA = "java";
    /**
     * File extension of compiled code.
     */
    /*package-private*/ final static String EXTENSION_CLASS = "class";

    /**
     * {@link FileVisitor<Path>} to delete the entire directory
     */
    /*package-private*/ static final SimpleFileVisitor<Path> DELETE = new SimpleFileVisitor<>() {
        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
        }
    };

    /**
     * Concatenate {@code text} and {@code reason} in special format {@code "%s! Cause: %s."}
     * for using in exception.
     *
     * @param text {@link String} that will be in first part
     * @param reason {@link String} that will be in second part
     * @return {@link String} text of exception.
     */
    /*package-private*/ static String exceptionMessageWithReason(final String text, final String reason) {
        return String.format("%s! Cause: %s.", text, reason);
    }

    /**
     * Returns the {@link Path} of {@link Class} by {@code token} and with {@code extension}.
     *
     * @param token {@link Class} that the full path will return
     * @param extension {@link String} extension of file
     * @return {@link Path} full class path
     */
     /*package-private*/ static Path getPath(final Class<?> token, final String extension) {
        return Path.of(token.getPackageName().replace(DOT, File.separator))
                .resolve(
                        String.format(
                                "%s%s%s",
                                ImplGeneratorCode.getImplName(token),
                                DOT,
                                extension
                        )
                );
    }

    /**
     * Found directory of full class path of {@code token} relative {@code root}.
     *
     * @param token {@link Class} that the full path will return
     * @param root {@link Path} relative to which the result will be found
     * @return {@link Path} full class path
     */
    /*package-private*/ static Path getPath(final Class<?> token, final Path root) {
        return root.resolve(getPath(token, EXTENSION_JAVA));
    }

    /**
     * Return parent path or if parent doesn't exist. If parent exist,
     * then function tried to create parent. If it's impossible, warning
     * will be written in {@code STDERR}.
     *
     * @param pathCode {@link Path} of file
     * @return parent path
     */
    /*package-private*/ static Path tryCreateDirectory(final Path pathCode) {
        final var dir = pathCode.getParent();
        if (dir != null) {
            try {
                Files.createDirectories(dir);
            } catch (final IOException e) {
                System.err.println(
                        exceptionMessageWithReason(
                                String.format("Implementor warning: can't create dir %s for code", dir),
                                e.getMessage()
                        )
                );
            }
        }
        return dir;
    }

    /**
     * It's a function {@link ImplFileAssistant#getPath(Class, Path)}. Wraps the exception
     * {@link InvalidPathException} to exception {@link ImplerException}.
     * After function will try to create directory by {@link ImplFileAssistant#tryCreateDirectory(Path)}.
     *
     * @param token {@link Class} that the full path will return
     * @param root {@link Path} relative to which the result will be found
     * @return {@link Path} full class path
     * @throws ImplerException if {@link ImplFileAssistant#getPath(Class, Path)}
     * threw {@link InvalidPathException}
     */
    /*package-private*/ static Path getCodePath(final Class<?> token, final Path root) throws ImplerException {
        // :NOTE: refactor | maybe fixed
        try {
            final Path pathCode = getPath(token, root);
            tryCreateDirectory(pathCode);
            return pathCode;
        } catch (final InvalidPathException e) {
            throw new ImplerException(
                    exceptionMessageWithReason("Error: invalid path was created", e.getMessage()),
                    e
            );
        }
    }

    /**
     * It's a function {@link Path#of(String, String...)}. Wraps the exception
     * {@link InvalidPathException} in exception {@link ImplerException}.
     *
     * @see Path#of(String, String...)
     *
     * @param pathName the path string
     * @return the path of pathName
     * @throws ImplerException if {@link Path#of(String, String...)} threw
     * {@link InvalidPathException}
     */
    /*package-private*/ static Path getPath(final String pathName) throws ImplerException {
        try {
            return Path.of(pathName);
        } catch (final InvalidPathException e) {
            throw new ImplerException(
                    exceptionMessageWithReason(
                            String.format(
                                    "Path %s was invalid!",
                                    pathName
                            ),
                            e.getMessage()
                    ),
                    e
            );
        }
    }

    /**
     * Returns class path of {@code token}.
     *
     * @param token {@link Path}
     * @return path of {@code token}.
     * @throws ImplerException if it couldn't convert to URO
     */
    /*package-private*/ static Path getClassPath(final Class<?> token) throws ImplerException {
        try {
            return Path.of(token.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (final URISyntaxException e) {
            throw new ImplerException(
                    exceptionMessageWithReason("Couldn't get location path!", e.getMessage()),
                    e
            );
        }
    }

    /**
     * Creates a temp directory with {@code root} and prefix {@code "tmp_"}.
     * Is a wrapper over a function {@link Files#createTempDirectory(String, FileAttribute[])}.
     * Wraps the exception {@link IOException} to exception {@link ImplerException}.
     *
     * @param root {@link Path} of created directory without prefix.
     * @return {@link Path} of temp directory that was created
     * @throws ImplerException if {@link Files#createTempDirectory(String, FileAttribute[])}
     * threw exception.
     */
    /*package-private*/ static Path createTmpDirectory(final Path root) throws ImplerException {
        try {
            return Files.createTempDirectory(root, "tmp_");
        } catch (final IOException e) {
            throw new ImplerException(
                    exceptionMessageWithReason(
                            String.format("Couldn't create temporary directory with name %s_tmp", root),
                            e.getMessage()
                    ),
                    e
            );
        }
    }

    /**
     * Translate {@code s} to unicode format.
     *
     * @param s {@link String} for translate
     * @return translated string
     */
    /*package-private*/ static String toFormat(final String s) {
        final StringBuilder res = new StringBuilder();
        for (final char c : s.toCharArray()) {
            res.append(c < 128 ? c : String.format("\\u%04X", (int) c));
        }
        return res.toString();
    }
}
