package info.kgeorgiy.ja.dmitriev.i18n.statistic;

import info.kgeorgiy.ja.dmitriev.i18n.builder.SentenceBuilder;

import java.util.List;
import java.util.Locale;

public class SentenceStatistic extends StringStatistic {
    public SentenceStatistic(
            final Locale locale,
            final List<String> values,
            final SentenceBuilder builder
    ) {
        super(locale, values, builder);
    }

    @Override
    public String getNameSection() {
        return "Sentence";
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
