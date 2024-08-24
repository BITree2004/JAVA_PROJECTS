package info.kgeorgiy.ja.dmitriev.project;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static info.kgeorgiy.ja.dmitriev.project.Util.*;

public class Main {
    final private static String READY_TASK_FORMAT = "* ~~%s : %s~~\n";
    final private static String STANDARD_FORMAT = "* %s : %s\n";
    enum MODE {READY, UNREADY, ANY}
    final Map<String, String> readyTasks = new HashMap<>();
    final Map<String, String> notReadyTasks = new HashMap<>();
    final Map<String, MODE> modeTaskMap = new HashMap<>();
    private void print(final MODE arg, final PrintStream outputStream, final boolean isMarked) {
        switch (arg) {
            case READY -> printUnready(outputStream);
            case UNREADY -> printReady(outputStream, isMarked);
            case ANY -> {
                print(MODE.UNREADY, outputStream, true);
                print(MODE.READY, outputStream, true);
            }
        }
    }

    private void printReady(final PrintStream out, final boolean isMarked) {
        readyTasks.forEach(
                (mark, task) -> out.format(
                        isMarked ? READY_TASK_FORMAT : STANDARD_FORMAT,
                        mark,
                        task
                )
        );
    }

    private void printUnready(final PrintStream out) {
        notReadyTasks.forEach((mark, task) -> out.format(STANDARD_FORMAT, mark, task));
    }

    private String getTask(final String[] args) {
        return Arrays.stream(args).skip(2).collect(Collectors.joining(" "));
    }

    private void add(
            final String[] args,
            final PrintStream errorStream
    ) {
        if (args.length != 2 && args.length != 3) {
            logError("Add must be have two or three args", errorStream);
            return;
        }
        if (args.length < 3) {
            try (final var bufferedReader = new BufferedReader(new FileReader(args[1]))) {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    final var parsedTokens = line.split("/* ~~| : |~~|/* ");
                    final var mark = parsedTokens[1];
                    final var task = getTask(parsedTokens);
                    if (line.endsWith("~~")) {
                        readyTasks.put(mark, task);
                    } else {
                        notReadyTasks.put(mark, task);
                    }
                }
            } catch (final IOException e) {
                logError(String.format("Failed read from file [%s] in add", args[1]), e, errorStream);
            }
        } else {
            notReadyTasks.put(args[1], getTask(args));
        }
    }

    private void mark(
            final String[] args,
            final PrintStream errorStream
    ) {
        if (args.length != 2) {
            logError("Mark must be have two args", errorStream);
            return;
        }
        final String task = notReadyTasks.remove(args[1]);
        if (task != null) {
            readyTasks.put(args[1], task);
        }
    }

    private void remove(
            final String[] args,
            final PrintStream errorStream
    ) {
        if (args.length != 2) {
            logError("Remove must be have two args", errorStream);
            return;
        }
        readyTasks.remove(args[1]);
        notReadyTasks.remove(args[1]);
    }

    private void print(
            final String[] args,
            final PrintStream outputStream,
            final PrintStream errorStream
    ) {
        if (args.length != 2 && args.length != 3) {
            logError("Print must be have two or three args", errorStream);
            return;
        }
        PrintStream actualStream = outputStream;
        if (args.length == 3) {
            try {
                actualStream = new PrintStream(new FileOutputStream(args[2]));
            } catch (final FileNotFoundException e) {
                logError(String.format("Failed read from file [%s] in add", args[2]), e, errorStream);
                return;
            }
        }
        print(modeTaskMap.get(args[1]), actualStream, false);
    }

    private void start(
            final InputStream inputStream,
            final PrintStream outputStream,
            final PrintStream errorStream
    ) {
        try (final var reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            String[] args;
            while ((line = reader.readLine()) != null) {
                args = line.split(" ");
                switch (args[0]) {
                    case "add" -> add(args, errorStream);
                    case "mark" -> mark(args, errorStream);
                    case "remove" -> remove(args, errorStream);
                    case "print" -> print(args, outputStream, errorStream);
                    default -> logError(String.format("Unsupported token: %s", args[0]), errorStream);
                }
            }
        } catch (final IOException e) {
            logError("Couldn't read from input stream", e, errorStream);
        }
    }

    public static void main(final String[] args) {
        new Main().start(System.in, System.out, System.err);
    }
}