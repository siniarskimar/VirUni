package io.github.siniarski.viruni.repository;

import io.github.siniarski.viruni.model.Account;
import io.github.siniarski.viruni.model.Subject;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

public class SubjectSpecification {
    public static Specification<Subject> hasParticipant(long participantId) {
        return (root, query, cb) -> {
            Join<Subject, Account> participants = root.join("participants", JoinType.INNER);
            return cb.equal(participants.get("id"), participantId);
        };
    }

    public static Specification<Subject> createdAfter(Instant after) {
        return (root, query, cb) -> cb.greaterThan(root.get("createdAfter"), after);
    }

    public static Specification<Subject> hasLeadingTeacher(long leadingTeacher) {
        return (root, query, cb) -> cb.equal(root.get("leadingTeacher"), leadingTeacher);
    }

    public static Specification<Subject> nameOrleadingTeacherFullnameContaining(String q) {
        String likeExpr = "%"+q+"%";
        return (root, query, cb) -> {
            Join<Subject, Account> leadingTeacher = root.join("leadingTeacher", JoinType.INNER);
            return cb.or(
                    cb.like(leadingTeacher.get("firstname"), likeExpr),
                    cb.like(leadingTeacher.get("lastname"), likeExpr),
                    cb.like(root.get("name"), likeExpr));
        };
    }
}
