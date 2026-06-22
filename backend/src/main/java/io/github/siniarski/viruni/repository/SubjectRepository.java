package io.github.siniarski.viruni.repository;

import io.github.siniarski.viruni.model.Subject;
import io.github.siniarski.viruni.model.SubjectParticipantId;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long>,
        JpaSpecificationExecutor<Subject> {

    @Query("SELECT DISTINCT p.id FROM Subject s JOIN s.participants p WHERE s.id = :id")
    Optional<Set<Long>> findParticipantIdsBySubjectId(Long id);

    @Modifying
    @NativeQuery("INSERT INTO subject_participants (subjects_id, participants_id) " +
            "SELECT :id, a.id FROM account a " +
            "WHERE a.id IN (:accountIds) " +
            " AND a.id NOT IN (SELECT p.participants_id FROM subject_participants p WHERE p.subjects_id = :id)")
    void saveParticipantsByIds(Long id, Set<Long> accountIds);

}
