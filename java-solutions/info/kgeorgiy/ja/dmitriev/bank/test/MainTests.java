package info.kgeorgiy.ja.dmitriev.bank.test;

import info.kgeorgiy.ja.dmitriev.bank.src.Client;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.rmi.RemoteException;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * A class that contains the main tests that check correctness in a single thread.
 *
 * @author Dmitriev Vladislav (bitree2004@yandex.ru)
 * @since 21
 */
public class MainTests extends CommonTest {
    /**
     * How many tests will be run.
     */
    protected int NUMBER_OF_ITERATION = 10;

    /**
     * Checks the assignment of a new amount to the account.
     */
    @Test
    public void test1_checkSetAmount() throws RemoteException {
        final var personCharacteristics = getRandom(PERSON_CHARACTERISTICS_LIST);
        addPerson(personCharacteristics);
        final var account = addAccount(personCharacteristics.passport(), getRandom(NAMES));
        for (int i = 0; i < 10; ++i) {
            checkCorrectAmount(account, i);
        }
        checkCorrectAmount(account, Integer.MAX_VALUE);
        for (int i = 1; i < 10; ++i) {
            checkIncorrectAmount(account, -i);
        }
        checkIncorrectAmount(account, Integer.MIN_VALUE);
    }

    /**
     * Checks the addition of new people.
     */
    @Test
    public void test2_checkAddPerson() throws RemoteException {
        for (final var personCharacteristics : PERSON_CHARACTERISTICS_LIST.stream()
                .skip(PERSON_CHARACTERISTICS_LIST.size() / 2)
                .toList()
        ) {
            addPerson(personCharacteristics);
            Assertions.assertNotNull(bank.getRemotePerson(personCharacteristics.passport()));
        }
        for (final var personCharacteristics : PERSON_CHARACTERISTICS_LIST.stream()
                .limit(PERSON_CHARACTERISTICS_LIST.size() / 2)
                .toList()
        ) {
            Assertions.assertNull(bank.getRemotePerson(personCharacteristics.passport()));
            Assertions.assertNull(bank.getLocalPerson(personCharacteristics.passport()));
        }
    }

    /**
     * Checks the addition of new accounts.
     */
    @Test
    public void test3_checkAddAccountTest() throws RemoteException {
        for (final var personCharacteristics : PERSON_CHARACTERISTICS_LIST) {
            addPerson(personCharacteristics);
            final var person = getRemotePerson(personCharacteristics.passport());
            final var subId = getRandom(NAMES);
            final var account = person.getAccount(subId);
            Assertions.assertNull(account);
            person.addAccount(subId);
            getRemoteAccount(personCharacteristics.passport(), subId);
        }
    }

    /**
     * Checks the functionality of the {@link Client#main(String...)} function.
     */
    @Test
    public void test4_checkClient() throws RemoteException {
        for (int i = 0; i < NUMBER_OF_ITERATION; i++) {
            final var personCharacteristics = getRandom(PERSON_CHARACTERISTICS_LIST);
            final int amount = getRandom(Integer.MAX_VALUE);
            final var subId = getRandom(NAMES);
            final int delta = amount - getAmount(personCharacteristics, subId);
            Client.main(personCharacteristics.firstName(),
                        personCharacteristics.secondName(),
                        personCharacteristics.passport(),
                        subId,
                        Integer.toString(delta)
            );
            checkCorrectClient(personCharacteristics.passport(), subId, amount);
        }
    }

    /**
     * Checks that the client class supports foreign numbers.
     */
    @Test
    public void test5_checkClientCrazyNumbers() throws RemoteException {
        for (int i = 0; i < NUMBER_OF_ITERATION; i++) {
            final var personCharacteristics = getRandom(PERSON_CHARACTERISTICS_LIST);
            final int delta = getRandom(999);
            final var subId = getRandom(NAMES);
            final int amount = delta + getAmount(personCharacteristics, subId);
            Client.main(personCharacteristics.firstName(),
                        personCharacteristics.secondName(),
                        personCharacteristics.passport(),
                        subId,
                        NumberFormat.getNumberInstance(Locale.forLanguageTag("fa")).format(delta)
            );
            checkCorrectClient(personCharacteristics.passport(), subId, amount);
        }
    }

    /**
     * Checks that the bank is not a lousy SQL table.
     */
    @Test
    public void test6_checkHuckClient() throws RemoteException {
        for (int i = 0; i < NUMBER_OF_ITERATION; i++) {
            final var personCharacteristics = getRandom(PERSON_CHARACTERISTICS_LIST);
            final int delta = getRandom(999);
            final var subId = " ;;;;;;   DROP  TABLE   USERS ;;;   ";
            final int amount = delta + getAmount(personCharacteristics, subId);
            Client.main(personCharacteristics.firstName(),
                        personCharacteristics.secondName(),
                        personCharacteristics.passport(),
                        subId,
                        NumberFormat.getNumberInstance(Locale.forLanguageTag("fa")).format(delta)
            );
            checkCorrectClient(personCharacteristics.passport(), subId, amount);
        }
    }

    /**
     * Checks that the client rejects large numbers.
     */
    @Test
    public void test7_tooLargeDelta() throws RemoteException {
        final var personCharacteristics = getRandom(PERSON_CHARACTERISTICS_LIST);
        final var delta = "9".repeat(32);
        final var subId = getRandom(NAMES);
        Client.main(personCharacteristics.firstName(),
                    personCharacteristics.secondName(),
                    personCharacteristics.passport(),
                    subId,
                    delta
        );
        checkInvalidClient(personCharacteristics.passport());
    }

    /**
     * Checks that the client does not reject numbers.
     */
    @Test
    public void test8_notInteger() throws RemoteException {
        final var personCharacteristics = getRandom(PERSON_CHARACTERISTICS_LIST);
        final var delta = "it's not a number";
        final var subId = getRandom(NAMES);
        Client.main(personCharacteristics.firstName(),
                    personCharacteristics.secondName(),
                    personCharacteristics.passport(),
                    subId,
                    delta
        );
        checkInvalidClient(personCharacteristics.passport());
    }

    /**
     * Checks that the client rejects numbers in quotes.
     */
    @Test
    public void test9_numberInQuotes() throws RemoteException {
        final var personCharacteristics = getRandom(PERSON_CHARACTERISTICS_LIST);
        final var delta = "\"21441\"";
        final var subId = getRandom(NAMES);
        Client.main(personCharacteristics.firstName(),
                    personCharacteristics.secondName(),
                    personCharacteristics.passport(),
                    subId,
                    delta
        );
        checkInvalidClient(personCharacteristics.passport());
    }

    /**
     * Checks that the client rejects nulls.
     */
    @Test
    public void test10_nullArgsInClient() {
        Assertions.assertThrows(NullPointerException.class,
                                () -> Client.main(null, null, null, null, null)
        );
    }

    /**
     * Checks that local persons are unlinked.
     */
    @Test
    public void test11_checkLocalPerson() throws RemoteException {
        for (final var personCharacteristics : PERSON_CHARACTERISTICS_LIST) {
            addPerson(personCharacteristics);
            final var person = getRemotePerson(personCharacteristics.passport());
            final var localPerson = getLocalPerson(personCharacteristics.passport());
            final var subId = getRandom(NAMES);
            var account = localPerson.getAccount(subId);
            Assertions.assertNull(account);
            person.addAccount(subId);
            account = localPerson.getAccount(subId);
            Assertions.assertNull(account);
            getLocalPerson(personCharacteristics.passport());
            getLocalAccount(personCharacteristics.passport(), subId);
        }
    }

    /**
     * Checks that remote persons are attached.
     */
    @Test
    public void test12_checkRemotePerson() throws RemoteException {
        for (final var personCharacteristics : PERSON_CHARACTERISTICS_LIST) {
            addPerson(personCharacteristics);
            var person = getRemotePerson(personCharacteristics.passport());
            final var subId = getRandom(NAMES);
            person.addAccount(subId);
            person = getRemotePerson(personCharacteristics.passport());
            getRemoteAccount(person.passport(), subId);
        }
    }


    /**
     * Checks the transfer from account to account.
     */
    @Test
    public void test13_transfersOfAmount() throws RemoteException {
        for (final var personCharacteristics : PERSON_CHARACTERISTICS_LIST) {
            addPerson(personCharacteristics);
        }
        for (var i = 0; i < PERSON_CHARACTERISTICS_LIST.size(); ++i) {
            final var firstPerson = getRemotePerson(PERSON_CHARACTERISTICS_LIST.get(i).passport());
            final var firstSubId = getRandom(NAMES);
            final var firstAccount = firstPerson.addAccount(firstSubId);
            final var amount = 1 + getRandom(100);
            final var delta = getRandom(amount);
            final var secondPersonCharacteristics = getRandom(PERSON_CHARACTERISTICS_LIST);
            if (secondPersonCharacteristics.equals(PERSON_CHARACTERISTICS_LIST.get(i))) {
                continue;
            }
            final var secondSubId = getRandom(NAMES);
            final var secondPerson = getRemotePerson(secondPersonCharacteristics.passport());
            final var secondAccount = secondPerson.addAccount(secondSubId);
            firstAccount.setAmount(amount);
            checkTransfer(firstAccount, secondAccount, delta);
        }
    }

    /**
     * Checks that money cannot be transferred.
     */
    @Test
    public void test14_failedTransfersOfAmount() throws RemoteException {
        final var firstPerson = addPerson(getRandom(PERSON_CHARACTERISTICS_LIST));
        final var secondPerson = addPerson(getRandom(PERSON_CHARACTERISTICS_LIST));
        final var firstAccount = addAccount(firstPerson.passport(), "first");
        final var secondAccount = addAccount(secondPerson.passport(), "second");
        final var amount = getRandom(Integer.MAX_VALUE);
        firstAccount.setAmount(amount - 1);
        checkInvalidTransfer(firstAccount, secondAccount, amount);
        firstAccount.setAmount(amount);
        checkTransfer(firstAccount, secondAccount, amount);
    }
}
