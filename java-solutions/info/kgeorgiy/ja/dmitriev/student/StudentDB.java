package info.kgeorgiy.ja.dmitriev.student;

import info.kgeorgiy.java.advanced.student.*;

import java.util.*;

@SuppressWarnings("unused")
public class StudentDB extends HardStudentDB implements AdvancedQuery {
    @Override
    public String getMostPopularName(final Collection<Student> students) {
        return getMostName(students, Map.Entry.comparingByValue(
                Comparator.naturalOrder()));
    }

    @Override
    public String getLeastPopularName(final Collection<Student> students) {
        return getMostName(students, Map.Entry.comparingByValue(
                Comparator.reverseOrder()));
    }

    @Override
    public List<String> getFirstNames(final Collection<Student> students,
            final int[] ids) {
        return filterIndices(Objects.requireNonNull(students), ids,
                             Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(final Collection<Student> students,
            final int[] ids) {
        return filterIndices(Objects.requireNonNull(students), ids,
                             Student::getLastName);
    }

    @Override
    public List<GroupName> getGroups(final Collection<Student> students,
            final int[] ids) {
        return filterIndices(Objects.requireNonNull(students), ids,
                             Student::getGroup);
    }

    @Override
    public List<String> getFullNames(final Collection<Student> students,
            final int[] ids) {
        return filterIndices(Objects.requireNonNull(students), ids,
                             AbstractStudentDB::getFullName);
    }
}
