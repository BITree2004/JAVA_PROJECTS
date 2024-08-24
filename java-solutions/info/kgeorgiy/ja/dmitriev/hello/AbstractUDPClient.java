package info.kgeorgiy.ja.dmitriev.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;

import static info.kgeorgiy.ja.dmitriev.hello.HelloUDPUtils.parseInteger;

/*package-private*/ abstract class AbstractUDPClient implements HelloClient {
    private static final String USAGE_CLIENT = """
            Use: host port prefix threads request
            host - the name or IP address of the computer on which the server is running;
            port - port number to which to send requests;
            prefix - requests prefix(string);
            threads - number of parallel request threads;
            request - number of requests in each thread.
            """;

    /**
     * Console interface of {@link HelloUDPClient#run}.
     *
     * @param args console arguments
     */
    protected static void commonMain(final String[] args, final Supplier<HelloClient> supplier) {
        if (args == null || Arrays.stream(args).anyMatch(Objects::isNull) || args.length != 5) {
            System.err.println(USAGE_CLIENT);
            return;
        }
        supplier.get().run(
                args[0],
                parseInteger(args[1], "Port"),
                args[2],
                parseInteger(args[3], "Threads"),
                parseInteger(args[4], "Request")
        );
        // :NOTE: catch RuntimeException | fixed
    }
}
