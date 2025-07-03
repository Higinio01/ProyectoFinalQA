package org.example.Security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.example.Entity.ApiToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.NonNull;
import org.example.Entity.EstadoUsuario;
import org.example.Repository.ApiTokenRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Collection;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final ApiTokenRepository apiTokenRepository;
    private final TokenValidationCache tokenValidationCache;
    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    // Eliminar UsuarioRepository ya que no se está usando
    public JwtAuthFilter(JwtService jwtService, UserDetailsService userDetailsService, ApiTokenRepository apiTokenRepository, TokenValidationCache tokenValidationCache) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.apiTokenRepository = apiTokenRepository;
        this.tokenValidationCache = tokenValidationCache;
    }


    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {


        String path = request.getServletPath();
        if (path.equals("/api/auth/login")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);

        try {
            // 🔍 LOG: Token recibido (completo para debug)
            log.info("🎯 TOKEN RECIBIDO - Longitud: {}, Primeros 20 chars: '{}'",
                    jwt.length(), jwt.substring(0, Math.min(jwt.length(), 20)));

            // 🔍 LOG: Estado del cache antes de consultar
            log.info("📊 Cache stats ANTES - Tamaño: {}", tokenValidationCache.size());

            Boolean cachedValidity = tokenValidationCache.get(jwt);

            // 🔍 LOG: Resultado de la consulta al cache
            log.info("🔎 CONSULTA CACHE - Resultado: {}", cachedValidity);

            if (cachedValidity != null) {
                log.info("🟢 CACHE HIT - Valor encontrado: {}", cachedValidity);
                if (!cachedValidity) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token revocado o inválido (cache).");
                    return;
                }
            } else {
                log.info("🔴 CACHE MISS - Consultando BD para token");

                // 🔍 LOG: Antes de consultar BD
                log.info("🗃️ CONSULTANDO BD...");

                boolean isValid = apiTokenRepository.findByToken(jwt)
                        .map(ApiToken::getUsuario)
                        .filter(usuario -> usuario.getEstado() == EstadoUsuario.ACTIVO)
                        .isPresent();

                // 🔍 LOG: Resultado de BD
                log.info("🗃️ RESULTADO BD - Token válido: {}", isValid);

                // 🔍 LOG: Intentando guardar en cache
                log.info("💾 INTENTANDO GUARDAR EN CACHE - Token válido: {}", isValid);

                tokenValidationCache.put(jwt, isValid);

                // 🔍 LOG: Verificar inmediatamente después de guardar
                log.info("✅ VERIFICACIÓN INMEDIATA - Cache tamaño: {}", tokenValidationCache.size());

                // 🔍 LOG: Verificar si realmente se guardó
                Boolean verificacion = tokenValidationCache.get(jwt);
                log.info("🔍 VERIFICACIÓN LECTURA - Valor en cache: {}", verificacion);

                if (!isValid) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token revocado o inválido.");
                    return;
                }
            }



            final String userEmail = jwtService.extractUsername(jwt);
            final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (userEmail != null && authentication == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                String role = jwtService.extractClaim(jwt, claims -> claims.get("rol", String.class));
                Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (ExpiredJwtException e) {
            log.warn("Token expirado: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expirado.");
            return;
        } catch (JwtException e) {
            log.warn("Token inválido: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido.");
            return;
        } catch (BadCredentialsException e) {
            log.warn("Credenciales inválidas: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Credenciales inválidas.");
            return;
        } catch (AccessDeniedException e) {
            log.warn("Acceso denegado: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "No tiene permisos para acceder a este recurso.");
            return;
        } catch (Exception e) {
            log.error("Error interno en autenticación JWT", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error interno en la autenticación.");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
