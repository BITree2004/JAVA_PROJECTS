package info.kgeorgiy.ja.dmitriev.i18n.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import info.kgeorgiy.ja.dmitriev.i18n.TextStatistics;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;

// :NOTE: "integration" test | fixed, see all
@TestMethodOrder(MethodOrderer.MethodName.class)
public class AbstractTest {
    private static final String SRC = "java-solutions/info/kgeorgiy/ja/dmitriev/i18n/test/src/";
    protected static final List<Integer> DATE_FORMATS =
            List.of(DateFormat.SHORT, DateFormat.MEDIUM, DateFormat.LONG, DateFormat.FULL);
    protected static final List<Number> NUMBERS_LIST = List.of(-1, 0, 1, Long.MAX_VALUE, Math.PI);
    protected static final List<Calendar> DATES = List.of(makeDate(2039, 9, 1), makeDate(2004, 6, 9), makeDate(1999, 9, 9));
    // :NOTE: arabic? | fixed
    protected static List<Locale> LOCALES = List.of(Locale.of("ar"), Locale.of("zh", "cn"), Locale.of("en", "us"), Locale.of("ru", "ru"));
    protected static Locale STANDARD_OUTPUT_LOCALE = Locale.ENGLISH;
    private void testIntegration(
            final String file,
            final String inputLocale,
            final String outputLocale
    ) throws IOException {
        final var input = Path.of(SRC + file + "_in.txt");
        final var output = Path.of(SRC + file + "_" + outputLocale + "_out.html");
        final var tmp = Path.of(SRC + file + "_" + outputLocale + "_tmp.html");
        TextStatistics.main(
                new String[]{inputLocale, outputLocale, input.toString(), tmp.toString()}
        );
        try {
            Assertions.assertEquals(Files.readString(output), Files.readString(tmp));
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    protected void IntegrationTest(final String file, final String locale) throws IOException {
        for (final var outputLocale : List.of("en-US", "ru-RU")) {
            testIntegration(file, locale, outputLocale);
        }
    }

    private static Calendar makeDate(final int year, final int month, final int day) {
        final var res = Calendar.getInstance();
        res.set(year, month, day, 0, 0, 0);
        res.set(Calendar.MILLISECOND, 0);
        return res;
    }
}
