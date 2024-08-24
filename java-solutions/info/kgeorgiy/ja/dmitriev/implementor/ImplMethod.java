package info.kgeorgiy.ja.dmitriev.implementor;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.HashMap;


/**
 * This class is a wrapper over a {@link Method}.
 * And allows it to be used as a key for {@link HashMap}.
 *
 * @author Dmitriev Vladislav (bitree2004@yandex.ru)
 * @since 21
 */
/*package-private*/ class ImplMethod {
    /**
     * a {@link Method} whose wrapper class is
     */
    private final Method method;
    /**
     * the name of {@code method}
     */
    private final String name;
    /**
     * the list of {@code method} argument names
     */
    private final List<String> parameters;
    /**
     * the hash of {@code method}
     */
    private final int hash;

    /**
     * The only way to create {@link ImplMethod}.
     * Create class by {@code method}.
     *
     * @param method a {@link Method} whose wrapper class is
     */
    public ImplMethod(final Method method) {
        this.method = method;
        this.name = method.getName();
        // :NOTE: formatting | fixed
        this.parameters = Arrays.stream(method.getParameters())
                .map(Parameter::getType)
                .map(Class::getCanonicalName)
                .toList();
        this.hash = Objects.hash(name, parameters);
    }

    /**
     * Return {@link Method} whose wrapper class is
     * @return {@link Method} is {@code method}
     */
    public Method getter() {
        return method;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        // :NOTE: Objects.equals for array | fixed, as list
        final ImplMethod other = (ImplMethod) obj;
        return name.equals(other.name) && parameters.equals(other.parameters);
    }
}
