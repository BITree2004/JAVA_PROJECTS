package info.kgeorgiy.ja.dmitriev.arrayset;

import java.util.*;

@SuppressWarnings("unused")
public class ArraySet<E> extends AbstractList<E> implements List<E>, NavigableSet<E> {
    // :NOTE: List.of() | fixed
    public ArraySet() {
        this(List.of(), null);
    }

    public ArraySet(
            final Collection<? extends E> c
    ) {
        this(c, null);
    }

    public ArraySet(
            final Collection<? extends E> c,
            final Comparator<? super E> comparator
    ) {
        // :NOTE: c can be already sorted | cast in TreeSet
        final var set = new TreeSet<E>(comparator);
        set.addAll(c);
        data = new ArrayList<>(set);
        cmp = Collections.reverseOrder(Collections.reverseOrder(comparator));
        returnedCmp = comparator;
    }

    private ArraySet(
            final List<E> data,
            final Comparator<? super E> cmp,
            final Comparator<? super E> returnedComparator
    ) {
        this.data = data;
        this.cmp = cmp;
        this.returnedCmp = returnedComparator;
    }

    @Override
    public E lower(final E e) {
        return get(lowerIndex(e));
    }

    private int lowerIndex(final E e) {
        return binarySearch(e, -1, -1);
    }

    @Override
    public E floor(final E e) {
        return get(floorIndex(e));
    }

    private int floorIndex(final E e) {
        return binarySearch(e, 0, -1);
    }

    @Override
    public E ceiling(final E e) {
        return get(ceilingIndex(e));
    }

    private int ceilingIndex(final E e) {
        return binarySearch(e, 0, 0);
    }

    @Override
    public E higher(final E e) {
        return get(higherIndex(e));
    }

    private int higherIndex(final E e) {
        return binarySearch(e, 1, 0);
    }

    @Override
    public E pollLast() {
        throw new UnsupportedOperationException(
                "Error: ArraySet doesn't support pollLast!");
    }

    @Override
    public E pollFirst() {
        throw new UnsupportedOperationException(
                "Error: ArraySet doesn't support pollFirst!");
    }

    @Override
    public Iterator<E> descendingIterator() {
        return descendingSet().iterator();
    }

    @Override
    public NavigableSet<E> subSet(
            final E fromElement,
            final boolean fromInclusive,
            final E toElement,
            final boolean toInclusive
    ) {
        if (cmp.compare(fromElement, toElement) > 0) {
            throw new IllegalArgumentException(
                    "Expected fromElement <= toElement!");
        }
        return subSetUnchecked(fromElement, fromInclusive, toElement, toInclusive);
    }

    @Override
    public ArraySet<E> descendingSet() {
        // :NOTE: ds -> ss -> ds -> ss performance | fixed in ReverseOrderListView
        return new ArraySet<>(data.reversed(), Collections.reverseOrder(cmp),
                              Collections.reverseOrder(returnedCmp));
    }

    @Override
    public NavigableSet<E> headSet(final E toElement, final boolean inclusive) {
        if (isEmpty()) {
            return new ArraySet<>(List.of(), cmp, returnedCmp);
        }
        return subSetUnchecked(first(), true, toElement, inclusive);
    }

    @Override
    public NavigableSet<E> tailSet(final E fromElement,
            final boolean inclusive) {
        if (isEmpty()) {
            return new ArraySet<>(List.of(), cmp, returnedCmp);
        }
        return subSetUnchecked(fromElement, inclusive, last(), true);
    }

    @Override
    public Comparator<? super E> comparator() {
        return returnedCmp;
    }

    @Override
    public SortedSet<E> subSet(final E fromElement, final E toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<E> headSet(final E toElement) {
        return headSet(toElement, false);
    }

    @Override
    public SortedSet<E> tailSet(final E fromElement) {
        return tailSet(fromElement, true);
    }

    @Override
    public E removeFirst() {
        throw new UnsupportedOperationException(
                "Error: ArraySet doesn't support removeFirst!");
    }

    @Override
    public E removeLast() {
        throw new UnsupportedOperationException(
                "Error: ArraySet doesn't support removeLast!");
    }

    @Override
    public ArraySet<E> reversed() {
        return descendingSet();
    }

    @Override
    public E first() {
        return getFirst();
    }

    @Override
    public E last() {
        return getLast();
    }

    @Override
    public Spliterator<E> spliterator() {
        return super.spliterator();
    }

    @Override
    public void addLast(final E e) {
        throw new UnsupportedOperationException(
                "Error: ArraySet doesn't support addLast!");
    }

    @Override
    public void addFirst(final E e) {
        throw new UnsupportedOperationException(
                "Error: ArraySet doesn't support addFirst!");
    }

    @Override
    public E getLast() {
        return data.getLast();
    }

    @Override
    public E getFirst() {
        return data.getFirst();
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public boolean contains(final Object o) {
        return indexOf(o) != -1;
    }

    private boolean isValidIndex(final int index) {
        return 0 <= index && index < size();
    }

    @Override
    public E get(final int index) {
        if (isValidIndex(index)) {
            return data.get(index);
        }
        return null;
    }

    private int binarySearch(final E key, final int found, final int notFound) {
        int res = Collections.binarySearch(data, key, cmp);
        if (res < 0) {
            res = -(res + 1) + notFound;
        } else {
            res += found;
        }
        return isValidIndex(res) ? res : -1;
    }

    @Override
    @SuppressWarnings("unchecked")
    public int indexOf(final Object o) {
        final E obj = (E) o;
        final int res = ceilingIndex(obj);
        return (res != higherIndex(obj) ? res : -1);
    }

    @Override
    public int lastIndexOf(final Object o) {
        return indexOf(o);
    }


    private NavigableSet<E> subSetUnchecked(
            final E fromElement,
            final boolean fromInclusive, final E toElement,
            final boolean toInclusive
    ) {
        final int l = fromInclusive ? ceilingIndex(fromElement) : higherIndex(fromElement);
        final int r = toInclusive ? floorIndex(toElement) : lowerIndex(toElement);
        if (l == -1 || r == -1 || l > r) {
            return new ArraySet<>(List.of(), cmp, returnedCmp);
        }
        return new ArraySet<>(data.subList(l, r + 1), cmp, returnedCmp);
    }

    private final List<E> data;
    private final Comparator<? super E> cmp;
    private final Comparator<? super E> returnedCmp;
}