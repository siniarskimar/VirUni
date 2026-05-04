package io.github.siniarski.viruni.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public class AssignAccountToSubjectForm {

    @NotNull
    @JsonProperty("account")
    private Long accountId;

    public long getAccountId() {
        return accountId;
    }

}
