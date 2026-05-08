package io.github.siniarski.viruni.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull
    @ManyToOne
    private Subject subject;

    @NotNull
    @ManyToOne
    private Account student;

    @NotNull
    @ManyToOne
    private Account teacher;

    @NotNull
    private float value;

    @CreationTimestamp
    private Instant creation;

    protected Grade() {}

    public Grade(Subject subject, Account student, Account teacher, float value) {
        setSubject(subject);
        setStudent(student);
        setTeacher(teacher);
        setValue(value);
    }

    public Grade(long id,
                 Subject subject,
                 Account student,
                 Account teacher,
                 float value,
                 Instant creation) {
        this.id = id;
        this.subject = subject;
        this.student = student;
        this.teacher = teacher;
        this.value = value;
        this.creation = creation;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public Account getStudent() {
        return student;
    }

    public void setStudent(Account student) {
        this.student = student;
    }

    public Account getTeacher() {
        return teacher;
    }

    public void setTeacher(Account teacher) {
        this.teacher = teacher;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public Instant getCreation() {
        return creation;
    }
}
