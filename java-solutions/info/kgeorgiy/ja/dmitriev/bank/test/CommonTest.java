package info.kgeorgiy.ja.dmitriev.bank.test;

import info.kgeorgiy.ja.dmitriev.bank.src.account.*;
import info.kgeorgiy.ja.dmitriev.bank.src.person.*;
import info.kgeorgiy.ja.dmitriev.bank.src.bank.*;

import static info.kgeorgiy.ja.dmitriev.bank.src.RemoteInformation.*;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.*;
import java.rmi.*;
import java.util.*;

/**
 * A class for reducing copy-paste in tests. Must be the ancestor of all tests.
 *
 * @author Dmitriev Vladislav (bitree2004@yandex.ru)
 * @since 21
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
public class CommonTest {
    /**
     * Strings for using in generating person's information.
     */
    protected static final List<String> NAMES =
            List.of("Dmitriev", "Vladislav", "Andrey", "Matveev", "Georgiy",
                    "Korneev", "Skakov", "Pavel", "المشاركين", "ويؤدي", "eläintieteellinen");
    /**
     * Random for generating passport and tests.
     */
    protected static final Random random = new Random(12);
    /**
     * All kinds of {@link PersonCharacteristics} obtained from {@code NAMES}.
     */
    protected static List<PersonCharacteristics> PERSON_CHARACTERISTICS_LIST = new ArrayList<>();
    /**
     * The bank to which requests for each test will go.
     */
    protected static Bank bank;

    /**
     * I count personal characteristics for tests once.
     */
    @BeforeAll
    public static void calcPersonCharacteristics() {
        NAMES.forEach(
                x -> NAMES.forEach(
                        y -> PERSON_CHARACTERISTICS_LIST.add(
                                new PersonCharacteristics(
                                        x,
                                        y,
                                        Integer.toString(random.nextInt())
                                )
                        )
                )
        );
    }

    /**
     * Creating a {@link java.rmi.registry.Registry} for tests to which everything will be linked.
     */
    @BeforeAll
    public static void startServer() throws IOException {
        try {
            LocateRegistry.createRegistry(BANK_PORT);
        } catch (final ExportException ignored) {
        }
        System.out.println("USAGE local registry:" + LocateRegistry.getRegistry(BANK_PORT));
    }

    /**
     * Before each test, need to restart the bank.
     * To delete all {@link RemotePerson} and their {@link Account}.
     */
    @BeforeEach
    public void startBank() throws IOException, NotBoundException {
        bank = new RemoteBank(BANK_PORT);
        try {
            UnicastRemoteObject.exportObject(bank, BANK_PORT);
            Naming.rebind(DEFAULT_BANK, bank);
            bank = (Bank) Naming.lookup(DEFAULT_BANK);
            System.out.println("Server started");
        } catch (final RemoteException e) {
            System.out.println("Cannot export object: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Assigns the {@code amount} to the {@code account} and makes the appropriate checks.
     *
     * @param account the account will be assigned to.
     * @param amount  the amount that will be assigned.
     */
    protected void checkCorrectAmount(
            final Account account,
            final int amount
    ) throws RemoteException {
        assert (amount >= 0);
        account.setAmount(amount);
        Assertions.assertEquals(amount, account.getAmount());
    }

    /**
     * Try to assign the negative {@code amount} to the {@code account}
     * and makes the appropriate checks.
     *
     * @param account the account will be assigned to.
     * @param amount  the amount that will be assigned.
     */
    protected void checkIncorrectAmount(
            final Account account,
            final int amount
    ) throws RemoteException {
        assert (amount < 0);
        Assertions.assertThrows(
                UnsupportedOperationException.class,
                () -> account.setAmount(amount)
        );
        Assertions.assertNotEquals(amount, account.getAmount());
    }

    /**
     * Checks that the added person has the expected values.
     *
     * @param person                Person to check.
     * @param personCharacteristics Characteristics of a person that are expected.
     */
    protected void checkPerson(
            final Person person,
            final PersonCharacteristics personCharacteristics
    ) throws RemoteException {
        Assertions.assertEquals(personCharacteristics.firstName(), person.firstName());
        Assertions.assertEquals(personCharacteristics.secondName(), person.secondName());
        Assertions.assertEquals(personCharacteristics.passport(), person.passport());
    }

    /**
     * Retrieves the {@link RemotePerson} from the bank and makes the appropriate checks.
     *
     * @param passport Passport for identifying a {@link RemotePerson}.
     * @return A {@link RemotePerson} who was pulled out of the bank.
     */
    protected Person getRemotePerson(final String passport) throws RemoteException {
        final var person = bank.getRemotePerson(passport);
        Assertions.assertNotNull(person);
        Assertions.assertEquals(passport, person.passport());
        Assertions.assertNotNull(person);
        return person;
    }

    /**
     * Retrieves the {@link LocalPerson} from the bank and makes the appropriate checks.
     *
     * @param passport Passport for identifying a {@link LocalPerson}.
     * @return A {@link LocalPerson} who was pulled out of the bank.
     */
    protected Person getLocalPerson(final String passport) throws RemoteException {
        final var person = bank.getLocalPerson(passport);
        Assertions.assertNotNull(person);
        Assertions.assertEquals(passport, person.passport());
        Assertions.assertNotNull(person);
        return person;
    }

    private String getAccountId(final String passport, final String subId) {
        return passport + ':' + subId;
    }

    /**
     * Retrieves the {@link RemotePerson} and his account.
     * Also performs all relevant checks.
     *
     * @param passport passport of the {@link RemotePerson}.
     * @param subId    account ID.
     * @return {@link Account} that was received.
     */
    protected Account getRemoteAccount(
            final String passport,
            final String subId
    ) throws RemoteException {
        final var person = getRemotePerson(passport);
        final var account = person.getAccount(subId);
        Assertions.assertEquals(getAccountId(passport, subId), account.getId());
        Assertions.assertNotNull(account);
        return account;
    }

    /**
     * Retrieves the {@link LocalPerson} and his account.
     * Also performs all relevant checks.
     *
     * @param passport passport of the {@link LocalPerson}.
     * @param subId    account ID.
     */
    protected void getLocalAccount(
            final String passport,
            final String subId
    ) throws RemoteException {
        final var person = getLocalPerson(passport);
        final var account = person.getAccount(subId);
        Assertions.assertNotNull(account);
    }

    /**
     * Adds a {@link Person} to the bank and makes the appropriate checks.
     *
     * @param personCharacteristics {@link PersonCharacteristics} of a person to create.
     * @return {@link Person} that was created
     */
    protected Person addPerson(
            final PersonCharacteristics personCharacteristics
    ) throws RemoteException {
        bank.createPerson(personCharacteristics);
        final var person = getRemotePerson(personCharacteristics.passport());
        checkPerson(person, personCharacteristics);
        return person;
    }

    /**
     * Adds an account based on the {@link Person}’s passport and {@link Account} ID.
     * Also performs all relevant checks.
     *
     * @param passport {@link Person}'s passport.
     * @param subId    {@link Account} ID.
     * @return The {@link Account} that was added.
     */
    protected Account addAccount(
            final String passport,
            final String subId
    ) throws RemoteException {
        final var person = getRemotePerson(passport);
        person.addAccount(subId);
        final var account = getRemoteAccount(passport, subId);
        Assertions.assertEquals(0, account.getAmount());
        return account;
    }

    /**
     * Checks that {@link info.kgeorgiy.ja.dmitriev.bank.src.Client#main(String...)} failed.
     *
     * @param passport The passport of the {@link Person} who was assigned to the function.
     */
    protected void checkInvalidClient(final String passport) throws RemoteException {
        Assertions.assertNull(bank.getRemotePerson(passport));
    }

    /**
     * Checks that {@link info.kgeorgiy.ja.dmitriev.bank.src.Client#main(String...)} success.
     *
     * @param passport The passport of the {@link Person} who was assigned to the function.
     * @param subId    The ID of the {@link Account} who was assigned to the function.
     * @param amount   The amount of the {@link Account} who was assigned to the function.
     */
    protected void checkCorrectClient(
            final String passport,
            final String subId,
            final int amount
    ) throws RemoteException {
        final Person person = getRemotePerson(passport);
        Assertions.assertNotNull(person);
        final Account account = getRemoteAccount(passport, subId);
        Assertions.assertEquals(amount, account.getAmount());
    }

    /**
     * Returns a pseudorandom, uniformly distributed int value between
     * 0 (inclusive) and the specified value (exclusive).
     *
     * @param right the upper bound (exclusive).
     * @return the next pseudorandom
     */
    protected static int getRandom(final int right) {
        return random.nextInt(0, right);
    }

    /**
     * Returns a random element in an list.
     *
     * @param list a {@link List} where they will take a random element.
     * @param <T>  list type
     * @return the random element of {@code list}
     */
    protected static <T> T getRandom(final List<T> list) {
        return list.get(getRandom(list.size()));
    }

    /**
     * Returns the current account value. He makes checks along the way.
     *
     * @param personCharacteristics Characteristics for {@link Person} recognition.
     * @param subId {@link Account} ID.
     * @return The amount in the found account.
     */
    protected int getAmount(
            final PersonCharacteristics personCharacteristics,
            final String subId
    ) throws RemoteException {
        final var person = bank.getRemotePerson(personCharacteristics.passport());
        if (person == null) {
            return 0;
        }
        final var account = person.getAccount(subId);
        if (account == null) {
            return 0;
        }
        return account.getAmount();
    }

    /**
     * Sends invoice money to the account. And does all the appropriate checks.
     *
     * @param from account from where it takes money
     * @param to account where money is sent
     * @param delta the amount that is sent
     */
    protected void checkTransfer(final Account from, final Account to, final int delta) throws RemoteException {
        final var oldAmount = from.getAmount();
        final var secondOldAmount = to.getAmount();
        from.transferTo(to, delta);
        Assertions.assertEquals(oldAmount - delta, from.getAmount());
        Assertions.assertEquals(secondOldAmount + delta, to.getAmount());
    }

    /**
     * Сhecks that money cannot be transferred.
     *
     * @param from account from where it takes money
     * @param to account where money is sent
     * @param delta the amount that is sent
     */
    protected void checkInvalidTransfer(final Account from, final Account to, final int delta) {
        Assertions.assertThrows(UnsupportedOperationException.class,
                                () -> from.transferTo(to, delta));
    }
}
