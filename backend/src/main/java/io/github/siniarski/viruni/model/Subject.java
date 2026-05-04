package io.github.siniarski.viruni.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class Subject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Account leadingTeacher;

    private String description;

    @CreationTimestamp
    private Instant createdAt;

    @JsonIgnore
    @ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private Set<Account> participants;

    @JsonIgnore
    @OneToMany(mappedBy = "subject")
    private List<Grade> grades;

    protected Subject() {}

    public Subject(String name, Account teacher) {
        setId(0);
        setName(name);
        this.participants = new HashSet<>();
        setLeadingTeacher(teacher);
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

    public Set<Account> getParticipants() {
        return participants;
    }

    public void setLeadingTeacher(Account teacher) {
        getParticipants().add(teacher);
        this.leadingTeacher = teacher;
    }

    public Account getLeadingTeacher() {
        return this.leadingTeacher;
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
