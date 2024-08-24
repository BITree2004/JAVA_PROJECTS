package info.kgeorgiy.ja.dmitriev.i18n.test;

import org.junit.jupiter.api.Test;

import java.io.IOException;

public class IntegrationTest extends CurrencyTest {
    @Test
    public void test9_chinaTest() throws IOException {
        IntegrationTest("china", "ch");
    }

    @Test
    public void test10_bengalTest() throws IOException {
        IntegrationTest("bengal", "be");
    }

    @Test
    public void test11_bengalTest() throws IOException {
        IntegrationTest("arabic", "ar");
    }

    @Test
    public void test12_numberTest() throws IOException {
        IntegrationTest("number", "en-US");
    }

    @Test
    public void test13_dateTest() throws IOException {
        IntegrationTest("date", "en-US");
    }

    @Test
    public void test14_moneyTest() throws IOException {
        IntegrationTest("money", "en-US");
    }

    @Test
    public void test15_checkBritishEnglish() throws IOException {
        IntegrationTest("date", "en-UK");
    }

    @Test
    public void test16_checkAllInOneTest() throws IOException {
        IntegrationTest("all", "en-US");
    }
}
