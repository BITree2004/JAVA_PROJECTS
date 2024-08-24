package info.kgeorgiy.ja.dmitriev.i18n.builder;

import info.kgeorgiy.ja.dmitriev.i18n.statistic.CurrencyStatistic;

import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * The class allows you to receive {@link CurrencyStatistic}.
 *
 * @author Dmitriev Vladislav
 * @since 21
 */
public class CurrencyBuilder extends AbstractBuilder<CurrencyStatistic> {
    // :NOTE: cache | fixed
    private final NumberFormat numberFormat;
    /**
     * Creates a class for parsing money.
     *
     * @param inputLocale the locale of input file
     * @param outputLocale the locale of output file
     */
    public CurrencyBuilder(final Locale inputLocale, final Locale outputLocale) {
        super(inputLocale, outputLocale);
        numberFormat = NumberFormat.getCurrencyInstance(inputLocale);
    }
    @Override
    public CurrencyStatistic getStatistic(final String input) {
        return new CurrencyStatistic(
                getValues(
                        input,
                        (position) -> parse(input, position)
                ),
                this
        );
    }

    /**
     * Try to parse money in the position {@code ParsePosition} at string {@code input}.
     *
     * @param input {@link String} that was parsed.
     * @param parsePosition {@link ParsePosition} where parse start.
     * @return The {@link Number} of money that was parsed.
     */
    public Number parse(
            final String input,
            final ParsePosition parsePosition
    ) {
        return numberFormat.parse(input, parsePosition);
    }

    @Override
    public MessageFormat getMessageFormat(final ResourceBundle resourceBundle) {
        return new MessageFormat(
                resourceBundle.getString("formatMoney"),
                inputLocale
        );
    }
}
