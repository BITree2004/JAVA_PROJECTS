package info.kgeorgiy.ja.dmitriev.bank.src.account;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Objects;

/**
 * Is the default implementation of the {@link Account}
 *
 * @author Dmitriev Vladislav (bitree2004@yandex.ru)
 * @since 21
 */
public class AbstractAccount implements Account, Serializable {
    private final String id;
    private int amount;

    /**
     * Default constructor to create new account without money with {@code id}.
     *
     * @param id identifier of account
     */
    public AbstractAccount(final String id) {
        this(0, id);
    }

    /**
     * Constructor to create new account with {@code amount} and {@code id}.
     *
     * @param amount amount of money in new account
     * @param id account identifier in new account
     */
    public AbstractAccount(final int amount, final String id) {
        Objects.requireNonNull(id);
        this.amount = amount;
        this.id = id;
    }

    /**
     * A constructor for creating a new account exactly like {@link Account}.
     *
     * @param account account for copying
     * @throws RemoteException If the {@code account} getters threw exceptions.
     */
    public AbstractAccount(final Account account) throws RemoteException {
        this(account.getAmount(), account.getId());
    }


    @Override
    public String getId() {
        return id;
    }

    @Override
    public synchronized int getAmount() {
        return amount;
    }

    @Override
    public synchronized void setAmount(final int amount) {
        if (amount < 0) {
            throw new UnsupportedOperationException("Amount must be positive integer!");
        }
        this.amount = amount;
    }

    @Override
    public synchronized void addAmount(final int delta) {
        setAmount(getAmount() + delta); // NOTE: copypaste
    }

    @Override
    public void transferTo( // NOTE: don't work
            final Account to,
            final int delta
    ) throws RemoteException {
        addAmount(-delta);
        to.addAmount(delta);
    }
}
