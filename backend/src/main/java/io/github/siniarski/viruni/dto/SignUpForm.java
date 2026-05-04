package io.github.siniarski.viruni.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.siniarski.viruni.model.AccountRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SignUpForm {
    @NotBlank
    @Size(min=4, max=64)
    private String username;

    @NotBlank
    @Size(min=8, max=128)
    private String password;

    @NotBlank
    @Size(min=1, max=50)
    private String firstname;

    @NotBlank
    @Size(min=1, max=50)
    private String lastname;

    private AccountRole role = AccountRole.USER;

    @JsonProperty("teacherToken")
    private String teacherRegistrationToken;

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
