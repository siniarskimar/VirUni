package io.github.siniarski.viruni.dto.request;

import jakarta.validation.constraints.NotBlank;

public class SignInRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    protected SignInRequest() {
    }

    public SignInRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
