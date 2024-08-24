package info.kgeorgiy.ja.dmitriev.iterative;

import java.util.AbstractList;
import java.util.List;

/**
 * Is a view of k elements.
 * That is, it stores only elements whose indices are divisible by {@code step}.
 * Class implementing {@link List}.
 *
 * @param <T> the type of elements in this list
 * @author Dmitriev Vladislav (bitree2004@yandex.ru)
 * @since 21
 */
/*package-private*/ class KthList<T> extends AbstractList<T> {
    private final List<? extends T> data;
    private final int step;

    /**
     * Constructs a list containing the elements of the {@code data}.
     * With special index that divide by {@code step}.
     *
     * @param data {@link List} that's view of which the class is
     * @param step What should the indexes be divided into?
     */
    public KthList(final List<? extends T> data, final int step) {
        this.data = data;
        this.step = step;
    }

    @Override
    public T get(final int index) {
        return data.get(index * step);
    }

    @Override
    public int size() {
        return (data.size() + step - 1) / step;
    }
}
