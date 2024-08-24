package info.kgeorgiy.ja.dmitriev.walk;

import java.nio.file.Files;
import java.util.function.Predicate;

public class Walk {
    private static final AbstractWalk ABSTRACT_WALK = new AbstractWalk(Predicate.not(Files::isDirectory));

    public static void main(final String[] args) {
        ABSTRACT_WALK.walkWithLog(args);
    }
}
