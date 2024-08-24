package info.kgeorgiy.ja.dmitriev.student;

import info.kgeorgiy.java.advanced.student.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*package-private*/ class AbstractStudentDB {
    protected static final Comparator<Student> COMPARATOR_STUDENTS_BY_NAME =
            Comparator.comparing(Student::getLastName).thenComparing(
                    Student::getFirstName).thenComparing(
                    Comparator.reverseOrder());

    protected static final Comparator<Group> COMPARATOR_GROUP_BY_NAME =
            Comparator.comparing(Group::getName);

    protected static final Comparator<Group> COMPARATOR_GROUP =
            Comparator.comparingInt(
                    (Group x) -> x.getStudents().size()).thenComparing(
                    COMPARATOR_GROUP_BY_NAME);
    protected static final Comparator<Group> REVERSED_COMPARATOR_GROUP_BY_NAME =
            COMPARATOR_GROUP_BY_NAME.reversed();

    protected static long getCountDistinctNames(final List<Student> students) {
        return students.stream().map(Student::getFirstName).distinct().count();
    }

    protected static final Comparator<Group> COMPARATOR_GROUP_UNIQUE =
            Comparator.comparingLong((Group x) -> getCountDistinctNames(
                    x.getStudents())).thenComparing(
                    REVERSED_COMPARATOR_GROUP_BY_NAME);

    protected static String getFullName(final Student a) {
        return String.format("%s %s", a.getFirstName(), a.getLastName());
    }

    protected static <R, A> Stream<R> applyToStream(
            final Collection<A> collection, final Function<A, R> func) {
        return collection.stream().map(func);
    }

    protected static <R, A> List<R> applyToList(final Collection<A> collection,
            final Function<A, R> func) {
        return applyToStream(collection, func).toList();
    }

    protected <R> List<R> sortToList(final Collection<R> collection,
            final Comparator<R> cmp) {
        return collection.stream().sorted(cmp).toList();
    }

    protected List<Group> sortGroupsByName(final Collection<Group> groups) {
        return sortToList(groups, Comparator.comparing(Group::getName));
    }

    protected static <A> List<Student> matchingToList(
            final Collection<Student> students,
            final Function<Student, A> getter, final A expected) {
        return students.stream().filter(
                x -> getter.apply(x).equals(expected)).sorted(
                COMPARATOR_STUDENTS_BY_NAME).toList();
    }

    protected static List<Group> getAllGroups(
            final Collection<Student> students) {
        return applyToList(students.stream().collect(
                                   Collectors.groupingBy(Student::getGroup)).entrySet(),
                           x -> new Group(x.getKey(), x.getValue()));
    }

    protected <R> List<R> filterIndices(final Collection<Student> students,
            final int[] ids, final Function<Student, R> func) {
        return applyToList(Arrays.stream(ids).mapToObj(
                students.stream().toList()::get).toList(), func);
    }

    protected static <R, A> R getMax(final Collection<A> collection,
            final Comparator<A> cmp, final Function<A, R> transform,
            final R defaultRes) {
        return collection.stream().max(cmp).map(transform).orElse(defaultRes);
    }

    protected String getMostName(final Collection<Student> students,
            final Comparator<Map.Entry<String, Integer>> cmp) {
        return getMax(sortToList(students.stream().collect(
                                         Collectors.groupingBy(Student::getFirstName,
                                                               Collectors.mapping(Student::getGroup,
                                                                                  Collectors.collectingAndThen(
                                                                                          Collectors.toSet(),
                                                                                          Set::size)))).entrySet(),
                                 Map.Entry.comparingByKey(
                                         Comparator.naturalOrder())), cmp,
                      Map.Entry::getKey, "");
    }
}