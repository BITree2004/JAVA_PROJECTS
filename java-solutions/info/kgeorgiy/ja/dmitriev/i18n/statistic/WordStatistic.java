package info.kgeorgiy.ja.dmitriev.i18n.statistic;

import info.kgeorgiy.ja.dmitriev.i18n.builder.WordBuilder;

import java.util.List;
import java.util.Locale;

public class WordStatistic extends StringStatistic {
    public WordStatistic(
            final Locale locale,
            final List<String> values,
            final WordBuilder builder
    ) {
        super(locale, values, builder);
    }

    @Override
    public String getNameSection() {
        return "Word";
    }

    /**
     * Returns the name of the section where the statistics should be located.
     *
     * @return Section name
     */
    @Override
    public String getAverageName() {
        return "averageLength" + getNameSection();
    }
}
