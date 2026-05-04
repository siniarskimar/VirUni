package io.github.siniarski.viruni.repository;

import io.github.siniarski.viruni.model.Account;
import io.github.siniarski.viruni.model.Grade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long>, JpaSpecificationExecutor<Grade> {
    Page<Grade> findAllByStudent(Account student, Pageable pageable);
}
