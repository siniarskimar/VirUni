package io.github.siniarski.viruni.dto.response;

import io.github.siniarski.viruni.security.permission.SubjectPermission;

import java.time.Instant;
import java.util.Set;

public record SubjectResponse (
    long id,
    String name,
    String description,
    AccountResponse leadingTeacher,
    Instant createdAt,
    Set<SubjectPermission> permissions

//    public SubjectResponse(Subject subject, SubjectPermission permissions) {
//        this.id = subject.getId();
//        this.name = subject.getName();
//        this.description = subject.getDescription();
//        this.leadingTeacher = subject.getLeadingTeacher();
//        this.createdAt = subject.getCreatedAt();
//        this.permissions = permissions;
//    }

) {}
