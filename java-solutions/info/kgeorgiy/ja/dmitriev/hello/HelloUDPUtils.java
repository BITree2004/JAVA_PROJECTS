package info.kgeorgiy.ja.dmitriev.hello;


import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.channels.DatagramChannel;
import java.nio.channels.Selector;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import static java.util.regex.Pattern.UNICODE_CHARACTER_CLASS;

/*package-private*/ final class HelloUDPUtils {
    /*package-private*/ static final int SOCKET_TIMEOUT = 100;
    /*package-private*/ static final long EXECUTOR_TIMEOUT = 3;
    /*package-private*/ static final Charset CHARSET = StandardCharsets.UTF_8;

    private static final Pattern PATTERN =
            Pattern.compile("(\\D*)(\\d+)(\\D*)(\\d+)(\\D*)", UNICODE_CHARACTER_CLASS);

    /*package-private*/
    static void logResponse(final String request) {
        System.out.printf("Response send: %s!%n", request);
    }

    /*package-private*/
    static void logRequest(final String request) {
        System.out.printf("Request send: %s!%n", request);
    }

    /*package-private*/
    static void logError(final String text, final Exception exception) {
        System.err.printf("Error : %s! Reason exception: %s.%n", text, exception.getMessage());
    }

    /*package-private*/
    static boolean safeShutdown(final ExecutorService executorService, final long timeout) {
        if (executorService == null) {
            return false;
        }
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(timeout, TimeUnit.SECONDS)) {
                System.err.println("The executor did not meet the deadline!");
                final var tasks = executorService.shutdownNow();
                System.err.printf("The executor was shutdown. %s tasks will not be completed!",
                                  tasks.size());
            }
        } catch (final InterruptedException e) {
            executorService.shutdownNow();
            logError("The main thread was interrupted while wait", e);
            return true;
        }
        return false;
    }

    /*package-private*/ static int parseInteger(final String arg, final String name) {
        try {
            return Integer.parseInt(arg);
        } catch (final NumberFormatException e) {
            throw new IllegalArgumentException(name + " must be integer!", e);
        }
    }

    private static int getInt(final String answer) {
        if (!answer.isEmpty()) {
            try {
                return Integer.parseInt(answer);
            } catch (final NumberFormatException e) {
                logError("Number wasn't correct", e);
            }
        }
        return -1;
    }

    /*package-private*/
    static boolean isValidAnswer(final int indexThread, final int indexRequest, final String answer) {
        final Matcher mather = PATTERN.matcher(answer);
        return mather.matches()
                && getInt(mather.group(2)) == indexThread
                && getInt(mather.group(4)) == indexRequest;

    }

    // utils for hw12 unblocking!
    /*package-private*/
    static DatagramChannel createChannel() throws IOException {
        final DatagramChannel res = DatagramChannel.open();
        res.configureBlocking(false);
        res.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        return res;
    }

    /*package-private*/
    static void safeCloseChannel(final DatagramChannel datagramChannel) {
        try {
            datagramChannel.close();
        } catch (final IOException ignored) {
        }
    }

    /*package-private*/
    static void safeCloseWithChannels(final Selector selector) throws IOException {
        if (selector == null) {
            return;
        }
        try {
            selector.keys().forEach(key -> safeCloseChannel((DatagramChannel) key.channel()));
            selector.close();
        } catch (final IOException e) {
            logError("Failed to end client!", e);
            throw e;
        }
    }
}
