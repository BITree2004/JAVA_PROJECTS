package info.kgeorgiy.ja.dmitriev.i18n.statistic;

import info.kgeorgiy.ja.dmitriev.i18n.builder.CurrencyBuilder;

import java.util.List;

/**
 * Instance class {@link AbstractStatistic<Number>} for storing money.
 *
 * @author Dmitriev Vladislav
 * @since 21
 */
public class CurrencyStatistic extends NumberStatistic {
    public CurrencyStatistic(
            final List<Number> values,
            final CurrencyBuilder builder
    ) {
        super(values, builder);
    }

    @Override
    public String getNameSection() {
        return "Money";
    }
}
