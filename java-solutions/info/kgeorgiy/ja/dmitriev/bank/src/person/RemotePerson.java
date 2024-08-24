package info.kgeorgiy.ja.dmitriev.bank.src.person;

import info.kgeorgiy.ja.dmitriev.bank.src.account.Account;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Implementation of the {@link AbstractPerson} interface.
 * Invoice changes made through this class are immediately applied globally.
 *
 * @author Dmitriev Vladislav (bitree2004@yandex.ru)
 * @since 21
 */
public class RemotePerson extends AbstractPerson implements Remote {
    private final int port;

    /**
     * The only way to create a remote person. Initially he has 0 accounts.
     *
     * @param characteristics personal information of a person
     * @param port port for {@link UnicastRemoteObject#exportObject(Remote, int)}.
     */
    public RemotePerson(final PersonCharacteristics characteristics, final int port) {
        super(new PersonCharacteristics(
                characteristics.firstName(),
                characteristics.secondName(),
                characteristics.passport()
              )
        );
        this.port = port;
    }

    @Override
    protected void apply(final Account account) throws RemoteException {
        UnicastRemoteObject.exportObject(account, port);
    }
}
