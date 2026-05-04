package io.github.siniarski.viruni.controller;

import io.github.siniarski.viruni.RestResponse;
import io.github.siniarski.viruni.dto.AssignGradeForm;
import io.github.siniarski.viruni.dto.PageResponse;
import io.github.siniarski.viruni.dto.UpdateGradeForm;
import io.github.siniarski.viruni.model.*;
import io.github.siniarski.viruni.repository.*;
import io.github.siniarski.viruni.model.Account;
import io.github.siniarski.viruni.model.AccountRole;
import io.github.siniarski.viruni.model.Grade;
import io.github.siniarski.viruni.model.Subject;
import io.github.siniarski.viruni.repository.AccountRepository;
import io.github.siniarski.viruni.repository.GradeRepository;
import io.github.siniarski.viruni.repository.GradeSpecification;
import io.github.siniarski.viruni.repository.SubjectRepository;
import io.github.siniarski.viruni.service.RoleHierarchyService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/grade")
@PreAuthorize("isAuthenticated()")
public class GradeController {
    private final GradeRepository gradeRepository;
    private final SubjectRepository subjectRepository;
    private final AccountRepository accountRepository;
    private final RoleHierarchyService roleHierarchyService;

    @Autowired
    public GradeController(GradeRepository gradeRepository,
                           SubjectRepository subjectRepository,
                           AccountRepository accountRepository,
                           RoleHierarchyService roleHierarchyService) {
        this.gradeRepository = gradeRepository;
        this.subjectRepository = subjectRepository;
        this.accountRepository = accountRepository;
        this.roleHierarchyService = roleHierarchyService;
    }

    @GetMapping
    public ResponseEntity<?> getMany(@PageableDefault(size = 10) Pageable pageable,
                                     @RequestParam(required = false) Long student,
                                     @RequestParam(required = false) Long teacher,
                                     @RequestParam(required = false) Long subject) {
        List<Specification<Grade>> specs = new ArrayList<>();
        if(student != null) specs.add(GradeSpecification.assignedToStudent(student));
        if(teacher != null) specs.add(GradeSpecification.assignedByTeacher(teacher));
        if(subject != null) specs.add(GradeSpecification.ofSubject(subject));

        if(specs.isEmpty()) return RestResponse.ok(new PageResponse<>(this.gradeRepository.findAll(pageable)));

        return RestResponse.ok(new PageResponse<>(this.gradeRepository.findAll(Specification.allOf(specs), pageable)));
    }

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> assignOne(@RequestBody @Valid AssignGradeForm gradeForm, Authentication auth) {
        Subject subject = subjectRepository.findById(gradeForm.getSubjectId()).orElse(null);
        Account student = accountRepository.findByRoleAndId(AccountRole.USER, gradeForm.getStudentId()).orElse(null);
        Account teacher = accountRepository.findById(gradeForm.getTeacherId()).orElse(null);

        if(subject == null) return RestResponse.badRequest("subject not found");
        if(student == null) return RestResponse.badRequest("student not found");
        if(teacher == null) return RestResponse.badRequest("teacher not found");

        if(!roleHierarchyService.hasRoleImplied(AccountRole.TEACHER, teacher)) {
            return RestResponse.badRequest("requested teacher account is not a teacher");
        }

        if(!accountRepository.existsByIdAndSubjectsId(student.getId(), subject.getId())) {
            return RestResponse.badRequest("student is not a participant of the subject");
        }

        if(!accountRepository.existsByIdAndSubjectsId(teacher.getId(), subject.getId())) {
            return RestResponse.badRequest("teacher is not a participant of the subject");
        }

        if(auth.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals(AccountRole.ADMIN.getName()))
            && !auth.getName().equals(teacher.getUsername())) {
            return RestResponse.forbidden("can't assign a grade in the name of another teacher");
        }

        float value = gradeForm.getValue();
        if(value < 0) {
            return RestResponse.badRequest("grade value must have a positive value");
        }

        Grade grade = new Grade(subject, student, teacher, gradeForm.getValue());

        gradeRepository.save(grade);
        return RestResponse.created(grade);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> deleteOne(@PathVariable long id, Authentication auth) {
        Grade grade = gradeRepository.findById(id).orElse(null);
        if(grade == null) return RestResponse.notFound();

        if(!accountRepository.existsByUsernameAndSubjectsId(auth.getName(), grade.getSubject().getId())
            && auth.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals(AccountRole.ADMIN.getName()))) {
            return RestResponse.forbidden("can't delete grade from subject which you are not part of");
        }

        gradeRepository.delete(grade);
        return RestResponse.noContent();
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> updateOne(@PathVariable long id,
                                           @Valid @RequestBody UpdateGradeForm form,
                                           Authentication auth) {
        Grade grade = gradeRepository.findById(id).orElse(null);
        if(grade == null) return RestResponse.notFound();

        if(!accountRepository.existsByUsernameAndSubjectsId(auth.getName(), grade.getSubject().getId())
                && auth.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals(AccountRole.ADMIN.getName()))) {
            return RestResponse.forbidden("can't update grade from subject which you are not a part of");
        }

        if(form.getValue() != null) grade.setValue(form.getValue());

        gradeRepository.save(grade);
        return RestResponse.ok(grade);
    }
}
