package io.github.siniarski.viruni.security.jwt;

import io.github.siniarski.viruni.security.auth.AccountPrinciple;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtProvider {
    @Value("${io.github.siniarski.iwa2025.final_project.jwtSecret}")
    private String jwtSecret;

    @Value("${io.github.siniarski.iwa2025.final_project.jwtExpiration}")
    private long jwtExpiration;

    public static SecretKey signingKey;

    public static JwtParser parser;

    @PostConstruct
    public void init() {
        signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        parser = Jwts.parser().verifyWith(signingKey).build();
    }

    public JwtDetails generateJwtToken(Authentication authentication) {
        AccountPrinciple principle = (AccountPrinciple) authentication.getPrincipal();

        Date issuedAt = new Date();
        Date expires = new Date(issuedAt.getTime() + jwtExpiration*1000);

        String token = Jwts.builder()
                .subject(principle.getUsername())
                .issuedAt(issuedAt)
                .expiration(expires)
                .signWith(signingKey)
                .compact();

        return new JwtDetails(token, expires.toInstant());
    }

    public String checkJwtTokenErrors(String authToken) {
        try {
            parser.parseSignedClaims(authToken);
        } catch (SignatureException e) {
            return "Invalid JWT signature: " + e.getMessage();
        } catch (MalformedJwtException e) {
            return "Invalid JWT token: " + e.getMessage();
        } catch (ExpiredJwtException e) {
            return "JWT token expored: " + e.getMessage();
        } catch (UnsupportedJwtException e) {
            return "Unsupported JWT token: " + e.getMessage();
        } catch (IllegalArgumentException e) {
            return "JWT claims are empty: " + e.getMessage();
        }

        return null;
    }

    public String getUsernameFromToken(String token) throws JwtException, IllegalArgumentException {
        return parser.parseSignedClaims(token).getPayload().getSubject();
    }

    public Instant getExpiryDate(String token) throws JwtException, IllegalArgumentException  {
        return parser.parseSignedClaims(token).getPayload().getExpiration().toInstant();
    }
}
