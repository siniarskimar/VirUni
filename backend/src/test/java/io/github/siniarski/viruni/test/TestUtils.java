package io.github.siniarski.viruni.test;

import io.github.siniarski.viruni.model.Account;
import io.github.siniarski.viruni.repository.AccountRepository;
import io.github.siniarski.viruni.security.auth.AccountPrinciple;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

public final class TestUtils {

    public static Authentication authenticateAs(String username,
                                                AccountRepository accountRepository,
                                                RoleHierarchy roleHierarchy) {
        var account = accountRepository.findByUsername(username).orElseThrow();
        return authenticateAs(account, roleHierarchy);
    }

    public static Authentication authenticateAs(Account account, RoleHierarchy roleHierarchy) {
        var principal = AccountPrinciple.build(account, roleHierarchy);
        Authentication auth = new TestingAuthenticationToken(principal, null, principal.getAuthorities());
        return auth;
    }

}
