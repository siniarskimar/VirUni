package io.github.siniarski.viruni.security.permission;

import io.github.siniarski.viruni.model.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.Serializable;

public class PermissionEvaluatorImpl implements PermissionEvaluator {

    @Autowired
    private AccountPermissionService accountPermissionService;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {

        if(targetDomainObject instanceof Account a && permission instanceof AccountPermission perm)
            return hasPermission(authentication, a, perm);

        return false;
    }

    public boolean hasPermission(Authentication authentication, Account targetDomainObject, AccountPermission permission) {
        return accountPermissionService.hasPermission(authentication, targetDomainObject, permission);
    }

    public boolean hasPermission(Account targetDomainObject, Object permission) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return hasPermission(auth, targetDomainObject, permission);
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        return false;
    }

}
