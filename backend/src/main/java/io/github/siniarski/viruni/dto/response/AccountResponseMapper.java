package io.github.siniarski.viruni.dto.response;

import io.github.siniarski.viruni.model.Account;
import io.github.siniarski.viruni.security.permission.AccountPermission;

import java.util.Set;

public class AccountResponseMapper {

    public static AccountResponse from(Account account, Set<AccountPermission> permissionSet) {
        return new AccountResponse(
                account.getId(),
                account.getUsername(),
                account.getFirstname(),
                account.getLastname(),
                permissionSet
        );
    }
}
