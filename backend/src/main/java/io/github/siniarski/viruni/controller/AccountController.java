package io.github.siniarski.viruni.controller;

import io.github.siniarski.viruni.RestResponse;
import io.github.siniarski.viruni.dto.response.PagedResponse;
import io.github.siniarski.viruni.dto.request.UpdateAccountRequest;
import io.github.siniarski.viruni.model.Account;
import io.github.siniarski.viruni.model.AccountRole;
import io.github.siniarski.viruni.model.Grade;
import io.github.siniarski.viruni.repository.AccountRepository;
import io.github.siniarski.viruni.repository.AccountSpecification;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/account")
public class AccountController {
    private final AccountRepository accountRepository;

    @Autowired
    public AccountController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @GetMapping
    public PagedResponse<Account> getMany(@PageableDefault Pageable pageable,
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

        if(specs.isEmpty()) return new PagedResponse<>(this.accountRepository.findAll(pageable));
        return new PagedResponse<>(this.accountRepository.findAll(Specification.allOf(specs), pageable));
    }

    @GetMapping("/{identifier}")
    public ResponseEntity<Account> getOne(@PathVariable String identifier) {
        Account account = null;


        try {
            if(identifier.matches("\\d+")) {
                long id = Long.parseLong(identifier);
                account = accountRepository.findById(id).orElse(null);
            }
        } catch (NumberFormatException ignored) {

        } finally {
            if(account == null) account = accountRepository.findByUsername(identifier).orElse(null);
        }

        if(account == null) return RestResponse.notFound();

        return RestResponse.ok(account);
    }

    private ResponseEntity<?> delete(Account account) {
        if (account == null) return RestResponse.notFound();
        if (account.getRole().equals(AccountRole.ADMIN) && accountRepository.countByRole(AccountRole.ADMIN) == 1)
            return RestResponse.forbidden("cannot delete a single left admin");

        accountRepository.delete(account);
        return RestResponse.noContent();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteById(@PathVariable long id) {
        return delete(accountRepository.findById(id).orElse(null));
    }

    @DeleteMapping
    public ResponseEntity<?> deleteSelf(Authentication auth) {
        return delete(accountRepository.findByUsername(auth.getName()).orElse(null));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> updateOne(@PathVariable long id, @Valid @RequestBody UpdateAccountRequest form) {
        Account account = accountRepository.findById(id).orElse(null);
        if(account == null) return RestResponse.notFound();

        if(form.getFirstname() != null) {
            account.setFirstname(form.getFirstname());
        }

        if(form.getLastname() != null) {
            account.setLastname(form.getLastname());
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
