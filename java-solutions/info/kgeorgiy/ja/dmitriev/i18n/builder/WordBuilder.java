package info.kgeorgiy.ja.dmitriev.i18n.builder;

import info.kgeorgiy.ja.dmitriev.i18n.statistic.WordStatistic;

import java.text.BreakIterator;
import java.util.Locale;

/**
 * The class allows you to receive {@link WordStatistic}.
 *
 * @author Dmitriev Vladislav
 * @since 21
 */
public class WordBuilder extends StringBuilder<WordStatistic> {
    /**
     * Creates a class for parsing word.
     *
     * @param inputLocale the locale of input file
     * @param outputLocale the locale of output file
     */
    public WordBuilder(
            final Locale inputLocale,
            final Locale outputLocale
    ) {
        super(
                inputLocale,
                outputLocale,
                (x) -> !x.isEmpty() && Character.isLetter(x.charAt(0)),
                BreakIterator.getWordInstance(inputLocale)
        );
    }

    @Override
    public WordStatistic getStatistic(final String input) {
        return new WordStatistic(
                inputLocale,
                getCommonStatistic(
                        input,
                        word -> word.toLowerCase(inputLocale)
                ),
                this
        );
    }
}
