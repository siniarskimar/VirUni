package io.github.siniarski.viruni.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.siniarski.viruni.model.AccountRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignUpRequest(
        @NotBlank
        @Size(min=4, max=64)
        String username,

        @NotBlank
        @Size(min=8, max=128)
        String password,

        @NotBlank
        @Size(min=1, max=50)
        String firstname,

        @NotBlank
        @Size(min=1, max=50)
        String lastname,

        AccountRole role,

        @JsonProperty("teacherToken")
        String teacherRegistrationToken
) {

    // TODO: Remove these getters and setters
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public AccountRole getRole() {
        return role;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public String getTeacherRegistrationToken() {
        return teacherRegistrationToken;
    }
}
