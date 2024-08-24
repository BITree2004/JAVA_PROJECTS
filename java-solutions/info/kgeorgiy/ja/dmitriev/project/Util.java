package info.kgeorgiy.ja.dmitriev.project;

import java.io.PrintStream;

public class Util {
    /*package-private*/ static void logError(
            final String message,
            final Exception exception,
            final PrintStream errorStream
    ) {
        errorStream.printf("%s, because: %s.", message, exception.getMessage());
    }

    /*package-private*/ static void logError(
            final String message,
            final PrintStream errorStream
    ) {
        errorStream.printf("%s.", message);
    }
}
