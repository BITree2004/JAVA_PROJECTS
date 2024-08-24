/**
 * This module is solutions to
 * <a href="https://www.kgeorgiy.info/courses/java-advanced/homeworks.html#implementor-javadoc">
 *     homework website
 *     </a>.
 *
 * @author Dmitriev Vladislav (bitree2004@yandex.ru)
 */
module info.kgeorgiy.ja.dmitriev {
    // requires standard library java
    requires java.compiler;
    requires java.rmi;
    // requires kgeorgiy's library
    requires info.kgeorgiy.java.advanced.student;
    requires info.kgeorgiy.java.advanced.implementor;
    requires info.kgeorgiy.java.advanced.iterative;
    requires info.kgeorgiy.java.advanced.mapper;
    requires info.kgeorgiy.java.advanced.crawler;
    requires info.kgeorgiy.java.advanced.hello;
    requires org.junit.jupiter.api;
    requires org.junit.jupiter.engine;
    requires org.junit.platform.commons;
    requires org.junit.platform.launcher;
    requires org.junit.platform.engine;


    exports info.kgeorgiy.ja.dmitriev.bank.src.account;
    exports info.kgeorgiy.ja.dmitriev.bank.src.person;
    exports info.kgeorgiy.ja.dmitriev.bank.src.bank;
    exports info.kgeorgiy.ja.dmitriev.bank;
    exports info.kgeorgiy.ja.dmitriev.bank.src;

    opens info.kgeorgiy.ja.dmitriev.bank.src.person;
    opens info.kgeorgiy.ja.dmitriev.i18n.test;
}
