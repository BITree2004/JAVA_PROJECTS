package info.kgeorgiy.ja.dmitriev.walk;

import java.nio.file.Path;

public class RecursiveWalk {
    private static final AbstractWalk ABSTRACT_WALK = new AbstractWalk((Path file) -> true);

    public static void main(final String[] args) {
        ABSTRACT_WALK.walkWithLog(args);
    }
}