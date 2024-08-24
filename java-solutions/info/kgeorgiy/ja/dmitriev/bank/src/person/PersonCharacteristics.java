package info.kgeorgiy.ja.dmitriev.bank.src.person;

import java.io.Serializable;

/**
 * Personal information for each person.
 *
 * @param firstName the name of the person.
 * @param secondName the last name of the person.
 * @param passport a person's passport.
 * @author Dmitriev Vladislav (bitree2004@yandex.ru)
 * @since 21
 */
public record PersonCharacteristics(String firstName, String secondName, String passport) implements Serializable {
}
