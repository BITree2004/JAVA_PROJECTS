package info.kgeorgiy.ja.dmitriev.bank.src;

import java.util.Arrays;
import java.util.Objects;

/**
 * Utility class for package and sub-pockets. Gives access to useful functions.
 *
 * @author Dmitriev Vladislav (bitree2004@yandex.ru)
 * @since 21
 */
public class Utils {
    /**
     * Checks that {@code args} and all its elements are not null.
     * Otherwise, it throws an {@link NullPointerException}.
     *
     * @param args array of strings
     */
    public static void checkForNull(final String... args) {
        Objects.requireNonNull(args);
        Arrays.stream(args).forEach(Objects::requireNonNull);
    }

    /**
     * Tries to parse a {@code arg} into a {@link Integer}.
     * If it does not work, it writes to stderr and returns null.
     *
     * @param arg parsing argument.
     * @param name The name of the argument for logging the error.
     * @return number obtained from the {@code arg}.
     */
    public static Integer parseArg(final String arg, final String name) {
        try {
            return Integer.parseInt(arg);
        } catch (final NumberFormatException e) {
            System.err.printf("%s must be integer!%n", name);
            return null;
        }
    }
}
