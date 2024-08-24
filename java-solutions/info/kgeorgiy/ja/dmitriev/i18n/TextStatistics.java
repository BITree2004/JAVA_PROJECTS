package info.kgeorgiy.ja.dmitriev.i18n;

import info.kgeorgiy.ja.dmitriev.i18n.builder.*;
import info.kgeorgiy.ja.dmitriev.i18n.statistic.*;
import info.kgeorgiy.ja.dmitriev.i18n.report.*;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.util.*;

import static info.kgeorgiy.ja.dmitriev.i18n.Utils.*;

/**
 * The class provides a console interface for generating text statistics.*
 * {@see TextStatistics#main(final String[] args)}
 *
 * @author Dmitriev Vladislav
 * @since 21
 */
public class TextStatistics {
    private static final String USAGE = """
            <Locale> - the locale of input file
            <Locale> - the locale of output file
            <fileName> - the path of input file
            <fileName> - the path of output file
            """;

    /**
     * Generates a text report from file to file.
     * Use in format: {@code USAGE}.
     *
     * @param args console arguments
     */
    public static void main(final String[] args) {
        if (args == null
                || args.length != 4
                || Arrays.stream(args).anyMatch(Objects::isNull)
        ) {
            System.err.println(USAGE);
            return;
        }
        final String input;
        try {
            input = read(args[2]);
        } catch (final IOException | InvalidPathException e) {
            logError("Couldn't read file", e);
            return;
        }
        final var firstLocale = new Locale.Builder().setLanguageTag(args[0]).build();
        final var secondLocale = new Locale.Builder().setLanguageTag(args[1]).build();
        if (!secondLocale.getLanguage().equals("en")
                && !secondLocale.getLanguage().equals("ru")
        ) {
            logError("Unsupported output locale! Use english or russian!");
            return;
        }
        final String res;
        res = Report.generate(secondLocale, getStatics(
                firstLocale, secondLocale, input
        ));

        try {
            write(args[3], res);
        } catch (final IOException e) {
            logError("Error, while write report", e);
        }

    }

    private static List<AbstractStatistic<?>> getStatics(
            final Locale intputLocale,
            final Locale outputLocale,
            final String input
    ) {
        final var res = new ArrayList<AbstractStatistic<?>>();
        res.add(
                new SentenceBuilder(
                        intputLocale,
                        outputLocale
                ).getStatistic(input)
        );
        res.add(
                new WordBuilder(
                        intputLocale,
                        outputLocale
                ).getStatistic(input)
        );
        final var moneyBuilder = new CurrencyBuilder(intputLocale, outputLocale);
        final var dateBuilder = new DateBuilder(intputLocale, outputLocale);
        final var numberBuilder = new NumberBuilder(intputLocale, outputLocale, moneyBuilder, dateBuilder);
        res.add(numberBuilder.getStatistic(input));
        res.add(moneyBuilder.getStatistic(input));
        res.add(dateBuilder.getStatistic(input));
        return res;
    }
}
