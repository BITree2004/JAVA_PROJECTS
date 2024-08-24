package info.kgeorgiy.ja.dmitriev.i18n;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
/*package-private*/ public class Utils {
    /*package-private*/ static Charset CHARSET = StandardCharsets.UTF_8;

    /*package-private*/
    static void logError(final String text, final Exception e) {
        logError(String.format("%s! Reason:%s", text, e.getMessage()));
    }

    /*package-private*/
    static void logError(final String text) {
        System.err.printf("%s!%n", text);
    }

    /*package-private*/
    static String read(final String input) throws IOException {
        return Files.readString(Path.of(input));
    }

    /*package-private*/
    static void write(
            final String output,
            final String text
    ) throws IOException {
        final var outputPath = Path.of(output);
        final var folder = outputPath.getParent();
        if (folder != null) {
            try {
                Files.createDirectories(folder);
            } catch (final IOException e) {
                logError("Couldn't create output folder", e);
            }
        }
        Files.writeString(outputPath, text, CHARSET);
    }
}
