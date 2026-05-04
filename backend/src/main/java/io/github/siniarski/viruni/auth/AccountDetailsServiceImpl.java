package io.github.siniarski.viruni.auth;

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
    @Autowired
    AccountRepository accountRepository;

    @Autowired
    RoleHierarchy roleHierarchy;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findByUsername(username).orElse(null);
        if(account == null) {
            throw new UsernameNotFoundException("Account with username '" + username + "' not found");
        }

        return AccountPrinciple.build(account, roleHierarchy);
    }
}
