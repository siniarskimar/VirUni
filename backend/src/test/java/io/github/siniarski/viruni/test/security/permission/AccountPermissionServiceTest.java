package io.github.siniarski.viruni.test.security.permission;

import io.github.siniarski.viruni.model.Account;
import io.github.siniarski.viruni.model.AccountRole;
import io.github.siniarski.viruni.repository.AccountRepository;
import io.github.siniarski.viruni.security.auth.AccountDetailsServiceImpl;
import io.github.siniarski.viruni.security.auth.AccountPrinciple;
import io.github.siniarski.viruni.security.permission.AccountPermission;
import io.github.siniarski.viruni.security.permission.AccountPermissionService;
import io.github.siniarski.viruni.service.RoleHierarchyService;
import io.github.siniarski.viruni.test.TestMocks;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.*;
import java.util.stream.Stream;

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

    @Test
    void shouldDisallowUnauthenticated() {
        assertThat(service.getPermissions(null, accounts.get(0)))
                .containsExactlyInAnyOrder();
    }

    @ParameterizedTest
    @MethodSource("permissionCases")
    void testPermissions(String authUsername, String targetUsername, Set<AccountPermission> expected) {
        TestMocks.stubAccountRepositoryByUsername(accountRepository, accounts);

        var auth = authenticateAs(authUsername);
        var acc = accountRepository.findByUsername(targetUsername).orElseThrow();

        var perms = service.getPermissions(auth, acc);
        assertThat(perms).containsExactlyInAnyOrderElementsOf(expected);

        // verify hasPermission matches membership in expected set for all enum values used
        for (AccountPermission p : AccountPermission.values()) {
            boolean expectedHas = expected.contains(p);
            assertThat(service.hasPermission(auth, acc, p)).isEqualTo(expectedHas);
        }
    }

    static Stream<Arguments> permissionCases() {
        var all = Set.of(
                AccountPermission.VIEW,
                AccountPermission.DELETE,
                AccountPermission.EDIT,
                AccountPermission.EDIT_CREDENTIALS
        );
        var viewOnly = Set.of(AccountPermission.VIEW);

        return Stream.of(
                // self cases
                Arguments.of("the_reeves", "the_reeves", all),
                Arguments.of("johndep", "johndep", all),
                Arguments.of("admin", "admin", all),

                // other as "the_reeves" viewing others => viewOnly
                Arguments.of("the_reeves", "johndep", viewOnly),
                Arguments.of("the_reeves", "admin", viewOnly),

                // admin viewing others => full perms
                Arguments.of("admin", "the_reeves", all),
                Arguments.of("admin", "johndep", all)
        );
    }
}
