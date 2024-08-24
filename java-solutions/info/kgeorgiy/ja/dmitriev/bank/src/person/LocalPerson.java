package info.kgeorgiy.ja.dmitriev.bank.src.person;

import info.kgeorgiy.ja.dmitriev.bank.src.account.Account;

import java.rmi.RemoteException;

/**
 * Implementation of the {@link AbstractPerson} interface.
 * Invoice changes made through this class are immediately applied only in this!
 *
 * @author Dmitriev Vladislav (bitree2004@yandex.ru)
 * @since 21
 */
public class LocalPerson extends AbstractPerson {
    /**
     * The only way to create a local person.
     * Standard way create from {@link RemotePerson}.
     *
     * @param remotePerson the person from whom they will receive all the information
     */
    public LocalPerson(final AbstractPerson remotePerson) throws RemoteException {
        super(remotePerson);
    }


    @Override
    public Account addAccount(final String subId) {
        try {
            return super.addAccount(subId);
        } catch (final RemoteException ignored) {
            throw new AssertionError("Impossible throw RemoteException for LocalPerson!");
        }
    }

    @Override
    protected void apply(final Account account) {}
}
