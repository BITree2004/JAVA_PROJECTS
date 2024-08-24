package info.kgeorgiy.ja.dmitriev.hello;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;

import static info.kgeorgiy.ja.dmitriev.hello.HelloUDPUtils.CHARSET;
import static info.kgeorgiy.ja.dmitriev.hello.HelloUDPUtils.logError;

/*package-private*/ record HelloUDPPacket(DatagramPacket datagramPacket) {
    /*package-private*/ String getStringData() {
        return new String(datagramPacket.getData(),
                          datagramPacket.getOffset(),
                          datagramPacket.getLength(),
                          CHARSET);
    }

    /*package-private*/ boolean receive(final DatagramSocket socket) {
        try {
            final byte[] buf = datagramPacket.getData();
            datagramPacket.setData(buf, 0, buf.length);
            socket.receive(datagramPacket);
        } catch (final IOException e) {
            if (socket.isClosed()) {
                logError("Failed to receive", e);
            }
            return false;
        }
        return true;
    }

    /*package-private*/ void send(
            final DatagramSocket datagramSocket,
            final String request
    ) throws IOException {
        final var bytes = request.getBytes(StandardCharsets.UTF_8);
        datagramPacket.setData(bytes, 0, bytes.length);
        datagramSocket.send(datagramPacket);
    }

    /*package-private*/ void setData(final byte[] buffer) {
        datagramPacket.setData(buffer, 0, buffer.length);
    }

    /*package-private*/ byte[] getData() {
        return datagramPacket.getData();
    }
}
