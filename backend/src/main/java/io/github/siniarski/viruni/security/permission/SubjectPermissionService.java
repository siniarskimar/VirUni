package io.github.siniarski.viruni.security.permission;

import io.github.siniarski.viruni.model.*;
import io.github.siniarski.viruni.repository.SubjectParticipantRepository;
import io.github.siniarski.viruni.security.auth.AccountPrincipal;
import io.github.siniarski.viruni.service.RoleHierarchyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SubjectPermissionService extends PermissionService<Subject, SubjectPermission> {
    private final RoleHierarchyService roleHierarchyService;
    private final SubjectParticipantRepository subjectParticipantRepository;

    @Autowired
    public SubjectPermissionService(RoleHierarchyService roleHierarchyService,
                                    SubjectParticipantRepository subjectParticipantRepository) {
        this.roleHierarchyService = roleHierarchyService;
        this.subjectParticipantRepository = subjectParticipantRepository;
    }

    @Override
    public Set<SubjectPermission> getPermissions(Authentication auth, Subject targetDomainObject) {
        if(auth == null) return Set.of();
        AccountPrincipal principal = (AccountPrincipal) auth.getPrincipal();
        Account account = principal.getAccount();


        if(roleHierarchyService.hasRoleImplied(AccountRole.ADMIN, account)) {
            return Set.of(SubjectPermission.values());
        }

        var participant = subjectParticipantRepository.findById(
                new SubjectParticipantId(account.getId(), targetDomainObject.getId()))
                .orElse(null);
        if(participant == null) {
            return Set.of();
        }
        var role = participant.getRole();

        Set<SubjectPermission> effectivePermissions = new HashSet<>();
        effectivePermissions.add(SubjectPermission.VIEW);


        if(role == ParticipantRole.LEADING_TEACHER) {
            effectivePermissions.addAll(List.of(
                    SubjectPermission.GRADE_CREATE,
                    SubjectPermission.USERS_UPDATE,
                    SubjectPermission.EDIT
            ));
        } else if(role == ParticipantRole.TEACHER) {
            effectivePermissions.addAll(List.of(
                    SubjectPermission.GRADE_CREATE,
                    SubjectPermission.USERS_UPDATE
            ));
        }

        return effectivePermissions;
    }

}
