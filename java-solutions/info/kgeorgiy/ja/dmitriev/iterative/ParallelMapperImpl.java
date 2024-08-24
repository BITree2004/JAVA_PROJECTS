package info.kgeorgiy.ja.dmitriev.iterative;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * Class implementing {@link ParallelMapper}.
 *
 * @author Dmitriev Vladislav (bitree2004@yandex.ru)
 * @see ParallelMapper
 * @since 21
 */
@SuppressWarnings("unused")
public class ParallelMapperImpl implements ParallelMapper {

    /*package-private*/ final List<Thread> listOfThreads;
    /*package-private*/ final MapperQueue<Task> queue = new MapperQueue<>();
    /*package-private*/ boolean active = true;

    /**
     * Creates an {@link ParallelMapper} instance
     * with the number of threads equal to {@code threads}.
     *
     * @param threads the number of threads, that will be created
     */
    public ParallelMapperImpl(final int threads) {
        if (threads < 0) {
            throw new IllegalArgumentException("Threads should be positive number!");
        }
        final Runnable runnable = () ->
        {
            try {
                while (!Thread.interrupted()) {
                    queue.poll().runnable().run();
                }
            } catch (final InterruptedException ignored) {
            }
        };
        listOfThreads = IntStream.range(0, threads)
                .mapToObj(index -> new Thread(runnable))
                .toList();
        listOfThreads.forEach(Thread::start);
    }

    @Override
    public <T, R> List<R> map(
            final Function<? super T, ? extends R> f,
            final List<? extends T> args
    ) throws InterruptedException {
        return new Tasks<T, R>(this, f, args).get();
    }

    @Override
    public void close() {
        active = false;
        listOfThreads.forEach(Thread::interrupt);
        queue.forEach(x -> x.tasks().close());
        for (int i = 0; i < listOfThreads.size(); ) {
            try {
                listOfThreads.get(i).join();
                i++;
            } catch (final InterruptedException ignored) {
            }
        } // fixed note
    }

}
