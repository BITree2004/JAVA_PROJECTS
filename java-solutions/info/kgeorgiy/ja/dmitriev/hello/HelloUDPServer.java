package info.kgeorgiy.ja.dmitriev.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.*;

import static info.kgeorgiy.ja.dmitriev.hello.HelloUDPUtils.*;

/**
 * Class, that the instance of interface {@link HelloServer}.
 *
 * @author Dmitriev Vladislav
 * @version 21
 * @see HelloServer
 * @since 21
 */
public class HelloUDPServer extends AbstractUDPServer {
    private final List<DatagramSocket> sockets = new ArrayList<>();

    /**
     * Console interface of {@link HelloUDPServer#start(int, Map)}.
     *
     * @param args console arguments
     */
    public static void main(final String[] args) {
        AbstractUDPServer.commonMain(args, HelloUDPNonblockingServer::new);
    }

    @Override
    public void start(final int threads, final Map<Integer, String> ports) {
        if (ports.isEmpty()) {
            return;
        }
        executorService = Executors.newFixedThreadPool(threads);
        portService = Executors.newFixedThreadPool(ports.size());
        ports.forEach((port, value) -> {
            try {
                final var socket = new DatagramSocket(port);
                sockets.add(socket);
                portService.submit(() -> read(answer -> value.replace("$", answer), socket));
            } catch (final SocketException e) {
                logError("Couldn't start server", e);
            }
        });
    }

    @Override
    public void close() {
        sockets.forEach(DatagramSocket::close);
        sockets.clear();
        final boolean isInterrupted = safeShutdown(executorService, EXECUTOR_TIMEOUT)
                | safeShutdown(portService, EXECUTOR_TIMEOUT);
        if (isInterrupted) {
            Thread.currentThread().interrupt();
        }
    }

    private void read(
            final Function<String, String> func,
            final DatagramSocket socket
    ) {
        final int bufLength;
        try {
            bufLength = socket.getReceiveBufferSize();
        } catch (final SocketException e) {
            logError("Couldn't start server", e);
            return;
        }
        while (!socket.isClosed() && !Thread.interrupted()) {
            final var packet = new HelloUDPPacket(
                    new DatagramPacket(new byte[bufLength], bufLength)
            );
            if (packet.receive(socket)) {
                executorService.submit(() -> runAndSend(func, socket, packet));
            }
        }
    }

    private static void runAndSend(
            final Function<String, String> func,
            final DatagramSocket socket,
            final HelloUDPPacket packet
    ) {
        try {
            packet.send(socket, func.apply(packet.getStringData()));
        } catch (final IOException e) {
            logError("Couldn't send reply", e);
        }
    }
}
