package io.github.siniarski.viruni.dto;

import io.github.siniarski.viruni.security.SubjectPermissions;
import io.github.siniarski.viruni.model.Account;
import io.github.siniarski.viruni.model.Subject;

import java.time.Instant;

public class SubjectDTO {
    private long id;
    private String name;
    private String description;
    private Account leadingTeacher;
    private Instant createdAt;

    private SubjectPermissions permissions;

    public SubjectDTO(Subject subject, SubjectPermissions permissions) {
        this.id = subject.getId();
        this.name = subject.getName();
        this.description = subject.getDescription();
        this.leadingTeacher = subject.getLeadingTeacher();
        this.createdAt = subject.getCreatedAt();
        this.permissions = permissions;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Account getLeadingTeacher() {
        return leadingTeacher;
    }

    public SubjectPermissions getPermissions() {
        return permissions;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
