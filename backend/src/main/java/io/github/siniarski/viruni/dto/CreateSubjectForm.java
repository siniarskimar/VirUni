package io.github.siniarski.viruni.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Set;

public class CreateSubjectForm {
    @NotBlank
    private String name;

    private String description;

    private Set<Long> participants;

    private String leadingTeacherUsername;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<Long> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<Long> participants) {
        this.participants = participants;
    }

    public String getLeadingTeacherUsername() {
        return this.leadingTeacherUsername;
    }
}
