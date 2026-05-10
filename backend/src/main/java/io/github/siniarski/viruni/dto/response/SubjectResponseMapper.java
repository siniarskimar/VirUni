package io.github.siniarski.viruni.dto.response;

import io.github.siniarski.viruni.model.Subject;
import io.github.siniarski.viruni.security.permission.SubjectPermission;

import java.util.Set;

public class SubjectResponseMapper {

    public static SubjectResponse from(Subject subject, Set<SubjectPermission> permissionSet) {
        return new SubjectResponse(
                subject.getId(),
                subject.getName(),
                subject.getDescription(),
                AccountResponseMapper.from(subject.getLeadingTeacher(), null),
                subject.getCreatedAt(),
                permissionSet
        );
    }
}
