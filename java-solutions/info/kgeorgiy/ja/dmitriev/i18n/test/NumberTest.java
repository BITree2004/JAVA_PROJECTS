package info.kgeorgiy.ja.dmitriev.i18n.test;

import info.kgeorgiy.ja.dmitriev.i18n.builder.AbstractBuilder;
import info.kgeorgiy.ja.dmitriev.i18n.builder.DateBuilder;
import info.kgeorgiy.ja.dmitriev.i18n.builder.CurrencyBuilder;
import info.kgeorgiy.ja.dmitriev.i18n.builder.NumberBuilder;
import info.kgeorgiy.ja.dmitriev.i18n.statistic.NumberStatistic;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.Function;

public class NumberTest extends DateTest {
    private static final BiFunction<Locale, Number, NumberStatistic> MONEY_SUPPLIER =
        getSupplier(
                (locale, number) -> NumberFormat.getInstance(locale).format(number),
                (locale) -> new NumberBuilder(
                        locale,
                        STANDARD_OUTPUT_LOCALE,
                        new CurrencyBuilder(locale, STANDARD_OUTPUT_LOCALE),
                        new DateBuilder(locale, STANDARD_OUTPUT_LOCALE)
                )
                );
    private static void checkNumber(
            final NumberStatistic numberStatistic,
            final int expectedValue,
            final double expectedAverage
    ) {
        Assertions.assertEquals(expectedValue, numberStatistic.getMinEntry().intValue());
        Assertions.assertEquals(expectedAverage, numberStatistic.getAverageValue().doubleValue(), 1e-2);
    }

    protected static void checkNumberStatic(
            final BiFunction<Locale, Number, NumberStatistic> numberStatisticFunction,
            final Number number
    ) {
        LOCALES.forEach(x -> checkNumber(numberStatisticFunction.apply(x, number), number.intValue(), number.doubleValue()));
    }

    protected static BiFunction<Locale, Number, NumberStatistic> getSupplier(
            final BiFunction<Locale, Number, String> numberStringFunction,
            final Function<Locale, AbstractBuilder<? extends NumberStatistic>> getAbstractBuilder
    ) {
        return (locale, number) ->
                getAbstractBuilder.apply(locale).getStatistic(numberStringFunction.apply(locale, number));
    }

    @Test
    public void test5_numberTest() {
        checkNumberStatic(MONEY_SUPPLIER, 777);
    }

    @Test
    public void test6_differentNumberTest() {
        NUMBERS_LIST.forEach(x->checkNumberStatic(MONEY_SUPPLIER, x));
    }
}
