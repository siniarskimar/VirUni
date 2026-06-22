package io.github.siniarski.viruni.repository;

import io.github.siniarski.viruni.model.Subject;
import io.github.siniarski.viruni.model.SubjectParticipant;
import io.github.siniarski.viruni.model.SubjectParticipantId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface SubjectParticipantRepository
        extends JpaRepository<SubjectParticipant, SubjectParticipantId> {

    @Query("SELECT DISTINCT p.participant_id FROM SubjectParticipants p WHERE s.id = :id")
    Optional<Set<Long>> findIdsBySubject(Subject subject);

    Long countById(SubjectParticipantId id);
}
