package io.github.siniarski.viruni.dto.response;

import io.github.siniarski.viruni.security.permission.SubjectPermission;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public record SubjectResponse (
    long id,
    String name,
    String description,
    List<AccountResponse> leadingTeachers,
    Instant createdAt,
    Set<SubjectPermission> permissions

) {}
