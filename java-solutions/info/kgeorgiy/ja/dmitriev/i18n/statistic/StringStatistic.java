package info.kgeorgiy.ja.dmitriev.i18n.statistic;

import info.kgeorgiy.ja.dmitriev.i18n.builder.StringBuilder;

import java.text.*;
import java.util.*;

/**
 * Instance class {@link AbstractStatistic<String>} for storing string.
 *
 * @author Dmitriev Vladislav
 * @since 21
 */
public abstract class StringStatistic extends AbstractStatistic<String> {
    public StringStatistic(
            final Locale locale,
            final List<String> values,
            final StringBuilder<?> builder
    ) {
        super(
                String::length,
                (x, y) -> Collator.getInstance(locale).compare(x, y),
                values,
                builder
        );
    }


    @Override
    public String getAverage(
            final ResourceBundle resourceBundle,
            final MessageFormat format
    ) {
        return format.format(new Object[]{getAverageLength()});
    }
}
