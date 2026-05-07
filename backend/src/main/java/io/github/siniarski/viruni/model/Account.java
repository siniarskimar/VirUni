package io.github.siniarski.viruni.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.*;

@Entity
public class Account {
    public static final int USERNAME_LEN_MIN = 4;
    public static final int USERNAME_LEN_MAX = 64;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank
    @Size(min = USERNAME_LEN_MIN, max = USERNAME_LEN_MAX)
    @Column(nullable = false, unique = true)
    private String username;

    @NotBlank
    @Column(nullable = false)
    @JsonIgnore
    private String password;

    @NotBlank
    @Size(min = 1, max = 50)
    private String firstname;

    @NotBlank
    @Size(min = 1, max = 50)
    private String lastname;

    @JsonIgnore
    @JoinColumn(nullable = false)
    @ManyToOne
    private AccountRole role;

    @JsonIgnore
    @ManyToMany(mappedBy = "participants", fetch = FetchType.LAZY)
    private Set<Subject> subjects;

    @JsonIgnore
    @OneToMany(mappedBy = "student", fetch = FetchType.LAZY)
    private List<Grade> receivedGrades;

    @JsonIgnore
    @OneToMany(mappedBy = "teacher", fetch = FetchType.LAZY)
    private List<Grade> assignedGrades;

    protected Account() {
    }

    public Account(String username,
            String password,
            String firstname,
            String lastname,
            AccountRole role) {
        setId(0);
        setFirstname(firstname);
        setLastname(lastname);
        setUsername(username);
        setPassword(password);
        setRole(role);
        this.subjects = new HashSet<>();
        this.receivedGrades = new ArrayList<>();
        this.assignedGrades = new ArrayList<>();
    }

    public Account(long id,
                   String username,
                   String password,
                   String firstname,
                   String lastname,
                   AccountRole role) {
        setId(id);
        setFirstname(firstname);
        setLastname(lastname);
        setUsername(username);
        setPassword(password);
        setRole(role);
        this.subjects = new HashSet<>();
        this.receivedGrades = new ArrayList<>();
        this.assignedGrades = new ArrayList<>();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setRole(AccountRole role) {
        this.role = role;
    }

    public AccountRole getRole() {
        return role;
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

    public Set<Subject> getSubjects() {
        return subjects;
    }

    public void setSubjects(Set<Subject> subjects) {
        this.subjects = subjects;
    }

    public List<Grade> getReceivedGrades() {
        return receivedGrades;
    }

    public void setReceivedGrades(List<Grade> receivedGrades) {
        this.receivedGrades = receivedGrades;
    }

    public List<Grade> getAssignedGrades() {
        return assignedGrades;
    }

    public void setAssignedGrades(List<Grade> assignedGrades) {
        this.assignedGrades = assignedGrades;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Account account))
            return false;
        return id == account.id && Objects.equals(username, account.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username);
    }
}
