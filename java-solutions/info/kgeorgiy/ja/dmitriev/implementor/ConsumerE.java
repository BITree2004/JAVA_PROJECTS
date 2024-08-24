package info.kgeorgiy.ja.dmitriev.implementor;

/**
 * Represents an operation that accepts a single input argument and returns no result
 * @param <T> the type of the input to the operation
 * @param <E> the type of exception that will be thrown if the writing is unsuccessful
 *
 * @author Dmitriev Vladislav (bitree2004@yandex.ru)
 * @since 21
 */
@FunctionalInterface
public interface ConsumerE<T, E extends Exception> {
    /**
     * Performs this operation on the given argument.
     * @param t is the input argument
     * @throws E will be thrown if the writing is unsuccessful
     */
    void write(T t) throws E;
}

