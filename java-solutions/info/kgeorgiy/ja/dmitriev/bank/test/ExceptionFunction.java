package info.kgeorgiy.ja.dmitriev.bank.test;

@FunctionalInterface
/*package-private*/ interface ExceptionFunction<T, V, E extends Throwable> {
    /*package-private*/ V apply(T it) throws E;
}
