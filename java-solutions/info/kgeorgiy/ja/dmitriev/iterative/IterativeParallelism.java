package info.kgeorgiy.ja.dmitriev.iterative;

import info.kgeorgiy.java.advanced.iterative.AdvancedIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class implementing {@link AdvancedIP}.
 *
 * @author Dmitriev Vladislav (bitree2004@yandex.ru)
 * @see AdvancedIP
 * @since 21
 */
@SuppressWarnings("unused")
public class IterativeParallelism implements AdvancedIP {
    private final ParallelMapper instanceOfParallelMapper;

    /**
     * To create an object that will use its generated threads.
     */
    public IterativeParallelism() {
        this.instanceOfParallelMapper = null;
    }

    /**
     * To create an object that will use {@code parallelMapper}.
     *
     * @param parallelMapper which will be used for calculations.
     */
    public IterativeParallelism(final ParallelMapper parallelMapper) {
        this.instanceOfParallelMapper = parallelMapper;
    }

    @Override
    public <T> T reduce(
            final int threads,
            final List<T> values,
            final T identity,
            final BinaryOperator<T> operator,
            final int step
    ) throws InterruptedException {
        return mapReduce(threads, values, Function.identity(), identity, operator, step);
    }


    private static void joinThreads(final List<Thread> threads) throws InterruptedException {
        for (int i = 0; i < threads.size(); i++) {
            try {
                threads.get(i).join();
            } catch (final InterruptedException e) {
                /*for (int j = i; i < threads.size(); j++) {
                    threads.get(j).interrupt();
                }*/ // deleted for tl
                for (int j = i; j < threads.size(); j++) {
                    try {
                        // :NOTE: threads.get(j).interrupt() for rest of the threads; fixed
                        threads.get(j).join();
                    } catch (final InterruptedException e2) {
                        e.addSuppressed(e2);
                    }
                }
                throw e;
            }
        }
    }

    private static <T> void checkArguments(final int threads, final List<? extends T> values) {
        Objects.requireNonNull(values);
        if (threads < 1) {
            throw new IllegalArgumentException("Error: count of threads must be positive!");
        }
    }

    @Override
    public <T> T maximum(
            final int threads,
            final List<? extends T> values,
            final Comparator<? super T> comparator,
            final int step
    ) throws InterruptedException {
        return run(
                threads,
                new KthList<>(values, step),
                stream -> stream.max(comparator).orElseThrow(),
                stream -> stream.max(comparator).orElseThrow()
        );
    }

    @Override
    public <T> T minimum(
            final int threads,
            final List<? extends T> values,
            final Comparator<? super T> comparator,
            final int step
    ) throws InterruptedException {
        return maximum(threads, values, comparator.reversed(), step);
    }

    @Override
    public <T> boolean all(
            final int threads,
            final List<? extends T> values,
            final Predicate<? super T> predicate,
            final int step
    ) throws InterruptedException {
        return mapReduce(threads, values, predicate::test, true,
                         (a, b) -> a & b, step);
    }

    @Override
    public <T> boolean any(
            final int threads,
            final List<? extends T> values,
            final Predicate<? super T> predicate,
            final int step
    ) throws InterruptedException {
        return !all(threads, values, predicate.negate(), step);
    }

    @Override
    public <T> int count(
            final int threads, final List<? extends T> values,
            final Predicate<? super T> predicate,
            final int step
    ) throws InterruptedException {
        return mapReduce(threads, values, x -> predicate.test(x) ? 1 : 0, 0,
                Integer::sum, step);
    }

    private static <T, R> Stream<R> simpleRun(
            final int threads,
            final List<Stream<T>> tasks,
            final Function<Stream<T>, R> mapperForThread,
            final ArrayList<R> results
    ) throws InterruptedException {
        final var listThreads = new ArrayList<Thread>(threads);
        for (int i = 0; i < threads; i++) {
            final var index = i;
            listThreads.add(
                    new Thread(
                            () -> results.set(index, mapperForThread.apply(tasks.get(index)))
                    )
            );
            listThreads.get(i).start();
        }
        joinThreads(listThreads);
        return results.stream();
    }

    private <T, R> Stream<R> mapperRun(
            final int threads,
            final List<Stream<T>> tasks,
            final Function<Stream<T>, R> mapperForThread,
            final ArrayList<R> results
    ) throws InterruptedException {
        assert this.instanceOfParallelMapper != null;
        return this.instanceOfParallelMapper.map(mapperForThread, tasks).stream();
    }

    private <T, R> List<R> toList(
            final int threads,
            final List<T> values,
            final Function<Stream<T>, Stream<? extends R>> functor
    ) throws InterruptedException {
        return run(threads, values,
                stream -> functor.apply(stream).collect(Collectors.toList()),
                stream -> stream.flatMap(List::stream).collect(Collectors.toList()));
    }

    @Override
    public <T, R> R mapReduce(
            final int threads,
            final List<T> values,
            final Function<T, R> lift,
            final R identity,
            final BinaryOperator<R> binaryFunctor,
            final int step
    ) throws InterruptedException {
        return run(threads, new KthList<>(values, step),
                stream -> stream.map(lift).reduce(identity, binaryFunctor),
                stream -> stream.reduce(identity, binaryFunctor));
    }

    private static <T> List<Stream<T>> splitList(final int threads, final List<T> values) {
        final var bucket = values.size() / threads;
        var rest = values.size() % threads;
        final var res = new ArrayList<Stream<T>>(threads);
        var start = 0;
        for (int i = 0; i < threads; ++i) {
            final var next = start + bucket + (--rest >= 0 ? 1 : 0);
            res.add(values.subList(start, next).stream());
            start = next;
        }
        return res;
    }

    private <T, R> R run(
            int threads,
            final List<T> values,
            final Function<Stream<T>, R> mapperForThread,
            final Function<Stream<R>, R> mapperForResult
    ) throws InterruptedException {
        checkArguments(threads, values);
        threads = Math.max(1, Math.min(threads, values.size()));
        final var tasks = splitList(threads, values);
        final var results = new ArrayList<R>(Collections.nCopies(threads, null));
        final Stream<R> resultsFromThreads;
        if (this.instanceOfParallelMapper == null) {
            resultsFromThreads = simpleRun(threads, tasks, mapperForThread, results);
        } else {
            resultsFromThreads = mapperRun(threads, tasks, mapperForThread, results);
        }
        return mapperForResult.apply(resultsFromThreads);
    }

    @Override
    public String join(
            final int threads,
            final List<?> values,
            final int step
    ) throws InterruptedException {
        return mapReduce(
                threads,
                values,
                Object::toString,
                "",
                String::concat,
                step
        );
    }

    @Override
    public <T> List<T> filter(
            final int threads,
            final List<? extends T> values,
            final Predicate<? super T> predicate,
            final int step
    ) throws InterruptedException {
        return toList(
                threads,
                new KthList<>(values, step),
                x -> x.filter(predicate)
        );
    }

    @Override
    public <T, U> List<U> map(
            final int threads,
            final List<? extends T> values,
            final Function<? super T, ? extends U> f,
            final int step
    ) throws InterruptedException {
        return toList(threads, new KthList<>(values, step), x -> x.map(f));
    }
}
