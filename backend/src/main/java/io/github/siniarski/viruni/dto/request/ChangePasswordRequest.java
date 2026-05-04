package io.github.siniarski.viruni.dto.request;

import jakarta.validation.constraints.NotBlank;

public class ChangePasswordRequest {
    @NotBlank
    private String password;

    public String getPassword() {
        return password;
    }
}
