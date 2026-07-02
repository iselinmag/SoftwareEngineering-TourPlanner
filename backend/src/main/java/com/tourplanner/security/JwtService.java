package com.tourplanner.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

// this is the ticket machine.
// when someone logs in we hand them a small signed token (jwt, a login ticket the browser
// keeps and shows on every request). this class makes those tickets and reads them back.
// the signature is like a wax seal, if anyone changes the ticket the seal breaks and we spot it.
@Service
public class JwtService {

    // the secret used to sign tickets. comes from config, never hardcoded
    @Value("${jwt.secret}")
    private String secret;

    // how long a ticket stays valid, in milliseconds. here, one day
    private static final long EXPIRY = 1000 * 60 * 60 * 24;

    // turns our secret text into the key used to stamp and check the wax seal
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