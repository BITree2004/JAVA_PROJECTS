package info.kgeorgiy.ja.dmitriev.bank.src.bank;

import info.kgeorgiy.ja.dmitriev.bank.src.person.Person;
import info.kgeorgiy.ja.dmitriev.bank.src.person.PersonCharacteristics;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface for working with bank.
 *
 * @author Dmitriev Vladislav (bitree2004@yandex.ru)
 * @since 21
 */
public interface Bank extends Remote {
    /**
     * Links a person to the bank according to his {@code personCharacteristics}.
     *
     * @param personCharacteristics personal data of the new client
     * @return new client
     */
    Person createPerson(final PersonCharacteristics personCharacteristics) throws RemoteException;

    /**
     * Returns a {@link info.kgeorgiy.ja.dmitriev.bank.src.person.LocalPerson} from the client list. If it is not there, it will return null.
     *
     * @param passport a person’s passport to identify him
     * @return {@link info.kgeorgiy.ja.dmitriev.bank.src.person.LocalPerson}
     */
    Person getLocalPerson(final String passport) throws RemoteException;

    /**
     * Returns a {@link info.kgeorgiy.ja.dmitriev.bank.src.person.RemotePerson} from the client list. If it is not there, it will return null.
     *
     * @param passport a person’s passport to identify him
     * @return {@link info.kgeorgiy.ja.dmitriev.bank.src.person.RemotePerson}
     */
    Person getRemotePerson(final String passport) throws RemoteException;
}