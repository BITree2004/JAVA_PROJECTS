package info.kgeorgiy.ja.dmitriev.i18n.builder;

import info.kgeorgiy.ja.dmitriev.i18n.statistic.DateStatistic;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParsePosition;
import java.util.*;

/**
 * The class allows you to receive {@link DateStatistic}.
 *
 * @author Dmitriev Vladislav
 * @since 21
 */
public class DateBuilder extends AbstractBuilder<DateStatistic> {
    private static final int[] FORMATS = {
            DateFormat.FULL, DateFormat.LONG, DateFormat.MEDIUM, DateFormat.SHORT
    };
    private final List<DateFormat> DATE_FORMATS = new ArrayList<>();

    /**
     * Creates a class for parsing date.
     *
     * @param inputLocale the locale of input file
     * @param outputLocale the locale of output file
     */
    public DateBuilder(
            final Locale inputLocale,
            final Locale outputLocale
    ) {
        super(inputLocale, outputLocale);
        Arrays.stream(FORMATS)
                .mapToObj(format -> DateFormat.getDateInstance(format, this.inputLocale))
                .forEach(DATE_FORMATS::add);
    }

    @Override
    public DateStatistic getStatistic(final String input) {
        return new DateStatistic(
                getValues(
                        input,
                        (position) -> parse(input, position)
                ),
                this
        );
    }

    /**
     * Try to parse date in the position {@code ParsePosition} at string {@code input}.
     *
     * @param input         {@link String} that was parsed.
     * @param parsePosition {@link ParsePosition} where parse start.
     * @return The {@link Number} of date that was parsed.
     */
    public Date parse(
            final String input,
            final ParsePosition parsePosition
    ) {
        for (final var dateFormat : DATE_FORMATS) {
            final var date = dateFormat.parse(input, parsePosition);
            if (date != null) {
                return date;
            }
        }
        return null;
    }

    @Override
    public MessageFormat getMessageFormat(
            final ResourceBundle resourceBundle
    ) {
        return new MessageFormat(
                resourceBundle.getString("formatDate"),
                outputLocale);
    }
}
