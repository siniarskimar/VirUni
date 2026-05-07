package io.github.siniarski.viruni.dto.request;

public class UpdateAccountRequest {
    private String firstname;
    private String lastname;
    private String password;

    public String getLastname() {
        return lastname;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getPassword() {return password;}
}
