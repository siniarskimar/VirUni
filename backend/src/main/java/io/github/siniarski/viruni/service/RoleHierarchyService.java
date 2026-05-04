package io.github.siniarski.viruni.service;

import io.github.siniarski.viruni.model.Account;
import io.github.siniarski.viruni.model.AccountRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Collection;

@Service
public class RoleHierarchyService {
    private final RoleHierarchy roleHierarchy;
    private final UserDetailsService userDetailsService;

    @Autowired
    public RoleHierarchyService(RoleHierarchy roleHierarchy,
                                UserDetailsService userDetailsService) {
        this.roleHierarchy = roleHierarchy;
        this.userDetailsService = userDetailsService;
    }

    private boolean hasRoleImplied(AccountRole role, String username) {
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
        Collection<? extends GrantedAuthority> authorities =
                roleHierarchy.getReachableGrantedAuthorities(userDetails.getAuthorities());

        return authorities.stream().anyMatch(auth -> auth.getAuthority().equals(role.getName()));
    }

    public boolean hasRoleImplied(AccountRole role, Account account) {
        return hasRoleImplied(role, account.getUsername());
    }

    public boolean hasRoleImplied(AccountRole role, Principal principal) {
        return hasRoleImplied(role, principal.getName());
    }

    public boolean hasRoleImplied(AccountRole role, Authentication authentication) {
        Collection<? extends GrantedAuthority> authorities =
                roleHierarchy.getReachableGrantedAuthorities(authentication.getAuthorities());

        return authorities.stream().anyMatch(auth -> auth.getAuthority().equals(role.getName()));
    }

}
