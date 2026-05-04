package io.github.siniarski.viruni.security;

import org.springframework.security.core.GrantedAuthority;

public enum Authority implements GrantedAuthority {
    SUBJECT_CREATE,
    SUBJECT_VIEW,
    SUBJECT_UPDATE,
    SUBJECT_DELETE,

    GRADE_CREATE,
    GRADE_VIEW,
    GRADE_DELETE,
    GRADE_UPDATE,

    ACCOUNT_VIEW,
    ACCOUNT_UPDATE,
    ACCOUNT_UPDATE_CREDENTIALS,
    ACCOUNT_DELETE,

    BROWSE_SUBJECTS,
    SUBJECT_MANAGEMENT,
    GRADE_MANAGEMENT,
    CREATE_TEACHER_TOKEN;

    @Override
    public String getAuthority() {
        return this.name();
    }
}
