package io.github.siniarski.viruni.security.permission;

public enum AccountPermission {
    /**
     * Allows to browse account details
     */
    VIEW,

    /**
     * Allows to delete the account
     */
    DELETE,

    /**
     * Allows to edit account details
     */
    EDIT,

    /**
     * Allows to edit credential details
     */
    EDIT_CREDENTIALS;
}
