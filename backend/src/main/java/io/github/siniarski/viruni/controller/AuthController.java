package io.github.siniarski.viruni.controller;

import io.github.siniarski.viruni.RestResponse;
import io.github.siniarski.viruni.security.auth.AccountPrinciple;
import io.github.siniarski.viruni.dto.request.ChangePasswordRequest;
import io.github.siniarski.viruni.dto.response.SignInResponse;
import io.github.siniarski.viruni.dto.request.SignInRequest;
import io.github.siniarski.viruni.dto.request.SignUpRequest;
import io.github.siniarski.viruni.model.Account;
import io.github.siniarski.viruni.model.AccountRole;
import io.github.siniarski.viruni.repository.AccountRepository;
import io.github.siniarski.viruni.security.jwt.JwtDetails;
import io.github.siniarski.viruni.security.jwt.JwtProvider;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.Instant;

@RestController
public class AuthController {
    private final AccountRepository accountRepository;
    private final DaoAuthenticationProvider daoAuthenticationProvider;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Autowired
    public AuthController(AccountRepository accountRepository,
                          DaoAuthenticationProvider daoAuthenticationProvider,
                          PasswordEncoder passwordEncoder,
                          JwtProvider jwtProvider) {
        this.accountRepository = accountRepository;
        this.daoAuthenticationProvider = daoAuthenticationProvider;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody SignInRequest signinRequest) {
        Authentication authentication = daoAuthenticationProvider.authenticate(
                new UsernamePasswordAuthenticationToken(
                        signinRequest.getUsername(),
                        signinRequest.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        JwtDetails jwtDetails = jwtProvider.generateJwtToken(authentication);
        AccountPrinciple userDetails = (AccountPrinciple) authentication.getPrincipal();

        return RestResponse.ok(new SignInResponse(jwtDetails, userDetails.getUsername(), userDetails.getId(), userDetails.getAuthorities()));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signupRequest) {
        if(accountRepository.existsByUsername(signupRequest.getUsername())) {
            return RestResponse.conflict("Username already taken");
        }

        String encodedPassword = passwordEncoder.encode(signupRequest.getPassword());
        Account account = new Account(
                                    signupRequest.getUsername(),
                                    encodedPassword,
                                    signupRequest.getFirstname(),
                                    signupRequest.getLastname(),
                                    signupRequest.getRole());

        accountRepository.save(account);

        return RestResponse.created(account);
    }

    @PostMapping("/account/password")
    public ResponseEntity<?> changePassword(Principal principal,
                                            @RequestBody @Valid ChangePasswordRequest form) {
        Account account = accountRepository.findByUsername(principal.getName()).orElse(null);
        if(account == null) return RestResponse.notFound("account not found");

        account.setPassword(passwordEncoder.encode(form.getPassword()));
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @PostMapping("/account/{id}/password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> changePasswordOfAccount(Principal principal,
                                                     @PathVariable long id,
                                                     @RequestBody @Valid ChangePasswordRequest form) {
        Account account = accountRepository.findById(id).orElse(null);
        if(account == null) return RestResponse.notFound();

        account.setPassword(passwordEncoder.encode(form.getPassword()));
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/auth/renew")
    public ResponseEntity<SignInResponse> renewToken(Authentication auth) {
        if(!accountRepository.existsByUsername(auth.getName())) {
            return RestResponse.notFound();
        }
        JwtDetails jwtDetails = jwtProvider.generateJwtToken(auth);
        AccountPrinciple userDetails = (AccountPrinciple) auth.getPrincipal();

        return RestResponse.ok(new SignInResponse(jwtDetails, userDetails.getUsername(), userDetails.getId(), userDetails.getAuthorities()));
    }

    @PostMapping("/account")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Account> createAccount(@RequestBody @Valid Account account) {
        account.setId(0);
        account.setPassword(passwordEncoder.encode(account.getPassword()));
        accountRepository.save(account);
        return RestResponse.ok(account);
    }
}
