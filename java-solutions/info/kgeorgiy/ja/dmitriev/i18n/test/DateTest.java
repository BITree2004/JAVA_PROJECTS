package info.kgeorgiy.ja.dmitriev.i18n.test;

import java.text.DateFormat;
import java.text.ParsePosition;
import java.util.Date;

import info.kgeorgiy.ja.dmitriev.i18n.builder.DateBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DateTest extends AbstractTest {
    protected void checkDate(final Date date, final int dateFormat) {
        LOCALES.forEach(x -> {
            final var input = DateFormat.getDateInstance(dateFormat, x).format(date);
            final var dateBuilder = new DateBuilder(x, STANDARD_OUTPUT_LOCALE);
            final var dateStatistic = dateBuilder.getStatistic(input);
            Assertions.assertNotNull(dateBuilder.parse(input, new ParsePosition(0)));
            Assertions.assertEquals(1, dateStatistic.getCountAll());
            Assertions.assertEquals(date.toString(), dateStatistic.getMinLengthEntry().toString());
            Assertions.assertEquals(date.toString(), dateStatistic.getMinEntry().toString());
        });
    }

    @Test
    public void test1_simpleDates() {
        checkDate(DATES.getFirst().getTime(), DATE_FORMATS.getFirst());
    }

    @Test
    public void test2_differentDates() {
        DATES.forEach(date -> checkDate(date.getTime(), DATE_FORMATS.getFirst()));
    }

    @Test
    public void test3_differentStyles() {
        DATES.forEach(date -> checkDate(date.getTime(), DATE_FORMATS.getFirst()));
    }

    @Test
    public void test4_differentStylesDates() {
        DATE_FORMATS.forEach(dateFormat -> DATES.forEach(date -> checkDate(date.getTime(), dateFormat)));
    }
}
