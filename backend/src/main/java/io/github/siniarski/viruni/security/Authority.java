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
;

    @Override
    public String getAuthority() {
        return this.name();
    }
}
