package io.github.siniarski.viruni.test;

import io.github.siniarski.viruni.repository.AccountRepository;
import io.github.siniarski.viruni.security.permission.AccountPermissionService;
import io.github.siniarski.viruni.security.auth.AccountDetailsServiceImpl;
import io.github.siniarski.viruni.service.RoleHierarchyService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@TestConfiguration
public class TestConfig {

    @Bean
    DaoAuthenticationProvider authProvider(UserDetailsService userDetailsService,
                                           PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AccountPermissionService accountPermissionService(RoleHierarchyService roleHierarchyService) {
        return new AccountPermissionService(roleHierarchyService);
    }

    @Bean
    public UserDetailsService userDetailsService(RoleHierarchy roleHierarchy,
                                                 AccountRepository accountRepository) {
        return new AccountDetailsServiceImpl(accountRepository, roleHierarchy);
    }

    @Bean
    static RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.withDefaultRolePrefix()
                .role("ADMIN").implies("TEACHER")
                .role("TEACHER").implies("USER")
                .build();
    }

    @Bean
    public RoleHierarchyService roleHierarchyService(RoleHierarchy roleHierarchy,
                                                     UserDetailsService userDetailsService) {
        return new RoleHierarchyService(
                roleHierarchy,
                userDetailsService
        );
    }
}
