package info.kgeorgiy.ja.dmitriev.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;
import info.kgeorgiy.java.advanced.hello.NewHelloServer;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

import static info.kgeorgiy.ja.dmitriev.hello.HelloUDPUtils.parseInteger;

/*package-private*/ abstract class AbstractUDPServer implements NewHelloServer {
    protected ExecutorService executorService;
    protected ExecutorService portService;
    private static final String USAGE_SERVER = """
            Use: host port
            host - the name or IP address of the computer on which the server is running;
            port - port number to which to send requests;
            """;

    /**
     * Console interface of {@link HelloUDPServer#start(int, Map)}.
     *
     * @param args console arguments
     */
    protected static void commonMain(final String[] args, final Supplier<HelloServer> supplier) {
        if (args == null || Arrays.stream(args).anyMatch(Objects::isNull) || args.length != 2) {
            System.err.println(USAGE_SERVER);
            return;
        }
        try (final var server = supplier.get()) {
            server.start(parseInteger(args[0], "Host"), parseInteger(args[1], "Port"));
        }
    }
}
