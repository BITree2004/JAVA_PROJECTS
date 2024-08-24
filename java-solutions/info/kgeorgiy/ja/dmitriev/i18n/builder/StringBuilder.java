package info.kgeorgiy.ja.dmitriev.i18n.builder;

import info.kgeorgiy.ja.dmitriev.i18n.statistic.StringStatistic;

import java.text.BreakIterator;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * The class allows you to receive {@link StringStatistic}.
 *
 * @author Dmitriev Vladislav
 * @since 21
 */
public abstract class StringBuilder<T extends StringStatistic> extends AbstractBuilder<T> {
    private final Predicate<String> stringPredicate;
    private final BreakIterator iterator;

    /**
     * Creates a class for parsing string.
     *
     * @param inputLocale     the locale of input file
     * @param outputLocale    the locale of output file
     * @param stringPredicate the predicate for test is valid token
     * @param iterator        the iterator of text
     */
    public StringBuilder(
            final Locale inputLocale,
            final Locale outputLocale,
            final Predicate<String> stringPredicate,
            final BreakIterator iterator
    ) {
        super(inputLocale, outputLocale);
        this.stringPredicate = stringPredicate;
        this.iterator = iterator;
    }

    protected List<String> getCommonStatistic(
            final String input,
            final Function<String, String> mapperToken
    ) {
        iterator.setText(input);
        final var values = new ArrayList<String>();
        for (int start = iterator.first(), end = iterator.next();
             end != BreakIterator.DONE;
             start = end, end = iterator.next()
        ) {
            final var value = input.substring(start, end).trim();
            if (stringPredicate.test(value) && !value.isEmpty()) {
                values.add(mapperToken.apply(value));
            }
        }
        return values;
    }

    @Override
    public MessageFormat getMessageFormat(final ResourceBundle resourceBundle) {
        return new MessageFormat(
                resourceBundle.getString("formatString"),
                inputLocale
        );
    }

    @Override
    public MessageFormat getAverageFormat(final ResourceBundle resourceBundle) {
        return new MessageFormat(
                resourceBundle.getString("formatNumber"),
                outputLocale
        );
    }
}
