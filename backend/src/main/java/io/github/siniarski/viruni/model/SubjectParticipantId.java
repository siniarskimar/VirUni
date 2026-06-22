package io.github.siniarski.viruni.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class SubjectParticipantId implements Serializable {
    @Column(name = "participant_id", nullable = false)
    private long participantId;

    @Column(name = "subject_id", nullable = false)
    private long subjectId;

    public long getParticipantId() {
        return participantId;
    }

    public long getSubjectId() {
        return subjectId;
    }

    protected SubjectParticipantId() {}

    public SubjectParticipantId(long participantId, long subjectId) {
        this.participantId = participantId;
        this.subjectId = subjectId;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SubjectParticipantId that)) return false;
        return participantId == that.participantId && subjectId == that.subjectId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(participantId, subjectId);
    }
}
