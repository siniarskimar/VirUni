package io.github.siniarski.viruni.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

@Entity
@Table(name = "participant_role")
public final class ParticipantRole {
    public static final ParticipantRole LEADING_TEACHER = new ParticipantRole(1, "LEADING_TEACHER");
    public static final ParticipantRole TEACHER = new ParticipantRole(2, "TEACHER");
    public static final ParticipantRole STUDENT = new ParticipantRole(3, "STUDENT");
    public static final ParticipantRole GUEST = new ParticipantRole(3, "GUEST");

    @Id
    private long id;

    @Column(nullable = false, unique = true)
    private String name;

    private ParticipantRole(long id, String name) {
        this.id = id;
        this.name = name;
    }

    // For serialization
    protected ParticipantRole() {}

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static ParticipantRole[] values() {
        return new ParticipantRole[] {LEADING_TEACHER, TEACHER, STUDENT, GUEST};
    }

    public static ParticipantRole valueOf(String name) {
        return valueOfOptional(name)
                .orElseThrow(IllegalArgumentException::new);
    }

    public static Optional<ParticipantRole> valueOfOptional(String name) {
        return Arrays.stream(values())
                .filter(r -> r.getName().equals(name))
                .findFirst();
    }

    public static ParticipantRole valueOf(long id) {
        return Arrays.stream(values())
                .filter(r -> r.getId() == id)
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ParticipantRole that)) return false;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
