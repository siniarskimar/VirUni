package io.github.siniarski.viruni.model;

import jakarta.persistence.*;

@Entity
@Table(name = "subject_participants")
public class SubjectParticipant {

    @EmbeddedId
    private SubjectParticipantId id;

    @OneToOne
    @MapsId("participantId")
    @JoinColumn(name = "participant_id", nullable = false)
    private Account participant;

    @ManyToOne
    @MapsId("subjectId")
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @JoinColumn(name = "role_id", nullable = false)
    @ManyToOne
    private ParticipantRole role;

    public SubjectParticipant (Account participant,
                               Subject subject,
                               ParticipantRole role) {
        this.participant = participant;
        this.subject = subject;
        this.role = role;
        this.id = new SubjectParticipantId(participant.getId(), subject.getId());
    }

    protected SubjectParticipant() {}

    public void setParticipant(Account participant) {
        this.participant = participant;
        this.id = new SubjectParticipantId(participant.getId(), this.id.getSubjectId());
    }

    public Account getParticipant() {
        return participant;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
        this.id = new SubjectParticipantId(this.id.getParticipantId(), subject.getId());
    }

    public Subject getSubject() {
        return subject;
    }

    public ParticipantRole getRole() {
        return role;
    }

    public void setRole(ParticipantRole role) {
        this.role = role;
    }

    public SubjectParticipantId getId() {
        return id;
    }
}
