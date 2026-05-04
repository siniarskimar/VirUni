package io.github.siniarski.viruni.controller;

import io.github.siniarski.viruni.RestResponse;
import io.github.siniarski.viruni.auth.AccountPrinciple;
import io.github.siniarski.viruni.dto.ChangePasswordForm;
import io.github.siniarski.viruni.dto.JwtResponse;
import io.github.siniarski.viruni.dto.SignInForm;
import io.github.siniarski.viruni.dto.SignUpForm;
import io.github.siniarski.viruni.model.Account;
import io.github.siniarski.viruni.model.AccountRole;
import io.github.siniarski.viruni.model.TeacherRegistrationToken;
import io.github.siniarski.viruni.repository.AccountRepository;
import io.github.siniarski.viruni.repository.TeacherRegistrationTokenRegistry;
import io.github.siniarski.viruni.security.JwtDetails;
import io.github.siniarski.viruni.security.JwtProvider;

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
    private final TeacherRegistrationTokenRegistry teacherRegistrationTokenRegistry;

    @Autowired
    public AuthController(AccountRepository accountRepository,
                          DaoAuthenticationProvider daoAuthenticationProvider,
                          PasswordEncoder passwordEncoder,
                          JwtProvider jwtProvider,
                          TeacherRegistrationTokenRegistry teacherRegistrationTokenRegistry) {
        this.accountRepository = accountRepository;
        this.daoAuthenticationProvider = daoAuthenticationProvider;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
        this.teacherRegistrationTokenRegistry = teacherRegistrationTokenRegistry;
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody SignInForm signinRequest) {
        Authentication authentication = daoAuthenticationProvider.authenticate(
                new UsernamePasswordAuthenticationToken(
                        signinRequest.getUsername(),
                        signinRequest.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        JwtDetails jwtDetails = jwtProvider.generateJwtToken(authentication);
        AccountPrinciple userDetails = (AccountPrinciple) authentication.getPrincipal();

        return RestResponse.ok(new JwtResponse(jwtDetails, userDetails.getUsername(), userDetails.getId(), userDetails.getAuthorities()));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpForm signupRequest) {
        if(accountRepository.existsByUsername(signupRequest.getUsername())) {
            return RestResponse.conflict("Username already taken");
        }

        AccountRole role = signupRequest.getRole();
        if(role.equals(AccountRole.TEACHER)) {
            String tokenGiven = signupRequest.getTeacherRegistrationToken();
            if(tokenGiven == null) return RestResponse.badRequest("missing teacher registration token");

            TeacherRegistrationToken token = teacherRegistrationTokenRegistry.findByToken(tokenGiven).orElse(null);
            if(token == null) return RestResponse.badRequest("invalid teacher registration token");

            if(Instant.now().isAfter(token.getExpires())) {
                teacherRegistrationTokenRegistry.delete(token);
                return RestResponse.badRequest("token expired");
            }

            if(!token.isReusable()) {
                teacherRegistrationTokenRegistry.delete(token);
            }
        }

        String encodedPassword = passwordEncoder.encode(signupRequest.getPassword());
        Account account = new Account(
                                    signupRequest.getUsername(),
                                    encodedPassword,
                                    signupRequest.getFirstname(),
                                    signupRequest.getLastname(),
                                    role);

        accountRepository.save(account);

        return RestResponse.created(account);
    }

    @PostMapping("/account/password")
    public ResponseEntity<?> changePassword(Principal principal,
                                            @RequestBody @Valid ChangePasswordForm form) {
        Account account = accountRepository.findByUsername(principal.getName()).orElse(null);
        if(account == null) return RestResponse.notFound("account not found");

        account.setPassword(passwordEncoder.encode(form.getPassword()));
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @PostMapping("/account/{id}/password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> changePasswordOfAccount(Principal principal,
                                                     @PathVariable long id,
                                                     @RequestBody @Valid ChangePasswordForm form) {
        Account account = accountRepository.findById(id).orElse(null);
        if(account == null) return RestResponse.notFound();

        account.setPassword(passwordEncoder.encode(form.getPassword()));
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/auth/renew")
    public ResponseEntity<JwtResponse> renewToken(Authentication auth) {
        if(!accountRepository.existsByUsername(auth.getName())) {
            return RestResponse.notFound();
        }
        JwtDetails jwtDetails = jwtProvider.generateJwtToken(auth);
        AccountPrinciple userDetails = (AccountPrinciple) auth.getPrincipal();

        return RestResponse.ok(new JwtResponse(jwtDetails, userDetails.getUsername(), userDetails.getId(), userDetails.getAuthorities()));
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
