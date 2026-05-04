package io.github.siniarski.viruni.dto;

import io.github.siniarski.viruni.security.JwtDetails;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private long tokenExpires;
    private long accountId;
    private String username;
    private Collection<String> authorities;

    protected JwtResponse() {}

    public JwtResponse(JwtDetails jwtDetails,
                       String username,
                       long accountId,
                       Collection<? extends GrantedAuthority> authorities) {
        this.token = jwtDetails.getToken();
        this.tokenExpires = jwtDetails.getExpires().getEpochSecond();
        this.username = username;
        this.accountId = accountId;
        this.authorities = authorities.stream().map(GrantedAuthority::getAuthority).toList();
    }

    public String getToken() {
        return token;
    }

    public String getType() {
        return type;
    }

    public String getUsername() {
        return username;
    }

    public Collection<String> getAuthorities() {
        return authorities;
    }

    public long getAccountId() {
        return accountId;
    }

    public long getTokenExpires() {
        return tokenExpires;
    }
}
