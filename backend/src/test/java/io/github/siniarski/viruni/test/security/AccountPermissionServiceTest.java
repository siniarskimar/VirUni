package io.github.siniarski.viruni.test.security;

import io.github.siniarski.viruni.model.Account;
import io.github.siniarski.viruni.model.AccountRole;
import io.github.siniarski.viruni.repository.AccountRepository;
import io.github.siniarski.viruni.security.AccountPermissionService;
import io.github.siniarski.viruni.security.Authority;
import io.github.siniarski.viruni.security.auth.AccountDetailsServiceImpl;
import io.github.siniarski.viruni.test.TestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@Import(TestConfig.class)
public class AccountPermissionServiceTest {
    @Autowired
    private AccountPermissionService service;

    @Autowired
    private DaoAuthenticationProvider daoAuthenticationProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserDetailsService accountDetailsService;

    @MockitoBean
    private AccountRepository accountRepository;


    @BeforeEach
    void beforeEach() {
        List<Account> mockAccounts = List.of(
                new Account(
                        0,
                        "admin",
                        passwordEncoder.encode("admin"),
                        "SYSTEM",
                        "ADMIN",
                        AccountRole.ADMIN
                ),
                new Account(
                        1,
                        "johndep",
                        passwordEncoder.encode("arasaka"),
                        "John",
                        "Depp",
                        AccountRole.TEACHER
                ),
                new Account(
                        2,
                        "the_reeves",
                        passwordEncoder.encode("beautiful"),
                        "Keanu",
                        "Reeves",
                        AccountRole.USER
                )
        );

        for(var acc : mockAccounts) {
            Mockito.when(accountRepository.findById(acc.getId())).thenReturn(Optional.of(acc));
            Mockito.when(accountRepository.findByUsername(acc.getUsername())).thenReturn(Optional.of(acc));
        }

        SecurityContextHolder.getContext().setAuthentication(null);
    }

    Authentication authenticateAs(String username, String password) {
        Authentication authentication = daoAuthenticationProvider.authenticate(
                new UsernamePasswordAuthenticationToken(
                        username,
                        password
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return authentication;
    }

    @ParameterizedTest
    @CsvSource({"the_reeves,beautiful", "johndep,arasaka", "admin,admin"})
    public void testPermissions_self(String username, String password) {
        var auth = authenticateAs(username,password);
        var acc = accountRepository.findByUsername(username).orElseThrow();

        assertThat(service.getPermissions(auth, acc))
                .isEqualTo(new HashSet<>(List.of(
                        Authority.ACCOUNT_VIEW,
                        Authority.ACCOUNT_DELETE,
                        Authority.ACCOUNT_UPDATE,
                        Authority.ACCOUNT_UPDATE_CREDENTIALS
                )));

        assertTrue(service.hasPermission(auth, acc, Authority.ACCOUNT_VIEW));
        assertTrue(service.hasPermission(auth, acc, Authority.ACCOUNT_DELETE));

        assertTrue(service.hasPermission(auth, acc, Authority.ACCOUNT_UPDATE));
        assertTrue(service.hasPermission(auth, acc, Authority.ACCOUNT_UPDATE_CREDENTIALS));
    }

    @ParameterizedTest
    @CsvSource({"johndep", "admin"})
    public void testPermissions_other(String otherUsername) {
        var auth = authenticateAs("the_reeves", "beautiful");
        var acc = accountRepository.findByUsername(otherUsername).orElseThrow();

        assertThat(service.getPermissions(auth, acc))
                .isEqualTo(new HashSet<>(List.of(
                        Authority.ACCOUNT_VIEW
                )));

        assertTrue(service.hasPermission(auth, acc, Authority.ACCOUNT_VIEW));
        assertFalse(service.hasPermission(auth, acc, Authority.ACCOUNT_DELETE));

        assertFalse(service.hasPermission(auth, acc, Authority.ACCOUNT_UPDATE));
        assertFalse(service.hasPermission(auth, acc, Authority.ACCOUNT_UPDATE_CREDENTIALS));
    }

    @ParameterizedTest
    @CsvSource({"the_reeves", "johndep"})
    public void testPermissions_admin(String otherUsername) {
        var auth = authenticateAs("admin", "admin");
        var acc = accountRepository.findByUsername(otherUsername).orElseThrow();

        assertThat(service.getPermissions(auth, acc))
                .isEqualTo(new HashSet<>(List.of(
                        Authority.ACCOUNT_VIEW,
                        Authority.ACCOUNT_DELETE,
                        Authority.ACCOUNT_UPDATE,
                        Authority.ACCOUNT_UPDATE_CREDENTIALS
                )));

        assertTrue(service.hasPermission(auth, acc, Authority.ACCOUNT_VIEW));
        assertTrue(service.hasPermission(auth, acc, Authority.ACCOUNT_DELETE));

        assertTrue(service.hasPermission(auth, acc, Authority.ACCOUNT_UPDATE));
        assertTrue(service.hasPermission(auth, acc, Authority.ACCOUNT_UPDATE_CREDENTIALS));
    }
}
