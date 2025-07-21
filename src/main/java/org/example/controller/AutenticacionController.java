package org.example.controller;

import org.example.Repository.UsuarioRepository;
import org.example.Request.LoginRequest;
import org.example.Security.jwt.TokenResponse;
import org.example.Service.AutenticacionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
public class AutenticacionController {

    private final AutenticacionService autenticacionService;
    private final UsuarioRepository usuarioRepository;

    public AutenticacionController(AutenticacionService autenticacionService, UsuarioRepository usuarioRepository) {
        this.autenticacionService = autenticacionService;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> Login(@RequestBody final LoginRequest loginRequest) {
        final TokenResponse token = autenticacionService.login(loginRequest);
        return ResponseEntity.ok(token);
    }

    @GetMapping("/me")
    public ResponseEntity<Long> getCurrentUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = authentication.getName();

        return usuarioRepository.findByEmail(email)
                .map(usuario -> ResponseEntity.ok(usuario.getId()))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

}
