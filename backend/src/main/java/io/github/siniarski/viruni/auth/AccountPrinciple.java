package io.github.siniarski.viruni.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.siniarski.viruni.model.Account;
import io.github.siniarski.viruni.model.AccountRole;
import io.github.siniarski.viruni.security.Authority;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AccountPrinciple implements UserDetails {
    private Long id;

    private String username;

    @JsonIgnore
    private String password;

    private Collection<? extends GrantedAuthority> authorities;

    @JsonIgnore
    private Account account;

    public AccountPrinciple() {}

    public AccountPrinciple(Long id, String username, String password, Collection<? extends GrantedAuthority> authorities, Account account) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.authorities = authorities;
        this.account = account;
    }

    public static AccountPrinciple build(Account account, RoleHierarchy roleHierarchy) {
        AccountRole role = account.getRole();
        SimpleGrantedAuthority roleAuthority = new SimpleGrantedAuthority(role.getName());

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(roleAuthority);

        var reachableRoles = roleHierarchy.getReachableGrantedAuthorities(
                List.of(roleAuthority))
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        if(reachableRoles.contains(AccountRole.TEACHER.getName())) {
            authorities.add(Authority.GRADE_MANAGEMENT);
            authorities.add(Authority.SUBJECT_MANAGEMENT);
        }

        if(reachableRoles.contains(AccountRole.ADMIN.getName())) {
            authorities.add(Authority.BROWSE_SUBJECTS);
            authorities.add(Authority.CREATE_TEACHER_TOKEN);
        }

        return new AccountPrinciple(
                account.getId(),
                account.getUsername(),
                account.getPassword(),
                authorities,
                account);
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AccountPrinciple that)) return false;
        return Objects.equals(id, that.id);
    }

    public Long getId() {
        return id;
    }

    public Account getAccount() {
        return account;
    }
}
