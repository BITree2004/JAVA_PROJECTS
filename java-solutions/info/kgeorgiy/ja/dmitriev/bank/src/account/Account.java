package info.kgeorgiy.ja.dmitriev.bank.src.account;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface for working with accounts. Must be attached to a {@link info.kgeorgiy.ja.dmitriev.bank.src.person.Person}.
 *
 * @author Dmitriev Vladislav (bitree2004@yandex.ru)
 * @since 21
 */
public interface Account extends Remote {
    /** Returns account identifier. */
    String getId() throws RemoteException;
    /** Returns amount of money in the account. */
    int getAmount() throws RemoteException;
    /** Sets amount of money in the account. */
    void setAmount(final int amount) throws RemoteException;
    /** Adds amount of money in the account. */
    void addAmount(final int delta) throws RemoteException;
    /**
     * Sends invoice money to the account.
     *
     * @param to account where money is sent
     * @param delta the amount that is sent
     */
    void transferTo(final Account to, final int delta) throws RemoteException;
}
