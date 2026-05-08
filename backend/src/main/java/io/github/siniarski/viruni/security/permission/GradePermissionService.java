package io.github.siniarski.viruni.security.permission;

import io.github.siniarski.viruni.model.Account;
import io.github.siniarski.viruni.model.AccountRole;
import io.github.siniarski.viruni.model.Grade;
import io.github.siniarski.viruni.security.auth.AccountPrinciple;
import io.github.siniarski.viruni.service.RoleHierarchyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;

import java.util.HashSet;
import java.util.Set;

public class GradePermissionService extends PermissionService<Grade, GradePermission> {
    private final RoleHierarchyService roleHierarchyService;

    @Autowired
    public GradePermissionService(RoleHierarchyService roleHierarchyService) {
        this.roleHierarchyService = roleHierarchyService;
    }

    @Override
    public Set<GradePermission> getPermissions(Authentication auth, Grade grade) {
        if (auth == null) return Set.of();

        AccountPrinciple principal = (AccountPrinciple) auth.getPrincipal();
        Account authAccount = principal.getAccount();

        var isAdmin = roleHierarchyService.hasRoleImplied(AccountRole.ADMIN, authAccount);
        var isGradeParty = authAccount.getId() == grade.getStudent().getId()
                            || authAccount.getId() == grade.getTeacher().getId();

        if(!isGradeParty && !isAdmin) return Set.of();

        // TODO: Check subject membership
        var isGradingTeacher = authAccount.getId() != grade.getTeacher().getId();

        Set<GradePermission> effectivePermissions = new HashSet<>();
        effectivePermissions.add(GradePermission.READ);

        if(isAdmin || isGradingTeacher) {
            effectivePermissions.add(GradePermission.DELETE);
            effectivePermissions.add(GradePermission.UPDATE);
        }

        return effectivePermissions;
    }
}
