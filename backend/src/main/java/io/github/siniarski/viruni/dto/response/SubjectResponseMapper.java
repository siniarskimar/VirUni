package io.github.siniarski.viruni.dto.response;

import io.github.siniarski.viruni.model.ParticipantRole;
import io.github.siniarski.viruni.model.Subject;
import io.github.siniarski.viruni.security.permission.SubjectPermission;

import java.util.Set;
import java.util.stream.Collectors;

public class SubjectResponseMapper {

    public static SubjectResponse from(Subject subject, Set<SubjectPermission> permissionSet) {
        return new SubjectResponse(
                subject.getId(),
                subject.getName(),
                subject.getDescription(),
                subject.getParticipants().stream()
                        .filter(p -> p.getRole().equals(ParticipantRole.LEADING_TEACHER))
                        .map(p -> AccountResponseMapper.from(p.getParticipant(), null))
                        .collect(Collectors.toUnmodifiableList()),
                subject.getCreatedAt(),
                permissionSet
        );
    }
}
