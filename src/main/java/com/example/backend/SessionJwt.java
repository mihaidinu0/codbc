package com.example.backend;

package com.example.backend.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Component
public class SessionJwt {

    @Value("${app.session.secret}")
    private String secret;

    @Value("${app.session.ttlMinutes:4320}") // default 3 zile
    private long ttlMinutes;

    public String create(String email) {
        Instant now = Instant.now();
        Instant exp = now.plus(Duration.ofMinutes(ttlMinutes));

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
    }
}
