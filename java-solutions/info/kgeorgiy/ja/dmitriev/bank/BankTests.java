package info.kgeorgiy.ja.dmitriev.bank;

import info.kgeorgiy.ja.dmitriev.bank.test.MultiThreadsTests;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;

import java.io.PrintWriter;
/**
 * A class that contains all tests and provides a console interface for running test.
 *
 * @author Dmitriev Vladislav (bitree2004@yandex.ru)
 * @since 21
 */
public class BankTests extends MultiThreadsTests {
    /**
     * Start running test.
     *
     * @param args ignoring console args
     */
    public static void main(final String[] args) {
        final var launcherDiscoveryRequest = LauncherDiscoveryRequestBuilder.request()
                .selectors(DiscoverySelectors.selectClass(BankTests.class))
                .build();
        final var launcher = LauncherFactory.create();
        final var summaryGeneratingListener = new SummaryGeneratingListener();
        launcher.registerTestExecutionListeners(summaryGeneratingListener);
        final var testPlan = launcher.discover(launcherDiscoveryRequest);
        launcher.execute(testPlan);
        final var testExecutorSummary = summaryGeneratingListener.getSummary();
        testExecutorSummary.printTo(new PrintWriter(System.out));
        System.exit(testExecutorSummary.getFailures().isEmpty() ? 0 : 1);
    }
}
