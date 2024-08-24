package info.kgeorgiy.ja.dmitriev.bank.test;

import info.kgeorgiy.ja.dmitriev.bank.src.person.Person;
import org.junit.jupiter.api.*;

import java.rmi.RemoteException;
import java.util.function.Function;
import java.util.concurrent.*;
import java.util.*;

/**
 * Class for multi-threaded library testing.
 *
 * @author Dmitriev Vladislav (bitree2004@yandex.ru)
 * @since 21
 */
public class MultiThreadsTests extends MainTests {
    private static ExecutorService executor;
    private static final int THREADS = 10;

    /**
     * Before you start, you need to create a thread pool.
     */
    @BeforeAll
    public static void start() {
        executor = Executors.newFixedThreadPool(THREADS);
    }

    /**
     * At the end, you need to close the thread pool.
     */
    @AfterAll
    public static void end() {
        if (executor != null) {
            executor.close();
        }
    }

    private void tackForThread(
            final String passport,
            final String subId,
            final Function<Integer, Integer> amountExpectedFunction,
            final ExceptionFunction<String, Person, RemoteException> builder1,
            final ExceptionFunction<String, Person, RemoteException> builder2
    ) throws RemoteException {
        final int newAmount = getRandom(Integer.MAX_VALUE);
        final Integer expectedAmount = amountExpectedFunction.apply(newAmount);
        final var person1 = builder1.apply(passport);
        Assertions.assertNotNull(person1);
        final var account1 = person1.addAccount(subId);
        final var person2 = builder2.apply(passport);
        Assertions.assertNotNull(person2);
        final var account2 = person2.getAccount(subId);
        checkCorrectAmount(account1, newAmount);
        if (expectedAmount == null) {
            Assertions.assertNull(account2);
        } else {
            Assertions.assertNotNull(account2);
            Assertions.assertEquals(expectedAmount, account2.getAmount());
        }
    }

    private void singleThreadTwoPerson(
            final Function<Integer, Integer> amountExpectedFunction,
            final ExceptionFunction<String, Person, RemoteException> builder1,
            final ExceptionFunction<String, Person, RemoteException> builder2
    ) throws RemoteException {
        for (final var personData : PERSON_CHARACTERISTICS_LIST) {
            bank.createPerson(personData);
            for (final var subId : NAMES) {
                tackForThread(personData.passport(),
                              subId,
                              amountExpectedFunction,
                              builder1,
                              builder2
                );
            }
        }
    }

    private void multiThreadTwoPerson(
            final Function<Integer, Integer> amountExpectedFunction,
            final ExceptionFunction<String, Person, RemoteException> builder1,
            final ExceptionFunction<String, Person, RemoteException> builder2
    ) throws RemoteException, ExecutionException, InterruptedException {
        final List<Future<Void>> futures = new ArrayList<>();
        for (final var personData : PERSON_CHARACTERISTICS_LIST) {
            bank.createPerson(personData);
            for (final var subId : NAMES) {
                futures.add(executor.submit(() -> {
                    tackForThread(personData.passport(),
                                  subId,
                                  amountExpectedFunction,
                                  builder1,
                                  builder2
                    );
                    return null;
                }));
            }
        }
        for (final Future<Void> x : futures) {
            x.get();
        }
    }

    /**
     * Checks the consistency of RemotePerson and RemotePerson in a single thread.
     */
    @Test
    public void remoteAndRemoteSingleThread() throws RemoteException {
        singleThreadTwoPerson(Function.identity(), bank::getRemotePerson, bank::getRemotePerson);
    }

    /**
     * Checks the consistency of RemotePerson and LocalPerson in a single thread.
     */
    @Test
    public void remoteAndLocalSingleThread() throws RemoteException {
        singleThreadTwoPerson((x) -> 0, bank::getRemotePerson, bank::getLocalPerson);
    }

    /**
     * Checks the consistency of LocalPerson and RemotePerson in a single thread.
     */
    @Test
    public void localAndRemoteSingleThread() throws RemoteException {
        singleThreadTwoPerson((x) -> null, bank::getLocalPerson, bank::getRemotePerson);
    }

    /**
     * Checks the consistency of LocalPerson and LocalPerson in a single thread.
     */
    @Test
    public void localAndLocalSingleThread() throws RemoteException {
        singleThreadTwoPerson((x) -> null, bank::getLocalPerson, bank::getLocalPerson);
    }

    /**
     * Checks the consistency of RemotePerson and RemotePerson in a multi thread.
     */
    @Test
    public void remoteAndRemoteMultiThread() throws RemoteException, ExecutionException, InterruptedException {
        multiThreadTwoPerson(Function.identity(), bank::getRemotePerson, bank::getRemotePerson);
    }

    /**
     * Checks the consistency of RemotePerson and LocalPerson in a multi thread.
     */
    @Test
    public void remoteAndLocalMultiThread() throws RemoteException, ExecutionException, InterruptedException {
        multiThreadTwoPerson((x) -> 0, bank::getRemotePerson, bank::getLocalPerson);
    }

    /**
     * Checks the consistency of LocalPerson and RemotePerson in a multi thread.
     */
    @Test
    public void localAndRemoteMultiThread() throws RemoteException, ExecutionException, InterruptedException {
        multiThreadTwoPerson((x) -> null, bank::getLocalPerson, bank::getRemotePerson);
    }

    /**
     * Checks the consistency of LocalPerson and LocalPerson in a multi thread.
     */
    @Test
    public void localAndLocalMultiThread() throws RemoteException, ExecutionException, InterruptedException {
        multiThreadTwoPerson((x) -> null, bank::getLocalPerson, bank::getLocalPerson);
    }


    /**
     * Multiple amount changes in multithreading.
     */
    @Test
    public void multiAmount() throws RemoteException, ExecutionException, InterruptedException {
        final List<Future<Integer>> futures = new ArrayList<>();
        for (final var personData : PERSON_CHARACTERISTICS_LIST) {
            addPerson(personData);
            for (final var subId : NAMES) {
                final var account = addAccount(personData.passport(), subId);
                final var pseudoThreadNumber = getRandom(THREADS);
                futures.add(executor.submit(() -> {
                    account.setAmount(pseudoThreadNumber);
                    return account.getAmount();
                }));
            }
        }
        for (final Future<Integer> x : futures) {
            Assertions.assertTrue(0 <= x.get() && x.get()< THREADS);
        }
    }
}
