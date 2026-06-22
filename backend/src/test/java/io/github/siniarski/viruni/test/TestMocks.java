package io.github.siniarski.viruni.test;

import io.github.siniarski.viruni.model.*;
import io.github.siniarski.viruni.repository.AccountRepository;
import io.github.siniarski.viruni.repository.GradeRepository;
import io.github.siniarski.viruni.repository.SubjectParticipantRepository;
import io.github.siniarski.viruni.repository.SubjectRepository;
import io.github.siniarski.viruni.service.RoleHierarchyService;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class TestMocks {
    public static void stubAccountRepositoryByUsername(AccountRepository accountRepository,
                                             List<Account> accounts) {
        Map<String, Account> usernameIndex = accounts.stream()
                .collect(Collectors.toMap(
                        Account::getUsername, // Extract username as key
                        a -> a, // Keep values as is
                        (a,b) -> b) // On duplicates, drop previous
                );

        Mockito.when(accountRepository.findByUsername(ArgumentMatchers.anyString()))
                .thenAnswer(inv -> {
                    String query = inv.getArgument(0);
                    Account account = usernameIndex.getOrDefault(query, null);
                    if(account == null) return Optional.empty();
                    return Optional.of(account);
                });
    }

    public static void stubAccountRepositoryById(AccountRepository accountRepository,
                                                 List<Account> accounts) {
        Map<Long, Account> idIndex = accounts.stream()
                .collect(Collectors.toMap(
                        Account::getId,
                        a -> a,
                        (a,b) -> b)
                );

        Mockito.when(accountRepository.findById(ArgumentMatchers.anyLong()))
                .thenAnswer(inv -> {
                    Long query = inv.getArgument(0);
                    Account account = idIndex.getOrDefault(query, null);
                    if(account == null) return Optional.empty();
                    return Optional.of(account);
                });
    }

    public static void stubGradeRepositoryById(GradeRepository gradeRepository,
                                               List<Grade> grades) {
        Map<Long, Grade> idIndex = grades.stream()
                .collect(Collectors.toMap(
                        Grade::getId,
                        v -> v,
                        (o, n) -> n
                ));

        Mockito.when(gradeRepository.findById(ArgumentMatchers.anyLong()))
                .thenAnswer(inv -> {
                    Long query = inv.getArgument(0);
                    Grade grade = idIndex.getOrDefault(query, null);
                    if(grade == null) return Optional.empty();
                    return Optional.of(grade);
                });
    }

    public static void stubSubjectRepositoryById(SubjectRepository subjectRepository,
                                                 List<Subject> subject) {
        Map<Long, Subject> idIndex = subject.stream()
                .collect(Collectors.toMap(
                        Subject::getId,
                        v -> v,
                        (o, n) -> n
                ));

        Mockito.when(subjectRepository.findById(ArgumentMatchers.anyLong()))
                .thenAnswer(inv -> {
                    Long query = inv.getArgument(0);
                    Subject grade = idIndex.getOrDefault(query, null);
                    if(grade == null) return Optional.empty();
                    return Optional.of(grade);
                });
    }

    public static void stubSubjectParticipantRepositoryById(SubjectParticipantRepository repository,
                                                            List<SubjectParticipant> participants) {
        Map<SubjectParticipantId, SubjectParticipant> idIndex = participants.stream()
                .collect(Collectors.toMap(
                        SubjectParticipant::getId,
                        p -> p,
                        (o, n) -> n
                ));

        Mockito.lenient().when(repository.findById(ArgumentMatchers.any(SubjectParticipantId.class)))
                .thenAnswer(inv -> {
                    SubjectParticipantId query = inv.getArgument(0);
                    var participant = idIndex.getOrDefault(query, null);
                    if(participant == null) return Optional.empty();
                    return Optional.of(participant);
                });
    }

}
