package info.kgeorgiy.ja.dmitriev.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.*;

import static info.kgeorgiy.ja.dmitriev.hello.HelloUDPUtils.*;

/**
 * Class, that the instance of interface {@link HelloServer}.
 *
 * @author Dmitriev Vladislav
 * @see HelloServer
 * @since 21
 */
public class HelloUDPNonblockingServer extends AbstractUDPServer {
    private final List<DatagramChannel> datagramChannels = new ArrayList<>();
    private final Queue<Context> updatedContext = new ConcurrentLinkedQueue<>();
    private Selector selector;

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
        try {
            selector = Selector.open();
            portService = Executors.newSingleThreadExecutor();
            executorService = Executors.newFixedThreadPool(threads);
            for (final var portAndPattern : ports.entrySet()) {
                final var channel = createChannel();
                channel.bind(new InetSocketAddress(portAndPattern.getKey()));
                datagramChannels.add(channel);
                final var bufSize = channel.socket().getReceiveBufferSize();
                channel.register(
                        selector,
                        SelectionKey.OP_READ,
                        new Context(
                                bufSize,
                                (answer) -> portAndPattern.getValue().replace("$", answer),
                                channel
                        )
                );
            }
            portService.submit(this::listen);
        } catch (final IOException e) {
            logError("Couldn't start server", e);
        }
    }

    private void listen() {
        while (!Thread.interrupted() && !selector.keys().isEmpty()) {
            try {
                selector.select();
            } catch (final IOException e) {
                logError("Failed listen", e);
                return;
            }
            for (final Iterator<SelectionKey> i =
                 selector.selectedKeys().iterator(); i.hasNext(); ) {
                final var selectionKey = i.next();
                if (selectionKey.isReadable()) {
                    read(selectionKey);
                }
                if (selectionKey.isWritable()) {
                    write(selectionKey);
                }
                i.remove();
            }
            /*
            *  If no selection operation is currently in progress
            *   then the next invocation of a selection operation will return immediately
            * */
            while (true) {
                final var context = updatedContext.poll();
                if (context == null) {
                    break;
                } else {
                    context.update();
                }
            }
        }
    }

    private void write(final SelectionKey selectionKey) {
        final var context = (Context) selectionKey.attachment();
        final var channel = (DatagramChannel) selectionKey.channel();
        final var packet = context.getPacket();
        if (packet == null) {
            return;
        }
        try {
            channel.send(packet.buffer, packet.socketAddress);
        } catch (final IOException e) {
            logError("Failed to send message", e);
        }
    }

    private void read(final SelectionKey selectionKey) {
        final var context = (Context) selectionKey.attachment();
        final ByteBuffer buffer = ByteBuffer.allocate(context.bufSize);
        final var channel = (DatagramChannel) selectionKey.channel();
        try {
            final var address = channel.receive(buffer);
            if (address == null) {
                return;
            }
            executorService.submit(() -> execute(context, new Packet(buffer, address, context)));
        } catch (final IOException e) {
            logError("Failed to get address in read", e);
        }
    }

    private void execute(final Context context, final Packet packet) {
        packet.buffer.flip();
        final String answer = context.calcResponse.apply(CHARSET.decode(packet.buffer).toString());
        packet.buffer.clear();
        packet.buffer.put(answer.getBytes(CHARSET));
        packet.buffer.flip();
        context.addPacket(packet);
    }

    @Override
    public void close() {
        if (selector != null) {
            try {
                selector.close();
            } catch (final IOException e) {
                logError("Error in close selector", e);
            }
        }
        for (final DatagramChannel datagramChannel : datagramChannels) {
            try {
                datagramChannel.close();
            } catch (final IOException e) {
                logError("Error in close channel", e);
            }
        }
        updatedContext.clear();
        datagramChannels.clear();
        final var isInterrupted =
                safeShutdown(executorService, EXECUTOR_TIMEOUT)
                        | safeShutdown(portService, EXECUTOR_TIMEOUT);
        if (isInterrupted) {
            Thread.currentThread().interrupt();
        }
    }


    private record Packet(ByteBuffer buffer, SocketAddress socketAddress, Context context) {
    }

    private class Context {
        final Queue<Packet> packets = new ConcurrentLinkedQueue<>();
        final Function<String, String> calcResponse;
        final int bufSize;
        final DatagramChannel channel;

        Context(
                final int bufSize,
                final Function<String, String> calcResponse,
                final DatagramChannel channel
        ) {
            this.bufSize = bufSize;
            this.calcResponse = calcResponse;
            this.channel = channel;
        }

        // :NOTE: synchronized | fixed
        void addPacket(final Packet packet) {
            packets.add(packet);
            updatedContext.add(this);
            selector.wakeup();
        }

        Packet getPacket() {
            updatedContext.add(this);
            // :NOTE: ?? | fixed delete wakeup
            return packets.poll();
        }

        void update() {
            final var key = channel.keyFor(selector);
            if (packets.isEmpty()) {
                key.interestOpsAnd(~SelectionKey.OP_WRITE);
            } else {
                key.interestOpsOr(SelectionKey.OP_WRITE);
            }
        }
    }
}
