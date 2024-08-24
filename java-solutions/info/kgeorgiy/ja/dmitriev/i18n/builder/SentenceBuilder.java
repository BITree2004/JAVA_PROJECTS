package info.kgeorgiy.ja.dmitriev.i18n.builder;

import info.kgeorgiy.ja.dmitriev.i18n.statistic.SentenceStatistic;

import java.text.BreakIterator;
import java.util.Locale;
import java.util.function.Function;

/**
 * The class allows you to receive {@link SentenceStatistic}.
 *
 * @author Dmitriev Vladislav
 * @since 21
 */
public class SentenceBuilder extends StringBuilder<SentenceStatistic> {
    /**
     * Creates a class for parsing sentence.
     *
     * @param inputLocale the locale of input file
     * @param outputLocale the locale of output file
     */
    public SentenceBuilder(
            final Locale inputLocale,
            final Locale outputLocale
    ) {
        super(
                inputLocale,
                outputLocale,
                (x) -> true,
                BreakIterator.getSentenceInstance(inputLocale)
        );
    }

    @Override
    public SentenceStatistic getStatistic(final String input) {
        return new SentenceStatistic(
                inputLocale,
                getCommonStatistic(
                        input,
                        Function.identity()
                ),
                this
        );
    }
}
