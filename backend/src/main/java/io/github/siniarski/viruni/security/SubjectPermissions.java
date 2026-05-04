package io.github.siniarski.viruni.security;

public class SubjectPermissions {
    private boolean canManageGrades;
    private boolean canManageAccounts;
    private boolean canDelete;
    private boolean canUpdate;

    public SubjectPermissions() {
        this.canManageGrades = false;
        this.canManageAccounts = false;
        this.canDelete = false;
        this.canUpdate = false;
    }

    public boolean getCanManageGrades() {
        return canManageGrades;
    }

    public void setCanManageGrades(boolean canManageGrades) {
        this.canManageGrades = canManageGrades;
    }

    public boolean getCanManageAssignedAccounts() {
        return canManageAccounts;
    }

    public void setCanManageAccounts(boolean canManageAccounts) {
        this.canManageAccounts = canManageAccounts;
    }

    public boolean isCanManageGrades() {
        return canManageGrades;
    }

    public boolean isCanManageAccounts() {
        return canManageAccounts;
    }

    public boolean isCanDelete() {
        return canDelete;
    }

    public void setCanDelete(boolean canDelete) {
        this.canDelete = canDelete;
    }

    public boolean isCanUpdate() {
        return canUpdate;
    }

    public void setCanUpdate(boolean canUpdate) {
        this.canUpdate = canUpdate;
    }

    public void setAll(boolean value) {
        this.canUpdate = value;
        this.canDelete = value;
        this.canManageAccounts = value;
        this.canManageGrades = value;
    }
}
