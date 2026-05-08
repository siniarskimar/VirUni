package io.github.siniarski.viruni.test.security.permission;

import io.github.siniarski.viruni.model.Account;
import io.github.siniarski.viruni.model.AccountRole;
import io.github.siniarski.viruni.repository.AccountRepository;
import io.github.siniarski.viruni.security.auth.AccountDetailsServiceImpl;
import io.github.siniarski.viruni.security.auth.AccountPrinciple;
import io.github.siniarski.viruni.security.permission.AccountPermission;
import io.github.siniarski.viruni.security.permission.AccountPermissionService;
import io.github.siniarski.viruni.service.RoleHierarchyService;
import io.github.siniarski.viruni.test.TestConfig;
import io.github.siniarski.viruni.test.TestMocks;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class AccountPermissionServiceTest {

    @Mock
    private AccountRepository accountRepository;

    private UserDetailsService userDetailsService;
    private RoleHierarchy roleHierarchy;
    private RoleHierarchyService roleHierarchyService;
    private AccountPermissionService service;

    private static final List<Account> accounts = List.of(
            new Account(
                    0,
                    "admin",
                    "admin",
                    "SYSTEM",
                    "ADMIN",
                    AccountRole.ADMIN
            ),
            new Account(
                    1,
                    "johndep",
                    "arasaka",
                    "John",
                    "Depp",
                    AccountRole.TEACHER
            ),
            new Account(
                    2,
                    "the_reeves",
                    "beautiful",
                    "Keanu",
                    "Reeves",
                    AccountRole.USER
            )
    );

    @BeforeEach
    void beforeEach() {
        TestMocks.stubAccountRepositoryByUsername(accountRepository, accounts);

        roleHierarchy = RoleHierarchyImpl.withDefaultRolePrefix()
                .role("ADMIN").implies("TEACHER")
                .role("TEACHER").implies("USER")
                .build();

        userDetailsService = new AccountDetailsServiceImpl(accountRepository, roleHierarchy);
        roleHierarchyService = new RoleHierarchyService(roleHierarchy, userDetailsService);
        service = new AccountPermissionService(roleHierarchyService);
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    Authentication authenticateAs(String username) {
        var account = accountRepository.findByUsername(username).orElseThrow();
        var principal = AccountPrinciple.build(account, roleHierarchy);
        Authentication auth = new TestingAuthenticationToken(principal, null, principal.getAuthorities());
        return auth;
    }


    @ParameterizedTest
    @ValueSource(strings = {"the_reeves", "johndep", "admin"})
    public void testPermissions_self(String username) {
        var auth = authenticateAs(username);
        var acc = accountRepository.findByUsername(username).orElseThrow();

        assertThat(service.getPermissions(auth, acc))
                .containsExactlyInAnyOrder(
                        AccountPermission.VIEW,
                        AccountPermission.DELETE,
                        AccountPermission.EDIT,
                        AccountPermission.EDIT_CREDENTIALS
                );

        assertTrue(service.hasPermission(auth, acc, AccountPermission.VIEW));
        assertTrue(service.hasPermission(auth, acc, AccountPermission.DELETE));

        assertTrue(service.hasPermission(auth, acc, AccountPermission.EDIT));
        assertTrue(service.hasPermission(auth, acc, AccountPermission.EDIT_CREDENTIALS));
    }

    @ParameterizedTest
    @ValueSource(strings = {"johndep", "admin"})
    public void testPermissions_other(String otherUsername) {
        var auth = authenticateAs("the_reeves");
        var acc = accountRepository.findByUsername(otherUsername).orElseThrow();

        assertThat(service.getPermissions(auth, acc))
                .containsExactlyInAnyOrder(
                        AccountPermission.VIEW
                );

        assertTrue(service.hasPermission(auth, acc, AccountPermission.VIEW));
        assertFalse(service.hasPermission(auth, acc, AccountPermission.DELETE));

        assertFalse(service.hasPermission(auth, acc, AccountPermission.EDIT));
        assertFalse(service.hasPermission(auth, acc, AccountPermission.EDIT_CREDENTIALS));
    }

    @ParameterizedTest
    @ValueSource(strings = {"the_reeves", "johndep"})
    public void testPermissions_admin(String otherUsername) {
        var auth = authenticateAs("admin");
        var acc = accountRepository.findByUsername(otherUsername).orElseThrow();

        assertThat(service.getPermissions(auth, acc))
                .containsExactlyInAnyOrder(
                        AccountPermission.VIEW,
                        AccountPermission.DELETE,
                        AccountPermission.EDIT,
                        AccountPermission.EDIT_CREDENTIALS
                );

        assertTrue(service.hasPermission(auth, acc, AccountPermission.VIEW));
        assertTrue(service.hasPermission(auth, acc, AccountPermission.DELETE));

        assertTrue(service.hasPermission(auth, acc, AccountPermission.EDIT));
        assertTrue(service.hasPermission(auth, acc, AccountPermission.EDIT_CREDENTIALS));
    }
}
