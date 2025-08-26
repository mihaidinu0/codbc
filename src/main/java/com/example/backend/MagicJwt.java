@Component
public class MagicJwt {
    @Value("${app.magic.secret}") private String secret;
    @Value("${app.magic.ttlMinutes:60}") private long ttl;

    public String create(String email, Long roundId) {
        Instant now = Instant.now(), exp = now.plus(Duration.ofMinutes(ttl));
        return Jwts.builder()
                .setSubject(email)
                .claim("rid", roundId)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
    }

    public ClaimsData parse(String jwt) throws JwtException {
        var claims = Jwts.parserBuilder()
                .setSigningKey(secret.getBytes(StandardCharsets.UTF_8))
                .build().parseClaimsJws(jwt).getBody();
        return new ClaimsData(claims.getSubject(), claims.get("rid", Number.class).longValue());
    }
    public record ClaimsData(String email, Long roundId) {}
}
