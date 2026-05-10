package io.github.siniarski.viruni.dto.request;

public record UpdateAccountRequest(
        String firstname,
        String lastname,
        String password
) {}
