package info.kgeorgiy.ja.dmitriev.bank.src.person;

import info.kgeorgiy.ja.dmitriev.bank.src.account.AbstractAccount;
import info.kgeorgiy.ja.dmitriev.bank.src.account.Account;

import java.util.concurrent.ConcurrentHashMap;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Map;

/**
 * Interface for working with person in bank.
 *
 * @author Dmitriev Vladislav (bitree2004@yandex.ru)
 * @since 21
 */
public abstract class AbstractPerson implements Person, Serializable {
    protected final PersonCharacteristics personStorage;
    protected final Map<String, Account> accountMap = new ConcurrentHashMap<>();

    public AbstractPerson(final PersonCharacteristics personStorage) {
        this.personStorage = personStorage;
    }

    public AbstractPerson(final AbstractPerson abstractPerson) throws RemoteException {
        personStorage = new PersonCharacteristics(
                abstractPerson.firstName(),
                abstractPerson.secondName(),
                abstractPerson.passport()
        );
        for (final var key : abstractPerson.accountMap.entrySet()) {
            accountMap.put(key.getKey(), new AbstractAccount(key.getValue()));
        }
    }


    @Override
    public String firstName() {
        return personStorage.firstName();
    }

    @Override
    public String secondName() {
        return personStorage.secondName();
    }

    @Override
    public String passport() {
        return personStorage.passport();
    }

    @Override
    public Account getAccount(final String subId) {
        return accountMap.get(getAccountId(subId));
    }

    @Override
    public Account addAccount(final String subId) throws RemoteException {
        final var accountId = getAccountId(subId);
        var res = accountMap.putIfAbsent(accountId, new AbstractAccount(accountId));
        if (res == null) {
            res = getAccount(subId);
            apply(res);
        }
        return res;
    }

    private String getAccountId(final String subId) {
        return passport() + ':' + subId;
    }

    protected abstract void apply(final Account account) throws RemoteException;
}
