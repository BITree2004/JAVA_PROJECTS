package info.kgeorgiy.ja.dmitriev.bank.src;

import info.kgeorgiy.ja.dmitriev.bank.src.bank.Bank;
import info.kgeorgiy.ja.dmitriev.bank.src.bank.RemoteBank;
import static info.kgeorgiy.ja.dmitriev.bank.src.Utils.*;
import static info.kgeorgiy.ja.dmitriev.bank.src.RemoteInformation.*;

import java.rmi.*;
import java.rmi.server.*;
import java.net.*;

/**
 * Console interface for working with the library. Provide to start {@link Bank}.
 * {@see Client#main}.
 *
 * @author Dmitriev Vladislav (bitree2004@yandex.ru)
 * @since 21
 */
public final class Server {

    /**
     * Allows you to run the server via the console.
     *
     * @param args console arguments
     */
    public static void main(final String... args) {
        final Integer port = args.length > 0 ? parseArg(args[0], "Port") : BANK_PORT;
        if (port == null) {
            return;
        }
        final Bank bank = new RemoteBank(port);
        try {
            UnicastRemoteObject.exportObject(bank, port);
            Naming.rebind(DEFAULT_BANK, bank);
            System.out.println("Server started");
        } catch (final RemoteException e) {
            System.out.println("Cannot export object: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (final MalformedURLException e) {
            System.out.println("Malformed URL");
        }
    }
}
