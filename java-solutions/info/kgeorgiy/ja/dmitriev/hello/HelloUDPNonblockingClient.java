package info.kgeorgiy.ja.dmitriev.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static info.kgeorgiy.ja.dmitriev.hello.HelloUDPUtils.*;

/**
 * Class, that the instance of interface {@link HelloClient}.
 *
 * @author Dmitriev Vladislav
 * @see HelloClient
 * @since 21
 */
public class HelloUDPNonblockingClient extends AbstractUDPClient {
    /**
     * Console interface of {@link HelloUDPClient#run}.
     *
     * @param args console arguments
     */
    public static void main(final String[] args) {
        AbstractUDPClient.commonMain(args, HelloUDPNonblockingClient::new);
    }

    @Override
    public void run(
            final String host,
            final int port,
            final String prefix,
            final int threads,
            final int requests
    ) {
        try {
            Selector selector = null;
            try {
                selector = makeSelector();
                makeChannels(host, port, threads, selector);
                listen(selector, prefix, requests);
            } finally {
                safeCloseWithChannels(selector);
            }
        } catch (final IOException e) {
            logError("Failed to work server!", e);
        }
    }

    private static Selector makeSelector() throws IOException {
        try {
            return Selector.open();
        } catch (final IOException e) {
            logError("Failed to open selector", e);
            throw e;
        }
    }

    private static void makeChannels(
            final String host,
            final int port,
            final int threads,
            final Selector selector
    ) throws IOException {
        final var socketAddress = new InetSocketAddress(host, port);
        for (int indexThread = 1; indexThread <= threads; indexThread++) {
            final var channel = createChannel();
            try {
                channel.connect(socketAddress);
                final var bufSize = channel.socket().getReceiveBufferSize();
                channel.register(
                        selector,
                        SelectionKey.OP_WRITE,
                        new Context(
                                ByteBuffer.allocate(bufSize),
                                indexThread,
                                1
                        )
                );
            } catch (final IOException e) {
                logError("Failed to create channel!", e);
                channel.close(); // :NOTE: resource leak | fixed, see finally
                throw e;
            }
        }
    }

    private static void listen(
            final Selector selector,
            final String prefix,
            final int requests
    ) {
        while (!Thread.interrupted() && !selector.keys().isEmpty()) {
            try {
                selector.select(SOCKET_TIMEOUT);
            } catch (final IOException e) {
                logError("Failed listen", e);
                continue;
            }
            final var selectionKeys = selector.selectedKeys();
            if (selectionKeys.isEmpty()) {
                selector.keys().forEach(key -> key.interestOps(SelectionKey.OP_WRITE));
            }
            for (final Iterator<SelectionKey> i = selectionKeys.iterator(); i.hasNext(); ) {
                final var selectionKey = i.next();
                if (selectionKey.isWritable()) {
                    write(selectionKey, prefix);
                }
                if (selectionKey.isReadable()) {
                    read(selectionKey, requests);
                }
                i.remove();
            }
        }
    }

    private static void read(final SelectionKey selectionKey, final int requests) {
        getChannelAndApply(
                selectionKey,
                (context) -> (key, channel) -> context.read(key, channel, requests)
        );
    }

    private static void write(final SelectionKey selectionKey, final String prefix) {
        // :NOTE: copy-paste | fixed
        getChannelAndApply(
                selectionKey,
                (context) -> (key, channel) -> context.write(key, channel, prefix)
        );
    }
    
    private static void getChannelAndApply(
            final SelectionKey selectionKey,
            final Function<Context, BiConsumer<SelectionKey, DatagramChannel>> func
    ) {
        final var context = (Context) selectionKey.attachment();
        final var datagramChannel = (DatagramChannel) selectionKey.channel();
        func.apply(context).accept(selectionKey, datagramChannel);
    }

    private static class Context {
        final ByteBuffer byteBuffer;
        final int indexThread;
        int indexRequest;

        private Context(
                final ByteBuffer byteBuffer,
                final int indexThread,
                final int indexRequest
        ) {
            this.byteBuffer = byteBuffer;
            this.indexThread = indexThread;
            this.indexRequest = indexRequest;
        }

        private void write(
                final SelectionKey selectionKey,
                final DatagramChannel datagramChannel,
                final String prefix
        ) {
            final var answer = prefix + indexThread + '_' + indexRequest;
            byteBuffer.clear();
            byteBuffer.put(answer.getBytes(CHARSET));
            byteBuffer.flip();
            try {
                datagramChannel.send(byteBuffer, datagramChannel.getRemoteAddress());
                logRequest(answer);
                selectionKey.interestOps(SelectionKey.OP_READ);
            } catch (final IOException e) {
                logError("Some I/O error occurs in send", e);
            }
        }

        private void read(
                final SelectionKey selectionKey,
                final DatagramChannel channel,
                final int requests
        ) {
            byteBuffer.clear();
            try {
                channel.receive(byteBuffer);
            } catch (final IOException e) {
                logError("Some I/O error occurs in receive", e);
                return;
                // :NOTE: continued?? | fixed
            }
            byteBuffer.flip();
            final var answer = CHARSET.decode(byteBuffer).toString();
            logResponse(answer);
            if (isValidAnswer(indexThread, indexRequest, answer)) {
                indexRequest++;
                if (indexRequest == requests + 1) {
                    safeCloseChannel(channel);
                    return;
                }
            }
            selectionKey.interestOps(SelectionKey.OP_WRITE);
        }
    }
}
