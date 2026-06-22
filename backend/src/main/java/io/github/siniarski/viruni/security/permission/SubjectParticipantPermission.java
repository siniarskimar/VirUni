package io.github.siniarski.viruni.security.permission;

public enum SubjectParticipantPermission {
    /**
     * Allows to create the association
     */
    CREATE,

    /**
     * Allows to view the association
     */
    READ,

    /**
     * Allows updating the metadata on the association
     */
    UPDATE,

    /**
     * Allows to delete the association
     */
    DELETE,
}
