package io.github.siniarski.viruni.security.permission;

import io.github.siniarski.viruni.model.Account;
import io.github.siniarski.viruni.model.AccountRole;
import io.github.siniarski.viruni.security.Authority;
import io.github.siniarski.viruni.security.auth.AccountPrinciple;
import io.github.siniarski.viruni.service.RoleHierarchyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AccountPermissionService extends PermissionService<Account, AccountPermission> {

    private final RoleHierarchyService roleHierarchyService;

    @Autowired
    public AccountPermissionService(RoleHierarchyService roleHierarchyService) {
        this.roleHierarchyService = roleHierarchyService;
    }

    public Set<AccountPermission> getPermissions(Authentication authentication, Account account) {
        if(authentication == null) return Set.of();

        AccountPrinciple principal = (AccountPrinciple) authentication.getPrincipal();
        var isAdmin = roleHierarchyService.hasRoleImplied(AccountRole.ADMIN, authentication);

        Set<AccountPermission> permissions = new HashSet<>();
        // FEAT: Hidden accounts?
        permissions.add(AccountPermission.VIEW);

        if(principal.getId() == account.getId() || isAdmin) {
            permissions.addAll(List.of(
                    AccountPermission.DELETE,
                    AccountPermission.EDIT,
                    AccountPermission.EDIT_CREDENTIALS
            ));
        }

        return permissions;
    }
}
