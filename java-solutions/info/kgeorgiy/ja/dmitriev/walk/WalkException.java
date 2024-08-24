package info.kgeorgiy.ja.dmitriev.walk;

public class WalkException extends Exception {
    public WalkException(final String message) {
        super(message);
    }

    public WalkException(final String message, final Throwable cause) {
        super(message, cause);
    }
}