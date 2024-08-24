package info.kgeorgiy.ja.dmitriev.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

import static info.kgeorgiy.ja.dmitriev.implementor.ImplFileAssistant.toFormat;

/**
 * The class provides an interface for generating code based on a token.
 *
 * @author Dmitriev Vladislav (bitree2004@yandex.ru)
 * @since 21
 */
/*package-private*/ class ImplGeneratorCode<E extends Exception> {
    /**
     * Char separator in this system
     */
    private static final String NEWLINE = System.lineSeparator();
    /**
     * Default modifier for class, methods and constructor
     */
    private static final String MODIFIER = "public ";
    /**
     * TAB, that will be used in class generating
     */
    // :NOTE: 4 spaces | fixed
    private static final String TAB = "    ";
    /**
     * A consumer to accept the code
     */
    private final ConsumerE<String, ? extends E> consumer;

    /**
     * This constructor allows the only way to create {@link ImplGeneratorCode}.
     * Afterward all the generated code will be transferred to {@code consumer}.
     *
     * @param consumer The consumer who will accept the code
     */
    /*package-private*/ ImplGeneratorCode(final ConsumerE<String, ? extends E> consumer) {
        this.consumer = consumer;
    }

    /**
     * Returns a {@link Constructor} of {@code token} that can be used.
     * If {@code token} is an interface, it returns null.
     *
     * @param token {@link Class} in which the constructor will be looked for
     * @return {@link Constructor} you can use
     * @throws ImplerException if the {@code token} does not have {@link Constructor} that can be used
     */
    // helper methods
    /*package-private*/
    static Constructor<?> getConstructor(final Class<?> token) throws ImplerException {
        if (Modifier.isInterface(token.getModifiers())) {
            return null;
        }
        for (final Constructor<?> constructor : token.getDeclaredConstructors()) {
            if (canBeImplemented(constructor, token.getPackage(), null)) {
                return constructor;
            }
        }
        // :NOTE: check in advance | fixed, write after check
        throw new ImplerException("Error: didn't find not private constructors!");
    }

    /**
     * Checks whether there is access to a type with a package {@code second}
     * and an access modifier {@code mod} from the package {@code first}.
     *
     * @param mod type access modifier
     * @param first package of type
     * @param second actual package, when it will check
     * @return whether there is access or not
     */
    private static boolean isInScope(final int mod, final Package first, final Package second) {
        return !Modifier.isPrivate(mod) &&
                // :NOTE: Object.equals | fixed
                (Modifier.isPublic(mod) || Modifier.isProtected(mod) || first.equals(second));
    }

    /**
     * Checks whether all method types are visible in package second.
     *
     * @param method the method that will check
     * @param second the package of {@code method}
     * @param returnType return type of {@code method}. If it's void, then it's null.
     * @return is it possible to override a {@code method}
     */
    private static boolean canBeImplemented(
            final Executable method,
            final Package second,
            final Class<?> returnType
    ) {
        final Package first = method.getDeclaringClass().getPackage();
        if (!isInScope(method.getModifiers(), first, second)) {
            return false;
        }
        if (Arrays.stream(method.getParameters())
                .anyMatch(
                        x -> !isInScope(x.getType().getModifiers(), first, second)
                )) {
            return false;
        }
        return returnType == null || isInScope(returnType.getModifiers(), first, second);
    }

    /**
     * Takes two {@link Method} with the same signature and returns the narrowest return type.
     * By signature author mean the name and arguments.
     *
     * @param a first {@link Method}
     * @param b second {@link Method}
     * @return {@link Method} with the narrowest type
     */
    public static Method merge(final Method a, final Method b) {
        return b.getReturnType().isAssignableFrom(a.getReturnType()) ? a : b;
    }

    /**
     * Add {@code methods} in {@code set}. If {@code} already contains,
     * then choose the one with the narrowest growing type.
     *
     * @param methods array of methods to add
     * @param map set for {@link ImplMethod} equivalence classes supports minimal function
     */
    private static void addMethods(final Method[] methods, final Map<ImplMethod, Method> map) {
        // added new code, was a bug
        Arrays.stream(methods)
                .map(ImplMethod::new)
                .forEach(x -> map.merge(
                        x,
                        x.getter(),
                        ImplGeneratorCode::merge
                        )
                );
    }

    /**
     * Extract all methods of {@code token}.
     *
     * @param token {@link Class} whose methods are extracted
     * @return {@link Collection} of all {@link Method} in {@code token}
     */
    private static Collection<Method> getAllMethods(Class<?> token) {
        final var res = new HashMap<ImplMethod, Method>();
        addMethods(token.getMethods(), res);
        while (token != null) {
            addMethods(token.getDeclaredMethods(), res);
            token = token.getSuperclass();
            // :NOTE: simplify | fixed
        }
        return res.values();
    }

    /**
     * Extract all methods of {@code token} that needed to implement.
     *
     * @param token {@link Class} whose methods are extracted
     * @return {@link Collection} of {@link Method} that need to be implemented
     * @throws ImplerException If a method cannot be overridden
     */
    // :NOTE: formatting | fixed
    /*package-private*/
    static Collection<Method> getAllAbstractMethods(final Class<?> token) throws ImplerException {
        final var res = getAllMethods(token);
        final var result = res.stream()
                .filter(x -> Modifier.isAbstract(x.getModifiers())).toList();
        // :NOTE: too late | fixed, write after check
        if (result.
                stream().
                anyMatch(x -> !canBeImplemented(x, token.getPackage(), x.getReturnType()))
        ) {
            throw new ImplerException("Error: can't realize abstract method!");
        }
        return result;
    }

    /**
     * Transfers all {@code strings} to the consumer.
     *
     * @param strings an array of strings that should be transferred to the consumer
     * @throws E if {@link ConsumerE#write(Object)} throws exceptions
     */
    private void writeAll(final String... strings) throws E {
        for (final var x : strings) {
            consumer.write(toFormat(x));
        }
    }

    /**
     * Returns the name of {@code token} in the format "%sImpl".
     *
     * @param token {@link Class} whose name will be converted
     * @return the name in format
     */
    // getter for source code
    /*package-private*/ static String getImplName(final Class<?> token) {
        return String.format("%sImpl", token.getSimpleName());
    }

    /**
     * Generates exception code for a {@code executable}.
     *
     * @param executable {@link Executable} for which a list of exceptions will be generated
     * @return {@link String} code that was generated
     */
    private static String getThrows(final Executable executable) {
        final Class<?>[] arr = executable.getExceptionTypes();
        if (arr.length == 0) {
            return "";
        }
        return String.format(" throws %s",
                             Arrays.stream(arr)
                                     .map(Class::getCanonicalName)
                                     .collect(Collectors.joining(", "))
        );
    }

    /**
     * Generates arguments code for a {@code executable}.
     *
     * @param executable {@link Executable} for which a list of arguments will be generated
     * @return {@link String} code that was generated
     */
    private static String getArguments(final Executable executable) {
        return Arrays.stream(executable.getParameters())
                .map(x -> x.getType().getCanonicalName() + " " + x.getName())
                .collect(Collectors.joining(", ", "(", ")"));
    }

    /**
     * Generates the body of the {@code constructor},
     * delegating all arguments to {@code constructor}.
     *
     * @param constructor {@link Constructor} to which everything will be delegated
     * @return {@link String} code that was generated
     */
    private static String getConstructorBody(final Constructor<?> constructor) {
        return Arrays.stream(constructor.getParameters())
                .map(Parameter::getName)
                .collect(Collectors.joining(", ", "super(", ");"));
    }

    /**
     * Generates a default value of {@code returnType}.
     *
     * @param returnType {@link Class} for which the default value is generated.
     * @return default value of {@code returnType}
     */
    private static String getBodyFunction(final Class<?> returnType) {
        if (returnType == void.class) {
            // :NOTE: ?? | fixed
            return "";
        }
        final String res;
        // :NOTE: .equals | fixed
        if (returnType == boolean.class) {
            res = "false";
        } else if (returnType.isPrimitive()) {
            res = "0";
        } else {
            res = "null";
        }
        return String.format(" return %s; ", res);
    }

    /**
     * Passes the code generated for the {@code executable} to the {@code consumer}.
     *
     * @param executable {@link Executable} for which code is generated.
     * @param returnedType {@link Class} of return type {@code executable}.
     * If it returns nothing, it accepts null.
     * @param name the name of {@code executable} that will be pass to {@code consumer}.
     * @param body the body of {@code executable} that will be pass to {@code consumer}.
     * @throws E If write to the {@code consumer} throws an exception
     */
    // functions for writing to a file
    private void writeFunction(
            final Executable executable,
            final Class<?> returnedType,
            final String name,
            final String body
    ) throws E {
        writeAll(TAB, MODIFIER);
        if (returnedType != null) {
            writeAll(String.format("%s ", returnedType.getCanonicalName()));
        }
        writeAll(
                name,
                getArguments(executable),
                getThrows(executable),
                " {",
                body,
                "}",
                NEWLINE
        );
    }

    /**
     * Passes the code generated for the {@code constructor} to the {@code consumer}.
     *
     * @param constructor {@link Constructor} for which code is generated.
     * @throws E If write to the {@code consumer} throws an exception
     */
    private void writeConstructor(final Constructor<?> constructor) throws E {
        writeFunction(
                constructor,
                null,
                getImplName(constructor.getDeclaringClass()),
                getConstructorBody(constructor)
        );
    }

    /**
     * Passes the code generated for the {@code method} to the {@code consumer}.
     *
     * @param method {@link Method} for which code is generated
     * @throws E If write to the {@code consumer} throws an exception
     */
    private void writeMethod(final Method method) throws E {
        writeFunction(
                method,
                method.getReturnType(),
                method.getName(),
                getBodyFunction(method.getReturnType())
        );
    }

    /**
     * Passes the code generated for package of {@code token} to the {@code consumer}.
     *
     * @param token {@link Class} for which the header is generated
     * @throws E If write to the {@code consumer} throws an exception
     */
    private void writeHeader(final Class<?> token) throws E {
        final var packageName = token.getPackageName();
        if (!packageName.isEmpty()) {
            writeAll(String.format("package %s;%n%n", packageName));
        }
    }

    /**
     * Passes the code generated of begin {@code token} to the {@code consumer}.
     *
     * @param token {@link Class} for which the beginning is generated
     * @throws E If write to the {@code consumer} throws an exception
     */
    private void writeBeginClass(final Class<?> token) throws E {
        // :NOTE: single String.format | fixed
        writeAll(MODIFIER,
                 String.format(
                         "class %s %s %s {%n",
                         getImplName(token),
                         Modifier.isInterface(token.getModifiers()) ? "implements" : "extends",
                         token.getCanonicalName()
                 )
        );
    }

    /**
     * Passes the code generated of end any {@link Class} to the {@code consumer}.
     *
     * @throws E If write to the {@code consumer} throws an exception
     */
    private void writeEndClass() throws E {
        writeAll("}", NEWLINE);
    }


    /**
     * Passes the code generated of {@code token} with constructor {@code constructor}
     * and methods {@code methods} to the {@code consumer}.
     *
     * @param token {@link Class} for which code is generated
     * @param constructor {@link Constructor} for generating
     * @param methods {@link Collection} of {@link Method} for generating
     * @throws E If write to the {@code consumer} throws an exception
     */
    /*package-private*/ void writeCodeSource(
            final Class<?> token,
            final Constructor<?> constructor,
            final Collection<Method> methods
    ) throws E {
        writeHeader(token);
        writeBeginClass(token);
        if (constructor != null) {
            writeConstructor(constructor);
        }
        for (final var method : methods) {
            writeMethod(method);
        }
        writeEndClass();
    }
}
