package io.github.siniarski.viruni.security.permission;

import io.github.siniarski.viruni.model.*;
import io.github.siniarski.viruni.repository.SubjectParticipantRepository;
import io.github.siniarski.viruni.security.auth.AccountPrincipal;
import io.github.siniarski.viruni.service.RoleHierarchyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SubjectParticipantPermissionService
        extends PermissionService<SubjectParticipant, SubjectParticipantPermission> {
    private final RoleHierarchyService roleHierarchyService;
    private final SubjectParticipantRepository subjectParticipantRepository;

    @Autowired
    public SubjectParticipantPermissionService(RoleHierarchyService roleHierarchyService,
                                               SubjectParticipantRepository subjectParticipantRepository) {
        this.roleHierarchyService = roleHierarchyService;
        this.subjectParticipantRepository = subjectParticipantRepository;
    }

    @Override
    public Set<SubjectParticipantPermission> getPermissions(Authentication auth, SubjectParticipant targetDomainObject) {
        if(auth == null) return Set.of();

        AccountPrincipal principal = (AccountPrincipal) auth.getPrincipal();
        Account account = principal.getAccount();


        if(roleHierarchyService.hasRoleImplied(AccountRole.ADMIN, account)) {
            return Set.of(SubjectParticipantPermission.values());
        }

        var participantId = new SubjectParticipantId(account.getId(), targetDomainObject.getSubject().getId());
        var participant = subjectParticipantRepository.findById(participantId).orElse(null);
        if(participant == null) {
            return Set.of();
        }

        var effectivePermissions = new HashSet<SubjectParticipantPermission>();
        var role = participant.getRole();
        if(Set.of(ParticipantRole.LEADING_TEACHER, ParticipantRole.TEACHER).contains(role)) {
            effectivePermissions.addAll(List.of(
                    SubjectParticipantPermission.CREATE,
                    SubjectParticipantPermission.READ,
                    SubjectParticipantPermission.UPDATE,
                    SubjectParticipantPermission.DELETE
            ));
        } else if(role == ParticipantRole.STUDENT) {
            effectivePermissions.add(SubjectParticipantPermission.READ);
        }

        return effectivePermissions;
    }
}
