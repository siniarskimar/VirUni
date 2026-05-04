package io.github.siniarski.viruni.repository;

import io.github.siniarski.viruni.model.Account;
import io.github.siniarski.viruni.model.AccountRole;
import io.github.siniarski.viruni.model.Subject;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public class AccountSpecification {

    public static Specification<Account> hasRole(AccountRole role) {
        return (root, query, cb) -> {
            Join<Account, AccountRole> roles = root.join("role", JoinType.INNER);
            return cb.equal(roles.get("name"), role.getName());
        };
    }


    public static Specification<Account> firstnameContains(String q) {
        return (root, query, cb) -> cb.like(cb.upper(root.get("firstname")), "%" + q.toUpperCase() + "%");
    }

    public static Specification<Account> lastnameContains(String q) {
        return (root, query, cb) -> cb.like(cb.upper(root.get("lastname")), "%" + q.toUpperCase() + "%");
    }


    public static Specification<Account> isParticipantOf(long subjectId) {
        return (root, query, cb) -> {
            Join<Account, Subject> subjects = root.join("subjects", JoinType.INNER);
            return cb.equal(subjects.get("id"), subjectId);
        };
    }
}
