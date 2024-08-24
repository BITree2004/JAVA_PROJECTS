package info.kgeorgiy.ja.dmitriev.i18n.statistic;

import info.kgeorgiy.ja.dmitriev.i18n.builder.AbstractBuilder;

import java.text.MessageFormat;
import java.util.*;

/**
 * Instance class {@link AbstractStatistic<Number>} for storing numbers.
 *
 * @author Dmitriev Vladislav
 * @since 21
 */
public class NumberStatistic extends AbstractStatistic<Number> {
    public NumberStatistic(
            final List<Number> values,
            final AbstractBuilder<?> builder
    ) {
        super(
                x -> x.toString().length(),
                Comparator.comparing(Number::doubleValue),
                values,
                builder
        );
        if (!values.isEmpty()) {
            // :NOTE: reorganize | fixed
            super.average = calcAverage(values, Number::doubleValue);
        }
    }

    @Override
    public String getNameSection() {
        return "Number";
    }

    @Override
    public String getAverage(
            final ResourceBundle resourceBundle,
            final MessageFormat format
    ) {
        return filter(resourceBundle, getAverageValue(), format).toString();
    }

    @Override
    public String getAverageName() {
        return "average" + getNameSection();
    }
}
