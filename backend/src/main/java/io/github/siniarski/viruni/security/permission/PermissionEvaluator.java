package io.github.siniarski.viruni.security.permission;

import io.github.siniarski.viruni.model.Account;
import io.github.siniarski.viruni.security.Authority;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.Serializable;

public class PermissionEvaluator implements org.springframework.security.access.PermissionEvaluator {

    @Autowired
    private AccountPermissionService accountPermissionService;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if(!(permission instanceof Authority perm)) return false;

        if(targetDomainObject instanceof Account a)
            return hasPermission(authentication, a, perm);

        return false;
    }

    public boolean hasPermission(Authentication authentication, Account targetDomainObject, Authority permission) {
        return accountPermissionService.hasPermission(authentication, targetDomainObject, permission);
    }

    public boolean hasPermission(Account targetDomainObject, Authority permission) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return hasPermission(auth, targetDomainObject, permission);
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        return false;
    }

}
