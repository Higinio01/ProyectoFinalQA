package org.example.Security;

import org.example.Security.jwt.JwtAuthFilter;
import org.example.Security.jwt.TokenValidationCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final TokenValidationCache tokenValidationCache;
    private final AuthenticationProvider authenticationProvider;
    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);


    public SecurityConfig(JwtAuthFilter jwtAuthFilter, TokenValidationCache tokenValidationCache, AuthenticationProvider authenticationProvider) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.tokenValidationCache = tokenValidationCache;
        this.authenticationProvider = authenticationProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/debug/cache").permitAll()
                        .requestMatchers("/api/auth/me").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/productos/**").hasAnyAuthority("ADMIN", "EMPLEADO", "CLIENTE")
                        .requestMatchers("/api/productos").hasAnyAuthority("ADMIN", "EMPLEADO")
                        .requestMatchers(HttpMethod.DELETE, "/api/productos/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/usuarios").hasAnyAuthority("ADMIN", "EMPLEADO")
                        .requestMatchers("/api/usuarios/**").hasAnyAuthority("ADMIN", "EMPLEADO")
                        .requestMatchers(HttpMethod.POST, "/api/usuarios").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/usuarios/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/usuarios/*/estado/*").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/usuarios/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/inventario").hasAnyAuthority("ADMIN", "EMPLEADO")
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .addLogoutHandler((request, response, authentication) -> {
                            final var authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
                            invalidateTokenInCache(authHeader);
                        })
                        .logoutSuccessHandler((request, response, authentication) -> {
                            SecurityContextHolder.clearContext();
                        })
                );

        return http.build();
    }

    private void logout(final String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new BadCredentialsException("Invalid token");
        }
        String jwt = token.substring(7);
        tokenValidationCache.invalidate(jwt);
        log.info("🧹 Token JWT removido de la cache al hacer logout: {}", jwt.substring(0, Math.min(jwt.length(), 20)));
    }

    private void invalidateTokenInCache(final String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            log.warn("Intento de logout sin token o token inválido.");
            return;
        }
        String jwt = token.substring(7);
        boolean existed = tokenValidationCache.invalidateAndCheck(jwt);
        if (existed) {
            log.info("🧹 JWT removido de cache en logout: {}", jwt.substring(0, Math.min(jwt.length(), 20)));
        } else {
            log.info("⚠️ JWT solicitado para logout no existía en cache (se intentó borrar igual): {}", jwt.substring(0, Math.min(jwt.length(), 20)));
        }
    }
}