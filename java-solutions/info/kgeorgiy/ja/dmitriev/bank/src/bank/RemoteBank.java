package info.kgeorgiy.ja.dmitriev.bank.src.bank;

import info.kgeorgiy.ja.dmitriev.bank.src.person.*;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of the {@link Bank} interface.
 *
 * @author Dmitriev Vladislav (bitree2004@yandex.ru)
 * @since 21
 */
public class RemoteBank implements Bank {
    private final Map<String, Person> personMap = new ConcurrentHashMap<>();
    private final int port;

    /**
     * The only way to create a bank.
     *
     * @param port the port to export {@link RemotePerson} on
     */
    public RemoteBank(final int port) {
        this.port = port;
    }

    @Override
    public Person createPerson(final PersonCharacteristics characteristics) throws RemoteException {
        final var newRemotePerson = new RemotePerson(characteristics, port);
        var remotePerson = personMap.putIfAbsent(characteristics.passport(), newRemotePerson);
        if (remotePerson == null) {
            remotePerson = newRemotePerson;
            UnicastRemoteObject.exportObject(remotePerson, port);
        }
        return remotePerson;
    }

    @Override
    public Person getLocalPerson(final String passport) throws RemoteException {
        if (getRemotePerson(passport) == null) {
            return null;
        }
        return new LocalPerson((RemotePerson) getRemotePerson(passport));
    }

    @Override
    public Person getRemotePerson(final String passport) throws RemoteException {
        return personMap.get(passport);
    }
}
