package io.github.siniarski.viruni.security.permission;

public enum SubjectPermission {
    /**
     * Allows to view subject's contents
     */
    VIEW,

    /**
     * Allows to delete subject
     */
    DELETE,

    /**
     * Allows to edit subject details
     */
    EDIT,

    /**
     * Allows to manage users within a subject
     */
    USERS_UPDATE,

    /**
     * Allows to assign grades within a subject
     */
    GRADE_CREATE,
}
