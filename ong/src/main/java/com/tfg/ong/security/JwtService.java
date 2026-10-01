package com.tfg.ong.security;

import com.tfg.ong.model.Usuario;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(@Value("${app.jwt.secret:}") String secret,
                      @Value("${app.jwt.expiration-ms}") long expirationMs) {
        if (secret == null || secret.isBlank()) {
            // Sin JWT_SECRET (desarrollo): clave aleatoria generada en cada arranque.
            // Es segura, pero al reiniciar el backend los tokens anteriores dejan de valer (hay que volver a iniciar sesión).
            this.key = Jwts.SIG.HS256.key().build();
            log.warn("JWT_SECRET no está definida: se usa una clave aleatoria temporal. "
                    + "Define la variable de entorno JWT_SECRET (mínimo 32 caracteres) para mantener las sesiones tras reiniciar.");
        } else {
            byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
            if (bytes.length < 32) {
                throw new IllegalStateException("JWT_SECRET debe tener al menos 32 caracteres");
            }
            this.key = Keys.hmacShaKeyFor(bytes);
        }
        this.expirationMs = expirationMs;
    }

    /** Genera un token firmado cuyo "subject" es el id del usuario. */
    public String generateToken(Usuario usuario) {
        Date ahora = new Date();
        return Jwts.builder()
                .subject(String.valueOf(usuario.getId()))
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + expirationMs))
                .signWith(key)
                .compact();
    }

    /** Devuelve el id del usuario si el token es válido y no ha caducado; vacío en caso contrario. */
    public Optional<Long> extractUserId(String token) {
        try {
            String subject = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
            return Optional.of(Long.parseLong(subject));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
