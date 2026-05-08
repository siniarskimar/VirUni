package io.github.siniarski.viruni.test;

import io.github.siniarski.viruni.model.Account;
import io.github.siniarski.viruni.model.AccountRole;
import io.github.siniarski.viruni.repository.AccountRepository;
import io.github.siniarski.viruni.service.RoleHierarchyService;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class TestMocks {
    public static void stubAccountRepositoryByUsername(AccountRepository accountRepository,
                                             List<Account> accounts) {
        Map<String, Account> usernameIndex = accounts.stream()
                .collect(Collectors.toMap(
                        Account::getUsername, // Extract username as key
                        a -> a, // Keep values as is
                        (a,b) -> b) // On duplicates, drop previous
                );

        Mockito.when(accountRepository.findByUsername(ArgumentMatchers.anyString()))
                .thenAnswer(inv -> {
                    String query = inv.getArgument(0);
                    Account account = usernameIndex.getOrDefault(query, null);
                    if(account == null) return Optional.empty();
                    return Optional.of(account);
                });
    }

    public static void stubAccountRepositoryById(AccountRepository accountRepository,
                                                 List<Account> accounts) {
        Map<Long, Account> idIndex = accounts.stream()
                .collect(Collectors.toMap(
                        Account::getId,
                        a -> a,
                        (a,b) -> b)
                );

        Mockito.when(accountRepository.findById(ArgumentMatchers.anyLong()))
                .thenAnswer(inv -> {
                    Long query = inv.getArgument(0);
                    Account account = idIndex.getOrDefault(query, null);
                    if(account == null) return Optional.empty();
                    return Optional.of(account);
                });

    }

}
