package org.example.Controller;

import org.example.Repository.UsuarioRepository;
import org.example.Request.LoginRequest;
import org.example.Security.jwt.TokenResponse;
import org.example.Service.AutenticacionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AutenticacionController {

    private final AutenticacionService autenticacionService;
    private final UsuarioRepository usuarioRepository;
    private static final Logger log = LoggerFactory.getLogger(AutenticacionController.class);

    public AutenticacionController(AutenticacionService autenticacionService, UsuarioRepository usuarioRepository) {
        this.autenticacionService = autenticacionService;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody final LoginRequest loginRequest) {
        try {
            log.debug("Recibiendo request de login para: {}", loginRequest.email());

            final TokenResponse token = autenticacionService.login(loginRequest);

            log.info("Login exitoso para usuario: {}", loginRequest.email());
            System.out.println("A ELLA LE GUSTA MI MOTORAAA Login exitoso para usuario: " + loginRequest.email());
            return ResponseEntity.ok(token);

        } catch (BadCredentialsException e) {
            log.warn("Intento de login fallido para: {}", loginRequest.email());
            throw e;

        } catch (Exception e) {
            log.error("Error inesperado en login endpoint para: {}", loginRequest.email(), e);
            throw e;
        }
    }

    @GetMapping("/me")
    public ResponseEntity<Long> getCurrentUserId(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("Request a /me sin autenticación válida");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String email = authentication.getName();
            log.debug("Request a /me para usuario: {}", email);

            return usuarioRepository.findByEmail(email)
                    .map(usuario -> {
                        log.debug("Usuario encontrado para /me: {}", email);
                        return ResponseEntity.ok(usuario.getId());
                    })
                    .orElseGet(() -> {
                        log.warn("Usuario autenticado pero no encontrado en BD: {}", email);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                    });

        } catch (Exception e) {
            log.error("Error inesperado en /me endpoint", e);
            throw e;
        }
    }
}