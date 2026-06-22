package io.github.siniarski.viruni.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SoftDelete;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@SoftDelete
public class Subject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @CreationTimestamp
    private Instant createdAt;

    @JsonIgnore
    @ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private Set<SubjectParticipant> participants;

    @JsonIgnore
    @OneToMany(mappedBy = "subject")
    private List<Grade> grades;

    protected Subject() {}

    public Subject(String name, Account teacher) {
        setId(0);
        setName(name);
        this.participants = new HashSet<>(
                List.of(
                        new SubjectParticipant(teacher, this, ParticipantRole.LEADING_TEACHER)
                )
        );
    }

    public Subject(long id,
                   String name,
                   String description,
                   Instant createdAt,
                   Set<SubjectParticipant> participants,
                   List<Grade> grades) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.participants = participants;
        this.grades = grades;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<SubjectParticipant> getParticipants() {
        return participants;
    }

    public List<Grade> getGrades() {
        return grades;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setGrades(List<Grade> grades) {
        this.grades = grades;
    }
}
