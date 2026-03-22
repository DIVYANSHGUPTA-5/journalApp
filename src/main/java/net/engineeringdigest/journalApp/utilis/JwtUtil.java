package net.engineeringdigest.journalApp.utilis;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    // ✅ Use strong & consistent key (must be >= 32 chars)
    private static final String SECRET_KEY = "TaK+HaV^uvCHEFsEVfypW#7g9^k*Z8$V";

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    // 🔹 Extract username
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    // 🔹 Extract expiration
    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    // 🔹 Extract all claims
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // 🔹 Check expiration
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // 🔹 Generate token
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }

    // 🔹 Create token
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .header().empty().add("typ", "JWT")
                .and()
                .issuedAt(new Date(System.currentTimeMillis()))
                // ✅ 1 hour expiry (fix comment mismatch)
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(getSigningKey())
                .compact();
    }

    // 🔹 Validate token (IMPORTANT FIX)
    public Boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    // 🔹 Optional (used by filter if needed)
    public Boolean validateToken(String token) {
        return !isTokenExpired(token);
    }
}