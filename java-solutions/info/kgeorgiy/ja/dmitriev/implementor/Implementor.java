package info.kgeorgiy.ja.dmitriev.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import static info.kgeorgiy.ja.dmitriev.implementor.ImplFileAssistant.*;

/**
 * The class allows you to generate valid code using the token class
 * and packing in jar.
 * Class implementing {@link JarImpler}. The class also contains a console interface.
 * And also extends {@link BaseImplementor}.
 *
 * @see JarImpler#implementJar(Class, Path)
 * @see Impler#implement(Class, Path)
 *
 * @author Dmitriev Vladislav (bitree2004@yandex.ru)
 * @since 21
 */
public class Implementor extends BaseImplementor implements JarImpler {

    /**
     * The format of console input. It expected, when use console interface.
     */
    private static final String FORMAT =
            "Expected arguments: <class-name> or -jar <class-name> <file.java>!";

    /**
     * Compile {@code token} in {@code root} from code source in same directory.
     *
     * @param token {@link Class} that compile
     * @param root {@link Path} of directory
     * @throws ImplerException compilation failed
     */
    private static void compile(final Class<?> token, final Path root) throws ImplerException {
        final var compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new ImplerException("Didn't find compiler!");
        }
        final var pathOfClass = getClassPath(token);
        if (compiler.run(
                null,
                null,
                null,
                getPath(token, root).toString(),
                "-cp",
                pathOfClass.toString(),
                "-encoding",
                StandardCharsets.UTF_8.name()
        ) != 0) {
            throw new ImplerException("Compilation failed!");
        }
    }

    /**
     * Create jar file with {@link Path} {@code toFile} from {@code token}
     * relative to {@code fromDir}.
     *
     * @param token {@link Class} from which the jar is generated
     * @param fromDir {@link Path} relative to which the file is searched
     * @param toFile {@link Path} of jar file that will be created
     * @throws ImplerException if you couldn't make jar
     */
    private static void jar(
            final Class<?> token,
            final Path fromDir,
            final Path toFile
    ) throws ImplerException {
        final var manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        try (final var outputStream = new JarOutputStream(
                Files.newOutputStream(toFile),
                manifest
        )) {
            final var pathOfFile = getPath(token, EXTENSION_CLASS).toString().replace(File.separatorChar, '/');
            outputStream.putNextEntry(new ZipEntry(pathOfFile.toString()));
            Files.copy(fromDir.resolve(pathOfFile), outputStream);
        } catch (final IOException e) {
            throw new ImplerException(
                    exceptionMessageWithReason("Couldn't make jar", e.getMessage()),
                    e
            );
        }
    }

    /**
     * Produces code implementing class in jar with name {@code jarFile}
     * specified by provided {@code token}.
     * It's implementation of {@link Implementor#implementJar(Class, Path)}
     * without checking arguments for null.
     *
     * @param token {@link Class} that will be implementation
     * @param jarFile {@link Path} that will be generated
     * @throws ImplerException couldn't implement
     * @throws ImplerException couldn't write, i.e. the {@link IOException} occurred
     * @throws ImplerException couldn't found java compiler
     */
    private static void implImplementJar(
            final Class<?> token,
            final Path jarFile
    ) throws ImplerException {
        final Path dir = tryCreateDirectory(jarFile);
        final Path tmpDir = createTmpDirectory(dir);
        try {
            try {
                implImplement(token, tmpDir);
                compile(token, tmpDir);
                jar(token, tmpDir, jarFile);
            } finally {
                Files.walkFileTree(tmpDir, DELETE);
            }
        } catch (final IOException e) {
            throw new ImplerException(
                    exceptionMessageWithReason(
                            "Couldn't remove tmp directory",
                            e.getMessage()
                    ),
                    e
            );
        }
    }

    @Override
    public void implementJar(final Class<?> token, final Path jarFile) throws ImplerException {
        if (token == null || jarFile == null) {
            throw new ImplerException("Argument's null!");
        }
        implImplementJar(token, jarFile);
    }

    /**
     * This function is a console interface of {@link Implementor#implementJar(Class, Path)}.
     * <p>
     *     Required argument is {@code classname}. 
     *     Usage: {@code "<class-name> or -jar <class-name> <file.java>"}.
     *     If it's first format, then will be call {@link BaseImplementor#main(String[])}
     *     All arguments must be non-null. If errors and warnings occur,
     *     they will be write in {@code STDERR}
     *     Errors inherited from {@link BaseImplementor#implement(Class, Path)}.
     * </p>
     *
     * @param args array of argument
     */
    public static void main(final String[] args)  {
        try {
            if (args == null || Arrays.stream(args).anyMatch(Objects::isNull) ||
                    (args.length != 1 && args.length != 3)) {
                throw new ImplerException(FORMAT);
            }
            if (args.length == 1) {
                BaseImplementor.main(args);
            } else  {
                implImplementJar(getClassForName(args[1]), getPath(args[2]));
            }
        } catch (final ImplerException e) {
            System.err.println(e.getMessage());
        }
    }
}
