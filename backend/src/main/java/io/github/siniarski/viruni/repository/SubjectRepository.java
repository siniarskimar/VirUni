package io.github.siniarski.viruni.repository;

import io.github.siniarski.viruni.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long>,
        JpaSpecificationExecutor<Subject> {

    @Query("SELECT DISTINCT p.id FROM Subject s JOIN s.participants p WHERE s.id = :id")
    Optional<Set<Long>> findParticipantIdsBySubjectId(Long id);
}
