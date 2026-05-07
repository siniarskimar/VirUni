package io.github.siniarski.viruni.controller;

import io.github.siniarski.viruni.RestResponse;
import io.github.siniarski.viruni.dto.response.AccountResponse;
import io.github.siniarski.viruni.dto.response.PagedResponse;
import io.github.siniarski.viruni.dto.request.UpdateAccountRequest;
import io.github.siniarski.viruni.model.Account;
import io.github.siniarski.viruni.model.AccountRole;
import io.github.siniarski.viruni.model.Grade;
import io.github.siniarski.viruni.repository.AccountRepository;
import io.github.siniarski.viruni.repository.AccountSpecification;
import io.github.siniarski.viruni.security.permission.AccountPermission;
import io.github.siniarski.viruni.security.permission.AccountPermissionService;
import io.github.siniarski.viruni.security.Authority;
import io.github.siniarski.viruni.security.permission.PermissionEvaluator;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@RestController
@RequestMapping("/account")
public class AccountController {
    private final AccountRepository accountRepository;
    private final PermissionEvaluator permissionEvaluator;
    private final AccountPermissionService accountPermissionService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AccountController(AccountRepository accountRepository,
                             PermissionEvaluator permissionEvaluator,
                             AccountPermissionService accountPermissionService,
                             PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.permissionEvaluator = permissionEvaluator;
        this.accountPermissionService = accountPermissionService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public PagedResponse<AccountResponse> getMany(@PageableDefault Pageable pageable,
                                          @RequestParam(required = false) AccountRole role,
                                          @RequestParam(required = false) String query,
                                          @RequestParam(required = false) Long subjectId) {
        List<Specification<Account>> specs = new ArrayList<>();

        if(role != null) specs.add(AccountSpecification.hasRole(role));
        if(query != null) {
            specs.add(Specification.anyOf(
                    AccountSpecification.firstnameContains(query),
                    AccountSpecification.lastnameContains(query)
            ));
        }
        if(subjectId != null) specs.add(AccountSpecification.isParticipantOf(subjectId));

        Function<Account, AccountResponse> mapper = acc -> new AccountResponse(
                acc.getId(),
                acc.getUsername(),
                acc.getFirstname(),
                acc.getLastname(),
                null
        );

        if(specs.isEmpty())
            return new PagedResponse<>(this.accountRepository.findAll(pageable).map(mapper));

        return new PagedResponse<>(this.accountRepository.findAll(Specification.allOf(specs), pageable).map(mapper));
    }

    @GetMapping("/{identifier}")
    public ResponseEntity<?> getOne(@PathVariable long identifier) {
        Account account = accountRepository.findById(identifier).orElse(null);
        if(account == null) return RestResponse.notFound();
        if(!permissionEvaluator.hasPermission(account, AccountPermission.VIEW))
            return RestResponse.forbidden("you don't have permission to view this account");

        return RestResponse.ok(new AccountResponse(
                account.getId(),
                account.getUsername(),
                account.getFirstname(),
                account.getLastname(),
                accountPermissionService.getPermissions(account)
        ));
    }

    private ResponseEntity<?> delete(Authentication auth, Account account) {
        if (account == null) return RestResponse.notFound();

        if(!permissionEvaluator.hasPermission(auth, account, Authority.ACCOUNT_DELETE))
            return RestResponse.forbidden("you don't have permission to delete this account");

        if (account.getRole().equals(AccountRole.ADMIN) && accountRepository.countByRole(AccountRole.ADMIN) == 1)
            return RestResponse.forbidden("cannot delete a single left admin");

        accountRepository.delete(account);
        return RestResponse.noContent();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteById(Authentication auth, @PathVariable long id) {
        return delete(auth, accountRepository.findById(id).orElse(null));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateOne(@PathVariable long id,
                                            @Valid @RequestBody UpdateAccountRequest reqBody) {
        Account account = accountRepository.findById(id).orElse(null);
        if(account == null) return RestResponse.notFound();

        var canEdit = permissionEvaluator.hasPermission(account, AccountPermission.EDIT);
        if(!canEdit)
            return RestResponse.forbidden("you are not permitted to update information on this account");

        var canEditCredentials = permissionEvaluator.hasPermission(account, AccountPermission.EDIT_CREDENTIALS);
        if(reqBody.password() != null && !canEditCredentials)
            return RestResponse.forbidden("you are not permitted to update credential information on this account");

        if(reqBody.firstname() != null) {
            account.setFirstname(reqBody.firstname());
        }

        if(reqBody.lastname() != null) {
            account.setLastname(reqBody.lastname());
        }

        if(reqBody.password() != null) {
            account.setPassword(passwordEncoder.encode(reqBody.password()));
        }

        accountRepository.save(account);
        return RestResponse.ok(account);
    }

    @GetMapping("/{id}/grade")
    public ResponseEntity<List<Grade>> getGrades(@PathVariable long id) {
        Account student = accountRepository.findById(id).orElse(null);
        if(student == null) return RestResponse.notFound();

        return RestResponse.ok(student.getReceivedGrades());
    }
}
