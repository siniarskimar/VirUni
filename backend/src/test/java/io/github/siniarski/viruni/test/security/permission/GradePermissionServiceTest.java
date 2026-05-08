package io.github.siniarski.viruni.test.security.permission;

import io.github.siniarski.viruni.model.Account;
import io.github.siniarski.viruni.model.AccountRole;
import io.github.siniarski.viruni.model.Grade;
import io.github.siniarski.viruni.model.Subject;
import io.github.siniarski.viruni.repository.AccountRepository;
import io.github.siniarski.viruni.repository.GradeRepository;
import io.github.siniarski.viruni.security.auth.AccountDetailsServiceImpl;
import io.github.siniarski.viruni.security.auth.AccountPrinciple;
import io.github.siniarski.viruni.security.permission.AccountPermission;
import io.github.siniarski.viruni.security.permission.AccountPermissionService;
import io.github.siniarski.viruni.security.permission.GradePermission;
import io.github.siniarski.viruni.security.permission.GradePermissionService;
import io.github.siniarski.viruni.service.RoleHierarchyService;
import io.github.siniarski.viruni.test.TestMocks;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static io.github.siniarski.viruni.test.TestUtils.authenticateAs;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class GradePermissionServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private GradeRepository gradeRepository;

    private UserDetailsService userDetailsService;
    private RoleHierarchy roleHierarchy;
    private RoleHierarchyService roleHierarchyService;
    private GradePermissionService service;

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

    private static final List<Grade> grades = List.of(
            new Grade(
                    0,
                    subjects.get(0),
                    accounts.get(2), // student: the_reeves
                    accounts.get(1), // grader: johndep
                    4.0f,
                    Instant.now()
            ),
            new Grade(
                    1,
                    subjects.get(1),
                    accounts.get(2), // the_reeves for Linear Algebra
                    accounts.get(4), // teacher: maria.santos
                    3.5f,
                    Instant.now()
            ),
            new Grade(
                    2,
                    subjects.get(1),
                    accounts.get(3), // alice for Linear Algebra
                    accounts.get(1),
                    4.5f,
                    Instant.now()
            )
    );

    static {
        subjects.get(0).getGrades().add(grades.get(0));
        subjects.get(1).getGrades().add(grades.get(1));
        subjects.get(1).getGrades().add(grades.get(2));
    }


    @BeforeEach
    void beforeEach() {
        roleHierarchy = RoleHierarchyImpl.withDefaultRolePrefix()
                .role("ADMIN").implies("TEACHER")
                .role("TEACHER").implies("USER")
                .build();

        userDetailsService = new AccountDetailsServiceImpl(accountRepository, roleHierarchy);
        roleHierarchyService = new RoleHierarchyService(roleHierarchy, userDetailsService);
        service = new GradePermissionService(roleHierarchyService);
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldDisallowUnauthenticated() {
        assertThat(service.getPermissions(null, grades.get(1)))
                .containsExactlyInAnyOrder();
    }

    @ParameterizedTest
    @MethodSource("permissionCases")
    void testPermissions(String authUsername, long targetId, Set<GradePermission> expected) {
        TestMocks.stubAccountRepositoryByUsername(accountRepository, accounts);
        TestMocks.stubGradeRepositoryById(gradeRepository, grades);

        var auth = authenticateAs(authUsername, accountRepository, roleHierarchy);
        var grade = gradeRepository.findById(targetId).orElseThrow();

        var perms = service.getPermissions(auth, grade);
        assertThat(perms).containsExactlyInAnyOrderElementsOf(expected);

        // verify hasPermission matches membership in expected set for all enum values used
        for (GradePermission p : GradePermission.values()) {
            boolean expectedHas = expected.contains(p);
            assertThat(service.hasPermission(auth, grade, p)).isEqualTo(expectedHas);
        }
    }

    static Stream<Arguments> permissionCases() {
        var all = Set.of(
                GradePermission.READ,
                GradePermission.DELETE,
                GradePermission.UPDATE
        );
        var viewOnly = Set.of(GradePermission.READ);
        Set<GradePermission> none = Set.of();

        return Stream.of(
                // Student can only see his grade
                Arguments.of("the_reeves", 0, viewOnly),
                // Teachers can see, update and delete (happens to be all)
                Arguments.of("johndep", 0, all),


                // Students can't see other students grades
                Arguments.of("alice", 0, none),

                // Teachers can't see grades from subjects they are not part of
                Arguments.of("maria.santos", 0, none),

                // Admins have all privileges
                Arguments.of("admin", 0, all),
                Arguments.of("admin", 1, all),
                Arguments.of("admin", 2, all)
        );
    }
}
