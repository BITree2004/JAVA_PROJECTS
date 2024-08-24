package info.kgeorgiy.ja.dmitriev.student;

import info.kgeorgiy.java.advanced.student.*;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;


public class HardStudentDB extends LiteStudentDB implements GroupQuery {
    @Override
    public List<Group> getGroupsByName(final Collection<Student> students) {
        return sortGroupsByName(getAllGroups(
                sortToList(students, COMPARATOR_STUDENTS_BY_NAME)));
    }

    @Override
    public List<Group> getGroupsById(final Collection<Student> students) {
        return sortGroupsByName(
                getAllGroups(sortToList(students, Comparator.naturalOrder())));
    }

    @Override
    public GroupName getLargestGroup(final Collection<Student> students) {
        // :NOTE: reuse comparator | fixed
        return getMax(getAllGroups(students), COMPARATOR_GROUP, Group::getName,
                      null);
    }

    @Override
    public GroupName getLargestGroupFirstName(
            final Collection<Student> students) {
        return getMax(getAllGroups(students), COMPARATOR_GROUP_UNIQUE,
                      Group::getName, null);
    }
}
