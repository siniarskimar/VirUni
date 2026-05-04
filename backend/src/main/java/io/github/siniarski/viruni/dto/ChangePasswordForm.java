package io.github.siniarski.viruni.dto;

import jakarta.validation.constraints.NotBlank;

public class ChangePasswordForm {
    @NotBlank
    private String password;

    public String getPassword() {
        return password;
    }
}
