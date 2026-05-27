package io.github.siniarski.viruni.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.Set;

public record CreateSubjectRequest(@NotBlank String name,
                                   String description,
                                   Set<Long> participants,
                                   String leadingTeacherUsername
) { }
