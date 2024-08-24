package info.kgeorgiy.ja.dmitriev.i18n.builder;

import info.kgeorgiy.ja.dmitriev.i18n.statistic.AbstractStatistic;
import info.kgeorgiy.ja.dmitriev.i18n.statistic.DateStatistic;

import java.text.BreakIterator;
import java.text.MessageFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.Function;

/**
 * The class allows you to receive {@link AbstractStatistic}.
 *
 * @author Dmitriev Vladislav
 * @since 21
 */
public abstract class AbstractBuilder<T extends AbstractStatistic<?>> {
    protected final Locale inputLocale;
    protected final Locale outputLocale;

    /**
     * Creates a class for parsing.
     *
     * @param inputLocale  the locale of input file
     * @param outputLocale the locale of output file
     */
    protected AbstractBuilder(final Locale inputLocale, final Locale outputLocale) {
        this.inputLocale = inputLocale;
        this.outputLocale = outputLocale;
    }

    /**
     * Creates an instance of a {@link AbstractStatistic} using {@code input}.
     *
     * @param input {@link String} of input data for parsing.
     * @return {@link DateStatistic} that was parsed.
     */
    public abstract T getStatistic(
            final String input
    );

    /**
     * Extracts the resulting values from the {@code input} into a list, if possible.
     *
     * @param input                    The input that will be parsed.
     * @param parsePositionTBiFunction Function for parsing a token.
     * @param <E>                      The type we get.
     * @return A list of values that we were able to parse.
     */
    protected <E> List<E> getValues(
            final String input,
            final Function<ParsePosition, E> parsePositionTBiFunction
    ) {
        final var iterator = BreakIterator.getWordInstance(inputLocale);
        iterator.setText(input);
        final var values = new ArrayList<E>();
        var ignorePoint = 0;
        for (int start = iterator.first(), end = iterator.next();
             end != BreakIterator.DONE;
             start = end, end = iterator.next()
        ) {
            if (start < ignorePoint) {
                continue;
            }
            final var parsePosition = new ParsePosition(start);
            final var value = parsePositionTBiFunction.apply(parsePosition);
            if (value != null) {
                values.add(value);
                ignorePoint = parsePosition.getIndex();
            }
        }
        return values;
    }

    /**
     * Gets {@link MessageFormat} from {@link ResourceBundle}.
     *
     * @param resourceBundle {@link ResourceBundle} where does {@link MessageFormat} come from
     * @return {@link MessageFormat} for section
     */
    public abstract MessageFormat getMessageFormat(final ResourceBundle resourceBundle);


    /**
     * Gets {@link MessageFormat} of average from {@link ResourceBundle}.
     *
     * @param resourceBundle {@link ResourceBundle} where does {@link MessageFormat} come from
     * @return {@link MessageFormat} for section
     */
    public MessageFormat getAverageFormat(final ResourceBundle resourceBundle) {
        return getMessageFormat(resourceBundle);
    }
}
