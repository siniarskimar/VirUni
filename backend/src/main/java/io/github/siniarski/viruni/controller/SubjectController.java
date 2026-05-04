package io.github.siniarski.viruni.controller;

import io.github.siniarski.viruni.RestResponse;
import io.github.siniarski.viruni.dto.*;
import io.github.siniarski.viruni.model.Account;
import io.github.siniarski.viruni.model.AccountRole;
import io.github.siniarski.viruni.model.Subject;
import io.github.siniarski.viruni.security.SubjectPermissionService;
import io.github.siniarski.viruni.security.SubjectPermissions;
import io.github.siniarski.viruni.dto.*;
import io.github.siniarski.viruni.service.RoleHierarchyService;
import io.github.siniarski.viruni.model.*;
import io.github.siniarski.viruni.repository.AccountRepository;
import io.github.siniarski.viruni.repository.SubjectRepository;
import io.github.siniarski.viruni.repository.SubjectSpecification;
import jakarta.validation.Valid;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/subject")
public class SubjectController {
    private final SubjectRepository subjectRepository;
    private final AccountRepository accountRepository;
    private final RoleHierarchyService roleHierarchyService;
    private final SubjectPermissionService subjectPermissionService;

    @Autowired
    public SubjectController(SubjectRepository subjectRepository,
                             AccountRepository accountRepository,
                             RoleHierarchyService roleHierarchyService,
                             SubjectPermissionService subjectPermissionService) {
        this.subjectRepository = subjectRepository;
        this.accountRepository = accountRepository;
        this.roleHierarchyService = roleHierarchyService;
        this.subjectPermissionService = subjectPermissionService;
    }

    @GetMapping
    public PageResponse<Subject> getMany(@PageableDefault() Pageable pageable,
                                         @RequestParam(required = false) Long leadingTeacher,
                                         @RequestParam(required = false) Long participant,
                                         @RequestParam(required = false) Instant createdAfter,
                                         @RequestParam(required = false) String query) {
        List<Specification<Subject>> specs = new ArrayList<>();

        if(participant != null) specs.add(SubjectSpecification.hasParticipant((participant)));
        if(createdAfter != null) specs.add(SubjectSpecification.createdAfter(createdAfter));
        if(leadingTeacher != null) specs.add(SubjectSpecification.hasLeadingTeacher(leadingTeacher));
        if(query != null) specs.add(SubjectSpecification.nameOrleadingTeacherFullnameContaining(query));

        if(specs.isEmpty()) return new PageResponse<>(subjectRepository.findAll(pageable));

        return new PageResponse<>(subjectRepository.findAll(Specification.allOf(specs), pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubjectDTO> getOne(@PathVariable long id, Authentication auth) {
        Subject subject = subjectRepository.findById(id).orElse(null);
        if(subject == null) return RestResponse.notFound();

        SubjectPermissions perms = this.subjectPermissionService.computePermissions(subject, auth);
        SubjectDTO responseObject = new SubjectDTO(subject, perms);
        return RestResponse.ok(responseObject);
    }

    private ResponseEntity<?> create(CreateSubjectForm form, Account leadingTeacher, Authentication auth) {
        Subject subject = new Subject(form.getName(), leadingTeacher);
        if(form.getParticipants() != null) {
            // TODO: move batch participant add to separate endpoint
            Set<Account> participants = new HashSet<>(accountRepository.findAllById(form.getParticipants()));
            Set<Long> foundParticipantsId = participants.stream().map(Account::getId).collect(Collectors.toSet());

            if(!foundParticipantsId.containsAll(form.getParticipants())) {
                return RestResponse.badRequest("some participants not found");
            }

            this.subjectPermissionService.evictFromCache(subject, participants);
            subject.getParticipants().addAll(participants);
        }

        if(form.getDescription() != null) subject.setDescription(form.getDescription());

        subject.getParticipants().add(leadingTeacher);
        this.subjectRepository.save(subject);

        SubjectPermissions perms = this.subjectPermissionService.computePermissions(subject, auth);
        SubjectDTO responseObject = new SubjectDTO(subject, perms);
        return RestResponse.created(responseObject);
    }

    private ResponseEntity<?> createOneByAdmin(CreateSubjectForm form, Authentication auth) {
        String leadingTeacherName = (form.getLeadingTeacherUsername() != null)
                                    ? form.getLeadingTeacherUsername()
                                    : auth.getName();

        Account leadingTeacher = this.accountRepository.findByUsername(leadingTeacherName).orElse(null);
        if(leadingTeacher == null)
            return RestResponse.badRequest("leading teacher not found");

        if(!roleHierarchyService.hasRoleImplied(AccountRole.TEACHER, leadingTeacher)) {
            return RestResponse.badRequest("leading teacher account must have a TEACHER role");
        }
        return create(form, leadingTeacher, auth);
    }

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> createOne(@RequestBody @Valid CreateSubjectForm subjectDTO, Authentication auth) {
        boolean hasAdminRole = roleHierarchyService.hasRoleImplied(AccountRole.ADMIN, auth);
        if(hasAdminRole) return createOneByAdmin(subjectDTO, auth);

        Account leadingTeacher = this.accountRepository.findByUsername(auth.getName()).orElse(null);
        return create(subjectDTO, leadingTeacher, auth);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> deleteOne(@PathVariable long id, Authentication auth) {
        Subject subject = this.subjectRepository.findById(id).orElse(null);
        if(subject == null) return RestResponse.notFound();

        if(!subjectPermissionService.hasDeletePermission(subject, auth)) {
            return RestResponse.forbidden("Insufficient permissions");
        }

        this.subjectRepository.delete(subject);
        return RestResponse.noContent();
    }

    @PostMapping("/{id}/account")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> assignAccount(@PathVariable long id, @Valid @RequestBody AssignAccountToSubjectForm form) {
        Subject subject = subjectRepository.findById(id).orElse(null);
        if(subject == null) return RestResponse.notFound();

        Account account = accountRepository.findById(form.getAccountId()).orElse(null);
        if(account == null) return RestResponse.badRequest("student not found");

        subjectPermissionService.evictFromCache(subject, account);
        subject.getParticipants().add(account);

        subjectRepository.save(subject);
        return RestResponse.ok();
    }

    @DeleteMapping("/{id}/account/{accountId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> removeParticipant(@PathVariable long id, @PathVariable long accountId) {

        Subject subject = subjectRepository.findById(id).orElse(null);
        if(subject == null) return RestResponse.notFound();

        Account account = accountRepository.findById(accountId).orElse(null);
        if(account == null) return RestResponse.notFound();

        subject.getParticipants().remove(account);

        subjectRepository.save(subject);
        return RestResponse.noContent();
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> update(@PathVariable long id, @Valid @RequestBody UpdateSubjectForm updates, Authentication auth) {
        Subject subject = subjectRepository.findById(id).orElse(null);
        if(subject == null) return RestResponse.notFound();

        if(updates.getName() != null) subject.setName(updates.getName());
        if(updates.getDescription() != null) subject.setDescription(updates.getDescription());

        subjectRepository.save(subject);


        SubjectPermissions perms = this.subjectPermissionService.computePermissions(subject, auth);
        SubjectDTO responseObject = new SubjectDTO(subject, perms);
        return RestResponse.ok(responseObject);
    }
}
