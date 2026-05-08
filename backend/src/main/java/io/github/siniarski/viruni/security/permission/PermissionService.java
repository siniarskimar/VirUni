package io.github.siniarski.viruni.security.permission;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Set;

public abstract class PermissionService<DomainObject, PermissionCategory> {

    public abstract Set<PermissionCategory> getPermissions(Authentication auth, DomainObject targetDomainObject);

    public Set<PermissionCategory> getPermissions(DomainObject targetDomainObject) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return getPermissions(auth, targetDomainObject);
    }

    public boolean hasPermission(Authentication auth, DomainObject targetDomainObject, PermissionCategory permission) {
        return getPermissions(auth, targetDomainObject).contains(permission);
    }

    public boolean hasPermission(DomainObject targetDomainObject, PermissionCategory permission) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return hasPermission(auth, targetDomainObject, permission);
    }
}
