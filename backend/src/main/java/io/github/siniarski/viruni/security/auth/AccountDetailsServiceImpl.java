package io.github.siniarski.viruni.security.auth;

import io.github.siniarski.viruni.model.Account;
import io.github.siniarski.viruni.repository.AccountRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AccountDetailsServiceImpl implements UserDetailsService {
    private final AccountRepository accountRepository;
    private final RoleHierarchy roleHierarchy;

    @Autowired
    public AccountDetailsServiceImpl(AccountRepository accountRepository, RoleHierarchy roleHierarchy) {
        this.accountRepository = accountRepository;
        this.roleHierarchy = roleHierarchy;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findByUsername(username).orElse(null);
        if(account == null) {
            throw new UsernameNotFoundException("Account with username '" + username + "' not found");
        }

        return AccountPrincipal.build(account, roleHierarchy);
    }
}
