package io.github.siniarski.viruni.repository;

import io.github.siniarski.viruni.model.Account;
import io.github.siniarski.viruni.model.Grade;
import io.github.siniarski.viruni.model.Subject;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public class GradeSpecification {

    public static Specification<Grade> assignedByTeacher(long teacherId) {
        return (root, query, cb) -> {
            Join<Grade, Account> account = root.join("teacher", JoinType.INNER);
            return cb.equal(account.get("id"), teacherId);
        };
    }

    public static Specification<Grade> assignedToStudent(long studentId) {
        return (root, query, cb) -> {
            Join<Grade, Account> account = root.join("student", JoinType.INNER);
            return cb.equal(account.get("id"), studentId);
        };
    }

    public static Specification<Grade> ofSubject(long subjectId) {
        return (root, query, cb) -> {
            Join<Grade, Subject> subject = root.join("subject", JoinType.INNER);
            return cb.equal(subject.get("id"), subjectId);
        };
    }
}
