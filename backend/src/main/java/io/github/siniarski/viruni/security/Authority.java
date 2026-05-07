package io.github.siniarski.viruni.security;

import org.springframework.security.core.GrantedAuthority;

public enum Authority implements GrantedAuthority {

    /**
     * Allows to create new subjects
     */
    SUBJECT_CREATE,

    /**
     * Allows to browse the list of subjects
     */
    SUBJECTS_READ,

    /**
     * Allows to renew the access token which requested renewal
     */
    TOKEN_RENEW,

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
