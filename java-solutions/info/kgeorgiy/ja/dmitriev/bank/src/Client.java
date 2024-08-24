package info.kgeorgiy.ja.dmitriev.bank.src;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import info.kgeorgiy.ja.dmitriev.bank.src.bank.*;
import info.kgeorgiy.ja.dmitriev.bank.src.person.*;

import static info.kgeorgiy.ja.dmitriev.bank.src.RemoteInformation.*;
import static info.kgeorgiy.ja.dmitriev.bank.src.Utils.*;

/**
 * Console interface for working with the library.
 * Provide to create {@link info.kgeorgiy.ja.dmitriev.bank.src.account.Account}.
 * {@see Client#main}.
 *
 * @author Dmitriev Vladislav (bitree2004@yandex.ru)
 * @since 21
 */
public final class Client {
    private static final String USAGE = """
            USAGE:
            first name
            second name
            number of passport
            subId
            delta of amount
            """;

    /**
     * Utility class.
     */
    private Client() {
    }

    /**
     * Provides access to create accounts through the console.
     *
     * @param args console arguments
     */
    public static void main(final String... args) throws RemoteException {
        checkForNull(args);
        if (args.length != 5) {
            System.err.println(USAGE);
            return;
        }
        final var personCharacteristics = new PersonCharacteristics(args[0], args[1], args[2]);
        final var subId = args[3];
        final var deltaAmount = parseArg(args[4], "Delta of amount");
        if (deltaAmount == null) {
            return;
        } else {
            System.out.println("Person already exists");
        }
        final Bank bank;
        try {
            bank = (Bank) Naming.lookup(DEFAULT_BANK);
        } catch (final NotBoundException e) {
            System.out.println("Bank is not bound");
            return;
        } catch (final MalformedURLException e) {
            System.out.println("Bank URL is invalid");
            return;
        }
        Person remotePerson;
        try {
            remotePerson = bank.getRemotePerson(personCharacteristics.passport());
        } catch (final RemoteException e) {
            System.err.println("ERROR in getRemotePerson!");
            return;
        }
        if (remotePerson == null) {
            System.out.println("Creating remote person");
            remotePerson = bank.createPerson(personCharacteristics);
        }
        var account = remotePerson.getAccount(subId);
        if (account == null) {
            account = remotePerson.addAccount(subId);
            System.out.println("Creating account");
        } else {
            System.out.println("Account already exists");
        }
        System.out.println("Account id: " + account.getId());
        System.out.println("Money: " + account.getAmount());
        System.out.println("Adding money");
        try {
            account.setAmount(account.getAmount() + deltaAmount);
        } catch (final IllegalArgumentException e) {
            System.err.println(e.getMessage());
            return;
        }
        System.out.println("Money: " + account.getAmount());
    }
}