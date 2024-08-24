package info.kgeorgiy.ja.dmitriev.i18n.report;

import info.kgeorgiy.ja.dmitriev.i18n.statistic.*;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

import static info.kgeorgiy.ja.dmitriev.i18n.report.Templates.*;
import static info.kgeorgiy.ja.dmitriev.i18n.statistic.AbstractStatistic.filter;

/**
 * The class allows you to generate a report in a special format from the received data.
 *
 * @author Dmitriev Vladislav
 * @since 21
 */
public class Report {
    /**
     * Generates a report from the received list {@code values}.
     * The order of report is exactly the same as in values.
     *
     * @param secondLocale {@link Locale} of output file
     * @param values {@link AbstractStatistic} that was parsed
     * @return the report that was received
     */
    public static String generate(
            final Locale secondLocale,
            final List<AbstractStatistic<?>> values
    ) {
        final var resourceBundle =
                ResourceBundle.getBundle(
                        "info.kgeorgiy.ja.dmitriev.i18n.src.output_title",
                        secondLocale
                );
        final var head = String.format(HEADER, resourceBundle.getString("title"));
        final var title = String.format(TITLE, resourceBundle.getString("file"));
        final var summary = generateSummary(
                secondLocale,
                values,
                resourceBundle
        );
        final var sections = generateSections(secondLocale, values, resourceBundle);
        return String.format(REPORT,
                             head,
                             title,
                             summary,
                             sections.get(0),
                             sections.get(1),
                             sections.get(2),
                             sections.get(3),
                             sections.get(4)
        );
    }

    private static String generateSummary(
            final Locale locale,
            final List<AbstractStatistic<?>> values,
            final ResourceBundle resourceBundle
    ) {
        final var args = new ArrayList<String>();
        final MessageFormat numberFormat = new MessageFormat(
                resourceBundle.getString("formatNumber"),
                locale
        );
        args.add(resourceBundle.getString("summaryStatics"));
        values.stream()
                .map((statistic) -> MessageFormat.format(
                             resourceBundle.getString("statisticFormat"),
                             resourceBundle.getString("sum" + statistic.getNameSection()),
                             numberFormat.format(new Object[]{statistic.getCountAll()})
                     )
                )
                .forEach(args::add);
        // :NOTE: String array? | fixed
        return String.format(SUMMARY, (Object[]) args.toArray(new String[0]));
    }

    private static List<String> generateSections(
            final Locale outputLocale,
            final List<AbstractStatistic<?>> values,
            final ResourceBundle resourceBundle
    ) {
        final var statisticFormat = resourceBundle.getString("statisticFormat");
        final var uniqueFormat = resourceBundle.getString("uniqueFormat");
        final var exampleFormat = resourceBundle.getString("formatWithExample");
        final var numberFormat = new MessageFormat(
                resourceBundle.getString("formatNumber"),
                outputLocale
        );
        return values.stream()
                .map(value -> {
                    final var name = value.getNameSection();
                    final var format = value.getBuilder().getMessageFormat(resourceBundle);
                    return String.format(
                            SECTION,
                            resourceBundle.getString("statistic" + name),
                            MessageFormat.format(
                                    uniqueFormat,
                                    resourceBundle.getString("sum" + name),
                                    value.getCountAll(),
                                    value.getCountUnique(),
                                    resourceBundle.getString("unique")
                            ),
                            MessageFormat.format(
                                    statisticFormat,
                                    resourceBundle.getString("min" + name),
                                    filter(resourceBundle,
                                           value.getMinEntry(),
                                           format)
                            ),
                            MessageFormat.format(
                                    statisticFormat,
                                    resourceBundle.getString("max" + name),
                                    filter(resourceBundle,
                                           value.getMaxEntry(),
                                           format)
                            ),
                            MessageFormat.format(
                                    exampleFormat,
                                    resourceBundle.getString("minLength" + name),
                                    numberFormat.format(new Object[]{value.getMinLength()}),
                                    filter(resourceBundle, value.getMinLengthEntry(), format)
                            ),
                            MessageFormat.format(
                                    exampleFormat,
                                    resourceBundle.getString("maxLength" + name),
                                    numberFormat.format(new Object[]{value.getMaxLength()}),
                                    filter(resourceBundle, value.getMaxLengthEntry(), format)
                            ),
                            MessageFormat.format(
                                    statisticFormat,
                                    resourceBundle.getString(value.getAverageName()),
                                    // :NOTE: instanceof | fixed
                                    value.getAverage(
                                            resourceBundle,
                                            value.getBuilder().getAverageFormat(resourceBundle)
                                    )
                            )
                    );
                }).collect(Collectors.toList());
    }
}
