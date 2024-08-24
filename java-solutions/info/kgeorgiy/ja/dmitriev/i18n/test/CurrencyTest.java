package info.kgeorgiy.ja.dmitriev.i18n.test;

import info.kgeorgiy.ja.dmitriev.i18n.builder.CurrencyBuilder;
import info.kgeorgiy.ja.dmitriev.i18n.statistic.NumberStatistic;
import org.junit.jupiter.api.Test;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.function.BiFunction;

public class CurrencyTest extends NumberTest {
    private static final BiFunction<Locale, Number, NumberStatistic> SUPPLIER =
            getSupplier((locale, number) ->
                                NumberFormat.getCurrencyInstance(locale).format(number),
                        (locale) -> new CurrencyBuilder(locale, STANDARD_OUTPUT_LOCALE)
            );
    @Test
    public void test7_currencyTest() {
        checkNumberStatic(SUPPLIER, 2000);
    }
    @Test
    public void test8_differentCurrencyTest() {
        NUMBERS_LIST.forEach(x -> checkNumberStatic(SUPPLIER, x));
    }
}
