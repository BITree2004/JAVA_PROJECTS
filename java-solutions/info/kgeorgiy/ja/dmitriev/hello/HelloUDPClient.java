package info.kgeorgiy.ja.dmitriev.hello;

import info.kgeorgiy.java.advanced.hello.*;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static info.kgeorgiy.ja.dmitriev.hello.HelloUDPUtils.*;

/**
 * Class, that the instance of interface {@link HelloClient}.
 *
 * @author Dmitriev Vladislav
 * @version 21
 * @see HelloClient
 * @since 21
 */
public class HelloUDPClient extends AbstractUDPClient {
    /**
     * Console interface of {@link HelloUDPClient#run}.
     *
     * @param args console arguments
     */
    public static void main(final String[] args) {
        AbstractUDPClient.commonMain(args, HelloUDPClient::new);
    }

    @Override
    public void run(
            final String host,
            final int port,
            final String prefix,
            final int threads,
            final int requests
    ) {
        if (threads == 0) {
            return;
        }
        final var inetSocketAddress = new InetSocketAddress(host, port);
        final var executorService = Executors.newFixedThreadPool(threads);
        IntStream.range(1, threads + 1)
                .<Runnable>mapToObj(i -> () -> taskForThread(i, inetSocketAddress, prefix, requests))
                .forEach(executorService::submit);
        if (safeShutdown(executorService, EXECUTOR_TIMEOUT * threads * requests)) {
            Thread.currentThread().interrupt();
        }
    }

    private static void taskForThread(
            final int indexThread,
            final InetSocketAddress inetSocketAddress,
            final String prefix,
            final int requests
    ) {
        try (final var datagramSocket = new DatagramSocket()) {
            datagramSocket.setSoTimeout(SOCKET_TIMEOUT);
            final var bufSize = datagramSocket.getReceiveBufferSize();
            final var packet = new HelloUDPPacket(
                    new DatagramPacket(new byte[bufSize], bufSize, inetSocketAddress)
            );
            final byte[] buffer = packet.getData();
            IntStream.range(1, requests + 1).forEach(indexRequest -> taskForRequest(
                    indexThread,
                    indexRequest,
                    prefix,
                    datagramSocket,
                    packet,
                    buffer
            ));
        } catch (final SocketException e) {
            logError("Failed to open socket!", e);
        }
    }

    private static void taskForRequest(
            final int indexThread,
            final int indexRequest,
            final String prefix,
            final DatagramSocket datagramSocket,
            final HelloUDPPacket packet,
            final byte[] buffer
    ) {
        final var request = prefix + indexThread + "_" + indexRequest;
        boolean isSuccessful = false;
        while (!isSuccessful && !datagramSocket.isClosed() && !Thread.interrupted()) {
            try {
                packet.send(datagramSocket, request);
                logRequest(request);
                packet.setData(buffer);
                if (packet.receive(datagramSocket)) {
                    final var answer = packet.getStringData();
                    logResponse(answer);
                    isSuccessful = isValidAnswer(indexThread, indexRequest, answer);
                }
            } catch (final IOException e) {
                logError(String.format("Failed to send request: %s", request), e);
            }
        }
    }
}
