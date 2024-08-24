package info.kgeorgiy.ja.dmitriev.bank.src.person;

import info.kgeorgiy.ja.dmitriev.bank.src.account.Account;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface for working with person in bank.
 *
 * @author Dmitriev Vladislav (bitree2004@yandex.ru)
 * @since 21
 */
public interface Person extends Remote {
    /**
     * returns the person's name
     *
     * @return person's name
     */
    String firstName() throws RemoteException;

    /**
     * returns the person's last name
     *
     * @return person's last name
     */
    String secondName() throws RemoteException;

    /**
     * returns the person's passport
     *
     * @return person's passport
     */
    String passport() throws RemoteException;

    /**
     * Returns the bank account if it exists, otherwise null.
     *
     * @param subId account ID
     * @return account if it exists, otherwise null
     */
    Account getAccount(final String subId) throws RemoteException;

    /**
     * Creates an account if it did not exist.
     * In any case, it returns the current account linked to this {@code subId}.
     *
     * @param subId the ID for defining the account
     * @return the current account linked to this {@code subId}.
     */
    Account addAccount(final String subId) throws RemoteException;
}
