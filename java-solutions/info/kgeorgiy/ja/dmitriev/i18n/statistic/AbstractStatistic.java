package info.kgeorgiy.ja.dmitriev.i18n.statistic;

import info.kgeorgiy.ja.dmitriev.i18n.builder.AbstractBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

/**
 * A class that allows you to store received values of type {@link T}.
 *
 * @param <T> the type of values
 * @author Dmitriev Vladislav
 * @since 21
 */
public abstract class AbstractStatistic<T> {
    // :NOTE: why not protected? | fixed
    /**
     * number of all objects.
     */
    protected int countAll;
    /**
     * number of unique objects.
     */
    protected int countUnique;
    /**
     * minimal object by comparator.
     */
    protected T minEntry;
    /**
     * maximal object by comparator.
     */
    protected T maxEntry;
    /**
     * minimum length among all objects.
     */
    protected int minLength;
    /**
     * maximum length among all objects.
     */
    protected int maxLength;
    /**
     * minimum length object.
     */
    protected T minLengthEntry;
    /**
     * maximum length object.
     */
    protected T maxLengthEntry;
    /**
     * Middle element. There is one for each class.
     */
    protected T average;
    /**
     * Average length.
     */
    protected double averageLength;

    private final AbstractBuilder<?> builder;

    /**
     * Creates an instance of a class by collecting statistics of all already given comparisons.
     *
     * @param getSize    Get the length of the value.
     * @param comparator Comparator for comparing two values.
     * @param values     List of received values.
     */
    protected AbstractStatistic(
            final Function<T, Integer> getSize,
            final Comparator<T> comparator,
            final List<T> values,
            final AbstractBuilder<?> builder
    ) {
        this.builder = builder;
        if (values.isEmpty()) {
            return;
        }

        values.sort(comparator);
        minEntry = values.getFirst();
        maxEntry = values.getLast();
        int cntUnique = 1;
        for (int i = 1; i < values.size(); ++i) {
            if (comparator.compare(values.get(i - 1), values.get(i)) != 0) {
                ++cntUnique;
            }
        }
        countUnique = cntUnique;

        values.sort(Comparator.comparing(getSize));
        minLengthEntry = values.getFirst();
        maxLengthEntry = values.getLast();
        minLength = getSize.apply(minLengthEntry);
        maxLength = getSize.apply(maxLengthEntry);

        averageLength =
                (double) values.stream().map(getSize).reduce(0, Integer::sum) / values.size();
        countAll = values.size();
    }

    /**
     * Returns the number of objects.
     *
     * @return The number of objects.
     */
    public int getCountAll() {
        return countAll;
    }

    /**
     * Returns the number of unique objects.
     *
     * @return Number of unique ones.
     */
    public int getCountUnique() {
        return countUnique;
    }

    /**
     * Returns the minimum object.
     *
     * @return The minimum object.
     */
    public T getMinEntry() {
        return minEntry;
    }

    /**
     * Returns the maximum object.
     *
     * @return The maximum object.
     */
    public T getMaxEntry() {
        return maxEntry;
    }

    /**
     * Returns the minimum length among all objects.
     *
     * @return The minimum length among all objects.
     */
    public int getMinLength() {
        return minLength;
    }

    /**
     * Returns the maximum length among all objects.
     *
     * @return The maximum length among all objects.
     */
    public int getMaxLength() {
        return maxLength;
    }

    /**
     * Returns an object with the minimum length.
     *
     * @return Object with the minimum length.
     */
    public T getMinLengthEntry() {
        return minLengthEntry;
    }

    /**
     * Returns an object with the maximum length.
     *
     * @return Object with the maximum length.
     */
    public T getMaxLengthEntry() {
        return maxLengthEntry;
    }

    /**
     * Returns the average value among all objects.
     *
     * @return The average value among all objects.
     */
    public T getAverageValue() {
        return average;
    }

    /**
     * Returns the average length among all objects.
     *
     * @return The average length among all objects.
     */
    public double getAverageLength() {
        return averageLength;
    }

    /**
     * Return builder who created this statistic.
     *
     * @return {@link java.util.Locale.Builder} who created class
     */
    public AbstractBuilder<?> getBuilder() {
        return builder;
    }

    /**
     * Returns the average value worth printing that makes sense for a given object type.
     *
     * @param resourceBundle {@link ResourceBundle} where get format.
     * @param format         {@link MessageFormat} of result.
     * @return {@link String} that will use in report.
     */
    public abstract String getAverage(
            final ResourceBundle resourceBundle,
            final MessageFormat format
    );

    /**
     * Return to the name of the average that will be used for this type.
     *
     * @return The name of the average that will be used for this type.
     */
    public abstract String getAverageName();

    /**
     * Filters out nulls for output.
     *
     * @param resourceBundle {@link ResourceBundle} for get format of absent object.
     * @param object         {@link Object} that will be printed.
     * @param messageFormat  {@link MessageFormat} of output.
     * @return {@link Object} that will be print.
     */
    public static Object filter(
            final ResourceBundle resourceBundle,
            final Object object,
            final MessageFormat messageFormat
    ) {
        return object == null ?
                resourceBundle.getString("absent") :
                messageFormat.format(new Object[]{object});
    }

    /**
     * Returns the name of the section where the statistics should be located.
     *
     * @return Section name
     */
    public abstract String getNameSection();

    protected BigDecimal calcAverage(
            final List<T> values,
            final ToDoubleFunction<T> toDoubleFunction
    ) {
        return calcSumOfAll(values, toDoubleFunction)
                .divide(BigDecimal.valueOf(countAll), RoundingMode.HALF_EVEN);
    }

    private BigDecimal calcSumOfAll(
            final List<T> values,
            final ToDoubleFunction<T> toDoubleFunction
    ) {
        return values.stream()
                .map(x -> BigDecimal.valueOf(toDoubleFunction.applyAsDouble(x)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
