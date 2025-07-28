package org.example.Security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.example.Entity.Usuario;
import org.example.Exception.InvalidTokenException;
import org.example.Exception.JwtConfigurationException;
import org.example.Exception.JwtGenerationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Map;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    public String generateToken(final Usuario usuario) {
        try {
            return buildToken(usuario);
        } catch (Exception e) {
            log.error("Error al generar token JWT para usuario: {}", usuario.getEmail(), e);
            throw new JwtGenerationException("No se pudo generar el token de autenticación", e);
        }
    }

    private String buildToken(Usuario usuario) {
        try {
            return Jwts.builder()
                    .id(usuario.getId().toString())
                    .claims(Map.of(
                            "nombre", usuario.getNombre(),
                            "rol", usuario.getRol().getRolNombre().name()
                    ))
                    .subject(usuario.getEmail())
                    .issuedAt(new Date(System.currentTimeMillis()))
                    .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // 24 horas
                    .signWith(getSignInKey())
                    .compact();
        } catch (Exception e) {
            log.error("Error en buildToken para usuario: {}", usuario.getEmail(), e);
            throw new JwtGenerationException("Error interno al construir token", e);
        }
    }

    private SecretKey getSignInKey() {
        try {
            if (secretKey == null || secretKey.trim().isEmpty()) {
                throw new IllegalStateException("JWT secret key no configurado");
            }
            byte[] keyBytes = Decoders.BASE64.decode(secretKey);
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (IllegalArgumentException e) {
            log.error("Error al decodificar la clave secreta JWT", e);
            throw new JwtConfigurationException("Clave secreta JWT mal configurada", e);
        }
    }

    public String extractUsername(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (ExpiredJwtException e) {
            log.warn("Token expirado al extraer username: {}", e.getMessage());
            throw e; // Re-lanzar para manejo específico
        } catch (MalformedJwtException e) {
            log.warn("Token malformado al extraer username: {}", e.getMessage());
            throw new InvalidTokenException("Token JWT malformado", e);
        } catch (JwtException e) {
            log.warn("Error JWT al extraer username: {}", e.getMessage());
            throw new InvalidTokenException("Token JWT inválido", e);
        }
    }

    private Date extractExpiration(final String token) {
        try {
            return extractClaim(token, Claims::getExpiration);
        } catch (JwtException e) {
            log.warn("Error al extraer fecha de expiración del token", e);
            throw new InvalidTokenException("No se pudo verificar expiración del token", e);
        }
    }

    private boolean isTokenExpired(final String token) {
        try {
            Date expiration = extractExpiration(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            log.warn("Error al verificar expiración del token", e);
            return true; // Considerar expirado si hay error
        }
    }

    public boolean isTokenValid(final String token, final UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (ExpiredJwtException e) {
            log.debug("Token expirado para usuario: {}", userDetails.getUsername());
            return false;
        } catch (JwtException e) {
            log.debug("Token inválido para usuario: {}", userDetails.getUsername());
            return false;
        }
    }

    public boolean isTokenValid(final String token, final Usuario usuario) {
        try {
            final String userEmail = extractUsername(token);
            return (userEmail.equals(usuario.getEmail()) && !isTokenExpired(token));
        } catch (ExpiredJwtException e) {
            log.debug("Token expirado para usuario: {}", usuario.getEmail());
            return false;
        } catch (JwtException e) {
            log.debug("Token inválido para usuario: {}", usuario.getEmail());
            return false;
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        try {
            Claims claims = extractAllClaims(token);
            return claimsResolver.apply(claims);
        } catch (ExpiredJwtException e) {
            throw e; // Re-lanzar para manejo específico
        } catch (JwtException e) {
            log.warn("Error al extraer claim del token", e);
            throw new InvalidTokenException("No se pudo extraer información del token", e);
        }
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.debug("Token expirado al extraer claims");
            throw e;
        } catch (UnsupportedJwtException e) {
            log.warn("Token JWT no soportado: {}", e.getMessage());
            throw new InvalidTokenException("Formato de token no soportado", e);
        } catch (MalformedJwtException e) {
            log.warn("Token JWT malformado: {}", e.getMessage());
            throw new InvalidTokenException("Token JWT malformado", e);
        } catch (SignatureException e) {
            log.warn("Firma JWT inválida: {}", e.getMessage());
            throw new InvalidTokenException("Firma del token inválida", e);
        } catch (IllegalArgumentException e) {
            log.warn("Token JWT vacío o nulo: {}", e.getMessage());
            throw new InvalidTokenException("Token JWT vacío o nulo", e);
        }
    }
}
