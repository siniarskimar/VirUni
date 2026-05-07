package io.github.siniarski.viruni.dto.response;

import io.github.siniarski.viruni.security.Authority;

import java.util.Objects;
import java.util.Set;

public class AccountResponse {
    long id;
    String username;
    String firstname;
    String lastname;
    Set<Authority> authorities;

    public AccountResponse(long id, String username, String firstname, String lastname, Set<Authority> authorities) {
        this.id = id;
        this.username = username;
        this.firstname = firstname;
        this.lastname = lastname;
        this.authorities = authorities;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public Set<Authority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Set<Authority> authorities) {
        this.authorities = authorities;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AccountResponse that)) return false;
        return id == that.id && Objects.equals(username, that.username) && Objects.equals(firstname, that.firstname) && Objects.equals(lastname, that.lastname) && Objects.equals(authorities, that.authorities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, firstname, lastname, authorities);
    }
}

