package org.example.Service;

import org.example.Exception.ResourceNotFoundException;
import org.example.Entity.Usuario;
import org.example.Repository.UsuarioRepository;
import org.example.Request.LoginRequest;
import org.example.Security.jwt.JwtService;
import org.example.Security.jwt.TokenResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AutenticacionService {
    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;

    public AutenticacionService(AuthenticationManager authenticationManager, UsuarioRepository usuarioRepository, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
    }

    public TokenResponse login(LoginRequest loginRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.email(),
                        loginRequest.password()
                )
        );
        var usuario = usuarioRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));;
        var jwtToken = jwtService.generateToken(usuario);

        System.out.println("Refresh Token generado: " + jwtToken);
        var refreshToken = jwtService.generateRefreshToken(usuario);

        System.out.println("Refresh Token generado: " + refreshToken);

        return new TokenResponse(jwtToken, refreshToken);
    }

    public TokenResponse refreshToken(final String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("El token de refresco no es válido");
        }

        final String refreshToken = authHeader.substring("Bearer ".length());
        final String userEmail = jwtService.extractUsername(refreshToken);

        if (userEmail == null) {
            throw new IllegalArgumentException("El token de refresco no es válido");
        }

        final Usuario usuario = usuarioRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if(!jwtService.isTokenValid(refreshToken, usuario)) {
            throw new IllegalArgumentException("El token de refresco no es válido");
        }

        final String accessToken = jwtService.generateToken(usuario);
        return new TokenResponse(accessToken, refreshToken);
    }


}
