package io.github.siniarski.viruni.controller;

import io.github.siniarski.viruni.RestResponse;
import io.github.siniarski.viruni.service.TeacherTokenService;
import io.github.siniarski.viruni.dto.CreateTeacherTokenForm;
import io.github.siniarski.viruni.model.TeacherRegistrationToken;
import io.github.siniarski.viruni.repository.TeacherRegistrationTokenRegistry;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/teacher-token")
public class TeacherTokenController {
    private final TeacherRegistrationTokenRegistry teacherRegistrationTokenRegistry;
    private final TeacherTokenService teacherTokenService;

    @Autowired
    public TeacherTokenController(TeacherRegistrationTokenRegistry teacherRegistrationTokenRegistry,
                                  TeacherTokenService teacherTokenService) {
        this.teacherRegistrationTokenRegistry = teacherRegistrationTokenRegistry;
        this.teacherTokenService = teacherTokenService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<TeacherRegistrationToken> getAll() {
        return teacherRegistrationTokenRegistry.findAll();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TeacherRegistrationToken> createTeacherToken(@RequestBody @Valid CreateTeacherTokenForm form) {
        try {
            Instant expires = form.getExpires();
            if(expires == null) expires = Instant.now().plusSeconds(24 * 60 * 60);

            TeacherRegistrationToken token = new TeacherRegistrationToken();
            token.setToken(teacherTokenService.createToken());
            token.setReusable(form.getReusable());
            token.setExpires(expires);

            teacherRegistrationTokenRegistry.save(token);
            return RestResponse.ok(token);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
