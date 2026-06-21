package com.tourplanner.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    // the secret used to sign tickets. comes from config, never hardcoded
    @Value("${jwt.secret}")
    private String secret;

    // how long a ticket stays valid, in milliseconds. here, one day
    private static final long EXPIRY = 1000 * 60 * 60 * 24;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // make a ticket that says who this user is
    public String makeToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRY))
                .signWith(key())
                .compact();
    }

    // read the username back out of a ticket, and verify it was not faked
    public String readUsername(String token) {
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}