package io.github.siniarski.viruni.security.jwt;

import java.time.Instant;

public class JwtDetails {
    private String token;
    private Instant expires;

    public JwtDetails(String token, Instant expires) {
        this.token = token;
        this.expires = expires;
    }

    public String getToken() {
        return token;
    }

    public Instant getExpires() {
        return expires;
    }
}
