package info.kgeorgiy.ja.dmitriev.i18n.statistic;

import info.kgeorgiy.ja.dmitriev.i18n.builder.DateBuilder;

import java.text.MessageFormat;
import java.util.*;

/**
 * Instance class {@link AbstractStatistic<Date>} for storing dates.
 *
 * @author Dmitriev Vladislav
 * @since 21
 */
// :NOTE: split statistics and calculation | fixed
public class DateStatistic extends AbstractStatistic<Date> {

    public DateStatistic(
            final List<Date> values,
            final DateBuilder builder
    ) {
        super(
                x -> x.toString().length(),
                Comparator.comparing(Date::getTime),
                values,
                builder
        );
        if (!values.isEmpty()) {
            super.average = new Date((long) calcAverage(values, Date::getTime).doubleValue());
        }
    }

    @Override
    public String getNameSection() {
        return "Date";
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
