package com.example.backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.Date;

@Component
public class MagicJwt {

    @Value("${app.magic.secret}")
    private String secret;

    @Value("${app.magic.ttlMinutes:60}")
    private long ttl;

    public String create(String email, Long roundId) {
        Instant now = Instant.now();
        Instant exp = now.plus(Duration.ofMinutes(ttl));
        return Jwts.builder()
                .setSubject(email)                // sub = email
                .claim("rid", roundId)            // round id
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
    }

    public ClaimsData parse(String jwt) throws JwtException {
        var claims = Jwts.parserBuilder()
                .setSigningKey(secret.getBytes(StandardCharsets.UTF_8))
                .build()
                .parseClaimsJws(jwt)
                .getBody();
        String email = claims.getSubject();
        Long rid = claims.get("rid", Number.class).longValue();
        Instant exp = claims.getExpiration().toInstant();
        return new ClaimsData(email, rid, exp);
    }

    public record ClaimsData(String email, Long roundId, Instant exp) {}
}
