package info.kgeorgiy.ja.dmitriev.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;

import static info.kgeorgiy.ja.dmitriev.implementor.ImplFileAssistant.*;

/**
 * The class allows you to generate valid code using the token class.
 * Class implementing {@link Impler}. The class also contains a console interface.
 *
 * @see Impler#implement(Class, Path)
 *
 * @author Dmitriev Vladislav (bitree2004@yandex.ru)
 * @since 21
 */
@SuppressWarnings("unused")
public class BaseImplementor implements Impler {
    /**
     * This function throw {@link ImplerException}.
     * Message in special format: "Error Implementor: unsupported %s !".
     *
     * @param name the name of type token that's unsupported
     * @throws ImplerException always throw
     */
    private static void throwUnsupported(final String name) throws ImplerException {
        throw new ImplerException(String.format("Error Implementor: unsupported %s !", name));
    }

    /**
     * Check, that {@code token} is supported. If it's not true,
     * then will be throw {@link ImplerException}.
     *
     * @param token {@link Class} that will be checking
     * @throws ImplerException if {@code token} is unsupported
     */
    private static void checkToken(final Class<?> token) throws ImplerException {
        final var modifiers = token.getModifiers();
        if (Modifier.isFinal(modifiers)) {
            throwUnsupported("final");
        }
        if (Modifier.isPrivate(modifiers)) {
            throwUnsupported("private");
        }
        if (token == Enum.class) {
            throwUnsupported("Enum.class");
        }
        if (token == Record.class) {
            throwUnsupported("Record.class");
        }
        if (token.isPrimitive()) {
            throwUnsupported("primitive");
        }
        if (token.isArray()) {
            throwUnsupported("array");
        }
    }

    @Override
    public void implement(final Class<?> token, final Path root) throws ImplerException {
        if (token == null || root == null) {
            throw new ImplerException("Argument's null!");
        }
        implImplement(token, root);
    }


    /**
     * Produces code implementing class or interface specified by provided {@code token}.
     * It's implementation of {@link Impler#implement(Class, Path)}
     * without checking arguments for null.
     *
     * @param token type token that will be implementation
     * @param root directory that will contain the class
     * @throws ImplerException couldn't implement
     * @throws ImplerException couldn't write, i.e. the {@link IOException} occurred
     */
    protected static void implImplement(
            final Class<?> token,
            final Path root
    ) throws ImplerException {
        checkToken(token);
        final var constructor = ImplGeneratorCode.getConstructor(token);
        final var methods = ImplGeneratorCode.getAllAbstractMethods(token);
        try (final var writer = Files.newBufferedWriter(getCodePath(token, root))) {
            new ImplGeneratorCode<>(writer::write).writeCodeSource(token, constructor, methods);
        } catch (final IOException e) {
            throw new ImplerException(
                    exceptionMessageWithReason("Error: writer fail", e.getMessage()),
                    e
            );
        }
    }

    /**
     * It's a function {@link Class#forName(String)}. Wraps the exception
     * {@link ClassNotFoundException} in exception {@link ImplerException}.
     *
     * @see Class#forName(String)
     *
     * @param className the binary name of class
     * @return the {@link Class} object of {@code classname}
     * @throws ImplerException if {@link Class#forName(String)} threw {@link ClassNotFoundException}
     */
    protected static Class<?> getClassForName(final String className) throws ImplerException {
        try {
            return Class.forName(className);
        } catch (final ClassNotFoundException e) {
            // :NOTE: bad messages | maybe fixed
            throw new ImplerException(
                    exceptionMessageWithReason(
                            String.format("Class %s wasn't found! Cause", className),
                            e.getMessage()
                    ),
                    e
            );
        }
    }

    /**
     * This function is a console interface of {@link BaseImplementor#implement(Class, Path)}.
     * <p>
     *     Required argument is {@code className}. Usage: {@code className}.
     *     All arguments must be non-null. If errors and warnings occur,
     *     they will be write in {@code STDERR }.
     *     Errors inherited from {@link BaseImplementor#implement(Class, Path)}.
     * </p>
     *
     * @param args array of argument
     */
    public static void main(final String[] args) {
        try {
            if (args == null || args.length != 1 || args[0] == null) {
                throw new ImplerException("Expected arguments: <classname>!");
            }
            // :NOTE: "."? | fixed
            implImplement(getClassForName(args[0]), getPath("."));
        } catch (final ImplerException e) {
            System.err.println(e.getMessage());
        }
    }
}
