package io.github.siniarski.viruni.security.permission;

import io.github.siniarski.viruni.security.auth.AccountPrinciple;
import io.github.siniarski.viruni.model.Account;
import io.github.siniarski.viruni.model.AccountRole;
import io.github.siniarski.viruni.model.Subject;
import io.github.siniarski.viruni.service.RoleHierarchyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SubjectPermissionService extends PermissionService<Subject, SubjectPermission> {
    private final RoleHierarchyService roleHierarchyService;

    @Autowired
    public SubjectPermissionService(RoleHierarchyService roleHierarchyService) {
        this.roleHierarchyService = roleHierarchyService;
    }

    @Override
    public Set<SubjectPermission> getPermissions(Authentication auth, Subject targetDomainObject) {
        if(auth == null) return Set.of();
        AccountPrinciple principal = (AccountPrinciple) auth.getPrincipal();
        Account account = principal.getAccount();

        Set<SubjectPermission> effectivePermissions = new HashSet<>();

        // TODO: check membership
        effectivePermissions.add(SubjectPermission.VIEW);

        if(roleHierarchyService.hasRoleImplied(AccountRole.TEACHER, account)) {
            effectivePermissions.addAll(List.of(
                    SubjectPermission.GRADE_CREATE,
                    SubjectPermission.USERS_UPDATE,
                    SubjectPermission.EDIT
            ));
        }

        if(roleHierarchyService.hasRoleImplied(AccountRole.ADMIN, account)) {
            effectivePermissions.add(SubjectPermission.DELETE);
        }

        return effectivePermissions;
    }

}
