package io.github.siniarski.viruni.security;

import io.github.siniarski.viruni.model.Account;
import io.github.siniarski.viruni.model.AccountRole;
import io.github.siniarski.viruni.security.auth.AccountPrincipal;
import io.github.siniarski.viruni.service.RoleHierarchyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AccountPermissionService {

    @Autowired
    private RoleHierarchyService roleHierarchyService;

    public Set<Authority> getPermissions(Authentication authentication, Account account) {
        AccountPrincipal principal = (AccountPrincipal) authentication.getPrincipal();
        Set<Authority> permissions = new HashSet<>();

        permissions.add(Authority.ACCOUNT_VIEW);

        if(principal.getId() == account.getId() || roleHierarchyService.hasRoleImplied(AccountRole.ADMIN, authentication)) {
            permissions.addAll(List.of(
                    Authority.ACCOUNT_DELETE,
                    Authority.ACCOUNT_UPDATE,
                    Authority.ACCOUNT_UPDATE_CREDENTIALS
            ));
        }

        return permissions;
    }

    public Set<Authority> getPermissions(Account account) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return getPermissions(auth, account);
    }

    public boolean hasPermission(Authentication authentication, Account account, Authority permission) {
        return getPermissions(authentication, account).contains(permission);
    }
}
