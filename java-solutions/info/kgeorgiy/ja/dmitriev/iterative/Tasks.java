package info.kgeorgiy.ja.dmitriev.iterative;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

/*package-private*/ class Tasks<T, R> {
    private final ParallelMapperImpl parallelMapper;
    private final List<R> data;
    private int numberActive;
    private RuntimeException runtimeException;

    /*package-private*/ Tasks(
            final ParallelMapperImpl parallelMapper,
            final Function<? super T, ? extends R> functor,
            final List<? extends T> args
    ) {
        this.parallelMapper = parallelMapper;
        numberActive = args.size();
        data = new ArrayList<>(Collections.nCopies(numberActive, null));
        runtimeException = null;
        parallelMapper.queue.addAll(IntStream.range(0, numberActive).mapToObj(
                index -> new Task(
                        this,
                        () -> {
                            try {
                                set(index, functor.apply(args.get(index)));
                            } catch (final RuntimeException e) {
                                set(e);
                            }
                        })
        ).toList());
    }

    /*package-private*/
    synchronized void set(final int index, final R element) {
        data.set(index, element);
        decrement();
    }

    /*package-private*/
    synchronized void set(final RuntimeException exception) {
        if (runtimeException == null) {
            runtimeException = exception;
        } else {
            runtimeException.addSuppressed(exception);
        }
        decrement();
    }

    /*package-private*/
    synchronized List<R> get() throws InterruptedException {
        while (numberActive != 0 && parallelMapper.active) {
            wait();
        }
        if (runtimeException != null) {
            throw runtimeException;
        }
        if (numberActive == 0) {
            return data;
        } else {
            throw new IllegalArgumentException(
                    "ParallelMapperImpl was closed, before how the calculations ended"
            );
        }
    }

    /*package-private*/
    synchronized void close() {
        notify();
    }

    private void decrement() {
        --numberActive;
        if (numberActive == 0) {
            notify();
        }
    }
}