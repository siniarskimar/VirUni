package io.github.siniarski.viruni.test.security.permission;

import io.github.siniarski.viruni.model.Account;
import io.github.siniarski.viruni.model.AccountRole;
import io.github.siniarski.viruni.model.Subject;
import io.github.siniarski.viruni.repository.AccountRepository;
import io.github.siniarski.viruni.repository.SubjectRepository;
import io.github.siniarski.viruni.security.auth.AccountDetailsServiceImpl;
import io.github.siniarski.viruni.security.permission.GradePermission;
import io.github.siniarski.viruni.security.permission.SubjectPermission;
import io.github.siniarski.viruni.security.permission.SubjectPermissionService;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.github.siniarski.viruni.test.TestUtils.authenticateAs;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class SubjectPermissionServiceTest {
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private SubjectRepository subjectRepository;

    private UserDetailsService userDetailsService;
    private RoleHierarchy roleHierarchy;
    private RoleHierarchyService roleHierarchyService;
    private SubjectPermissionService service;

    private static final List<Account> accounts = List.of(
            new Account(0, "admin", "admin", "SYSTEM", "ADMIN", AccountRole.ADMIN),
            new Account(1, "johndep", "arasaka", "John", "Depp", AccountRole.TEACHER),
            new Account(2, "the_reeves", "beautiful", "Keanu", "Reeves", AccountRole.USER),
            new Account(3, "alice", "wonder", "Alice", "Wonderland", AccountRole.USER),
            new Account(4, "maria.santos", "T3stP@ssw0rd", "Maria", "Santos", AccountRole.TEACHER)
    );

    private static final List<Subject> subjects = List.of(
            new Subject(
                    0,
                    "Principles of Macroeconomics",
                    accounts.get(1), // teacher: johndep
                    null,
                    Instant.now(),
                    Set.of(accounts.get(1), accounts.get(2)),
                    new ArrayList<>()
            ),
            // new subject
            new Subject(
                    1,
                    "Linear Algebra",
                    accounts.get(4), // teacher: maria.santos
                    null,
                    Instant.now(),
                    Set.of(accounts.get(4), accounts.get(2), accounts.get(3)),
                    new ArrayList<>()
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
        service = new SubjectPermissionService(roleHierarchyService);
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldDisallowUnauthenticated() {
        assertThat(service.getPermissions(null, subjects.get(0)))
                .containsExactlyInAnyOrder();
    }

    @ParameterizedTest
    @MethodSource("permissionCases")
    void testPermissions(String authUsername, long targetId, Set<SubjectPermission> expected) {
        TestMocks.stubAccountRepositoryByUsername(accountRepository, accounts);
        TestMocks.stubSubjectRepositoryById(subjectRepository, subjects);

        var auth = authenticateAs(authUsername, accountRepository, roleHierarchy);
        var grade = subjectRepository.findById(targetId).orElseThrow();

        var perms = service.getPermissions(auth, grade);
        assertThat(perms).containsExactlyInAnyOrderElementsOf(expected);

        // verify hasPermission matches membership in expected set for all enum values used
        for (SubjectPermission p : SubjectPermission.values()) {
            boolean expectedHas = expected.contains(p);
            assertThat(service.hasPermission(auth, grade, p)).isEqualTo(expectedHas);
        }
    }

    static Stream<Arguments> permissionCases() {
        var all = Arrays.stream(SubjectPermission.values()).collect(Collectors.toSet());

        var teacherPerms = Set.of(
                SubjectPermission.VIEW,
                SubjectPermission.USERS_UPDATE,
                SubjectPermission.GRADE_CREATE,
                SubjectPermission.EDIT
        );
        var viewOnly = Set.of(SubjectPermission.VIEW);
        Set<SubjectPermission> none = Set.of();

        return Stream.of(
                // Student can only see the subject
                Arguments.of("the_reeves", 0, viewOnly),

                // Teachers can see, update, manage grades and manage accounts within subject
                Arguments.of("johndep", 0, teacherPerms),

                // Students can't see subject contents they are not part of
                Arguments.of("alice", 0, none),

                // Teachers can't see subject contents they are not part of
                Arguments.of("maria.santos", 0, none),

                // Admins have all privileges
                Arguments.of("admin", 0, all),
                Arguments.of("admin", 1, all)
        );
    }

}
