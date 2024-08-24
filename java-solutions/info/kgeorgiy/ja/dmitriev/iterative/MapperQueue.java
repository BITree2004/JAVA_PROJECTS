package info.kgeorgiy.ja.dmitriev.iterative;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Queue;
import java.util.function.Consumer;

/*package-private*/ class MapperQueue<E> {
    private final Queue<E> data = new ArrayDeque<>();

    /*package-private*/ synchronized void forEach(final Consumer<? super E> consumer) {
        data.forEach(consumer);
    }

    /*package-private*/ synchronized E poll() throws InterruptedException {
        while (data.isEmpty()) {
            wait();
        }
        return data.poll();
    }

    /*package-private*/ synchronized void addAll(final Collection<? extends E> collection) {
        data.addAll(collection);
        data.forEach(x -> this.notify());
    }
}
