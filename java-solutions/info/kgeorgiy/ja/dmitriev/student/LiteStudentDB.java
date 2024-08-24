package info.kgeorgiy.ja.dmitriev.student;

import info.kgeorgiy.java.advanced.student.*;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

public class LiteStudentDB extends AbstractStudentDB implements StudentQuery {
    @Override
    public List<String> getFirstNames(final List<Student> students) {
        return applyToList(students, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(final List<Student> students) {
        return applyToList(students, Student::getLastName);
    }

    @Override
    public List<GroupName> getGroups(final List<Student> students) {
        return applyToList(students, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(final List<Student> students) {
        return applyToList(students, AbstractStudentDB::getFullName);
    }

    @Override
    public Set<String> getDistinctFirstNames(final List<Student> students) {
        return applyToStream(students, Student::getFirstName).collect(
                Collectors.toCollection(TreeSet::new));
    }

    @Override
    public String getMaxStudentFirstName(final List<Student> students) {
        return getMax(students, Comparator.naturalOrder(),
                      Student::getFirstName, "");
    }

    @Override
    public List<Student> sortStudentsById(final Collection<Student> students) {
        return sortToList(students, Comparator.naturalOrder());
    }

    @Override
    public List<Student> sortStudentsByName(
            final Collection<Student> students) {
        return sortToList(students, COMPARATOR_STUDENTS_BY_NAME);
    }

    @Override
    public List<Student> findStudentsByFirstName(
            final Collection<Student> students, final String name) {
        return matchingToList(students, Student::getFirstName, name);
    }

    @Override
    public List<Student> findStudentsByLastName(
            final Collection<Student> students, final String name) {
        return matchingToList(students, Student::getLastName, name);
    }

    @Override
    public List<Student> findStudentsByGroup(final Collection<Student> students,
            final GroupName group) {
        return matchingToList(students, Student::getGroup, group);
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(
            final Collection<Student> students, final GroupName group) {
        return findStudentsByGroup(students, group).stream().collect(
                Collectors.toUnmodifiableMap(Student::getLastName,
                                             Student::getFirstName,
                                             BinaryOperator.minBy(
                                                     Comparator.naturalOrder())));
    }
}
