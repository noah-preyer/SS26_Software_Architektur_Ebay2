package group5.ebay2.auth;

import group5.ebay2.auth.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final SecretKey secretKey;
    private final long expirationMinutes;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-minutes}") long expirationMinutes
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMinutes = expirationMinutes;
        log.info("JwtService initialized — expirationMinutes={}", expirationMinutes);
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(expirationMinutes * 60);

        String token = Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("username", user.getUsername())
                .claim("email", user.getEmail())
                .claim("userId", user.getId().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();

        log.debug("JWT generated — userId={}, expiresAt={}", user.getId(), expiresAt);
        return token;
    }
}