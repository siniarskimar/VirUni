package io.github.siniarski.viruni.security;

import io.github.siniarski.viruni.auth.AccountPrinciple;
import io.github.siniarski.viruni.model.Account;
import io.github.siniarski.viruni.model.AccountRole;
import io.github.siniarski.viruni.model.Subject;
import io.github.siniarski.viruni.service.RoleHierarchyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SubjectPermissionService {
    private final RoleHierarchyService roleHierarchyService;

    private final Map<AccountIdSubjectIdPair, SubjectPermissions> cache;

    @Autowired
    public SubjectPermissionService(RoleHierarchyService roleHierarchyService) {
        this.roleHierarchyService = roleHierarchyService;
        this.cache = new ConcurrentHashMap<>();
    }

    private  AccountIdSubjectIdPair createKey(Subject subject, Authentication auth) {
        AccountPrinciple principle = (AccountPrinciple) auth.getPrincipal();
        return new AccountIdSubjectIdPair(principle.getId(), subject.getId());
    }

    private AccountIdSubjectIdPair createKey(Subject subject, Account account) {
        return new AccountIdSubjectIdPair(account.getId(), subject.getId());
    }

    public void evictFromCache(Subject subject, Authentication auth) {
        this.cache.remove(createKey(subject, auth));
    }

    public void evictFromCache(Subject subject, Account account) {
        this.cache.remove(createKey(subject, account));
    }

    public void evictFromCache(Subject subject, Collection<Account> accounts) {
        for(Account account : accounts) {
            this.evictFromCache(subject, account);
        }
    }

    public SubjectPermissions computePermissions(Subject subject, Authentication auth) {
        AccountPrinciple principle = (AccountPrinciple) auth.getPrincipal();

        return cache.computeIfAbsent(createKey(subject, auth), (k) -> {
            SubjectPermissions perms = new SubjectPermissions();

            if(this.roleHierarchyService.hasRoleImplied(AccountRole.ADMIN, auth)) {
                perms.setAll(true);
                return perms;
            }


            if(!subject.getParticipants().contains(principle.getAccount())) {
                return perms;
            }

            if(this.roleHierarchyService.hasRoleImplied(AccountRole.TEACHER, auth)) {
                perms.setCanManageAccounts(true);
                perms.setCanManageGrades(true);

                if(subject.getLeadingTeacher().getId() == principle.getId()) {
                    perms.setCanDelete(true);
                    perms.setCanUpdate(true);
                }

                return perms;
            }

            return perms;
        });
    }

    public boolean hasDeletePermission(Subject subject, Authentication auth) {
        var perms = computePermissions(subject, auth);
        return perms.isCanDelete();
    }

}

record AccountIdSubjectIdPair(long accountId, long subjectId) {

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AccountIdSubjectIdPair that)) return false;
        return accountId == that.accountId && subjectId == that.subjectId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, subjectId);
    }
}
