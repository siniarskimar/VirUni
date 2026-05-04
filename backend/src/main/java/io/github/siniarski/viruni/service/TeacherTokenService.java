package io.github.siniarski.viruni.service;

import io.jsonwebtoken.impl.lang.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
public class TeacherTokenService {
    private final SecureRandom secureRandom;
    private final Base64.Encoder base64Encoder;

    @Autowired
    public TeacherTokenService() throws NoSuchAlgorithmException {
        this.base64Encoder = Base64.getEncoder();
        this.secureRandom = SecureRandom.getInstance("NativePRNG");
    }

    public String createToken() throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        int randomInt = secureRandom.nextInt();
        Instant now = Instant.now();
        long nowSeconds = now.getEpochSecond();

        digest.update(Bytes.toBytes(randomInt));
        String token = Base64.getEncoder().encodeToString(digest.digest(Bytes.toBytes(nowSeconds)));

        return token;
    }
}
