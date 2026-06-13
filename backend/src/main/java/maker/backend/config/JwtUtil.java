package maker.backend.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Utilitaire JWT : génération, extraction et validation des tokens.
 * Utilise HMAC-SHA256 avec une clé secrète configurée dans application.properties.
 */
@Component
public class JwtUtil {

    // Injection directe sur champ pour éviter les problèmes de RestartClassLoader devtools
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    /** Construit la clé HMAC à partir du secret (lazy, appelé à l'usage) */
    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /** Génère un token JWT signé avec le username comme sujet */
    public String genererToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /** Extrait le username depuis un token */
    public String extraireUsername(String token) {
        return getClaims(token).getSubject();
    }

    /** Vérifie que le token est valide (signature + expiration) */
    public boolean estValide(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
