package io.github.siniarski.viruni.test;

import io.github.siniarski.viruni.security.AccountPermissionService;
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
    DaoAuthenticationProvider authProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService());
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AccountPermissionService accountPermissionService() {
        return new AccountPermissionService();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new AccountDetailsServiceImpl();
    }

    @Bean
    static RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.withDefaultRolePrefix()
                .role("ADMIN").implies("TEACHER")
                .role("TEACHER").implies("USER")
                .build();
    }

    @Bean
    public RoleHierarchyService roleHierarchyService() {
        return new RoleHierarchyService(
                roleHierarchy(),
                userDetailsService()
        );
    }
}
