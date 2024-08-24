package info.kgeorgiy.ja.dmitriev.i18n.builder;

import info.kgeorgiy.ja.dmitriev.i18n.statistic.NumberStatistic;

import java.text.BreakIterator;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * The class allows you to receive {@link NumberStatistic}.
 *
 * @author Dmitriev Vladislav
 * @since 21
 */
public class NumberBuilder extends AbstractBuilder<NumberStatistic> {
    private final CurrencyBuilder moneyBuilder;
    private final DateBuilder dateBuilder;

    /**
     * Creates a class for parsing number.
     *
     * @param inputLocale  the locale of input file
     * @param outputLocale the locale of output file
     */
    public NumberBuilder(
            final Locale inputLocale,
            final Locale outputLocale,
            final CurrencyBuilder moneyBuilder,
            final DateBuilder dateBuilder
    ) {
        super(inputLocale, outputLocale);
        this.moneyBuilder = moneyBuilder;
        this.dateBuilder = dateBuilder;
    }

    @Override
    public NumberStatistic getStatistic(final String input) {
        final var iterator = BreakIterator.getWordInstance(inputLocale);
        iterator.setText(input);
        final var values = new ArrayList<Number>();
        final var numberFormat = NumberFormat.getInstance(inputLocale);
        var ignorePoint = 0;
        for (int start = iterator.first(), end = iterator.next();
             end != BreakIterator.DONE;
             start = end, end = iterator.next()
        ) {
            if (start < ignorePoint) {
                continue;
            }
            var parsePosition = new ParsePosition(start);
            if (moneyBuilder.parse(input, parsePosition) != null
                    || dateBuilder.parse(input, parsePosition) != null) {
                ignorePoint = parsePosition.getIndex();
                continue;
            }
            parsePosition = new ParsePosition(start);
            final var value = numberFormat.parse(input, parsePosition);
            if (value != null) {
                values.add(value);
                ignorePoint = parsePosition.getIndex();
            }
        }
        return new NumberStatistic(values, this);
    }

    @Override
    public MessageFormat getMessageFormat(
            final ResourceBundle resourceBundle
    ) {
        return new MessageFormat(
                resourceBundle.getString("formatNumber"),
                outputLocale);
    }
}
