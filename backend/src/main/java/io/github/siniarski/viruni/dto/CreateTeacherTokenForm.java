package io.github.siniarski.viruni.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public class CreateTeacherTokenForm {
    @NotNull
    private Boolean reusable;

    private Instant expires;

    public Boolean getReusable() {
        return reusable;
    }

    public Instant getExpires() {
        return expires;
    }
}
