package org.example.Service;

import org.example.Entity.ApiToken;
import org.example.Entity.EstadoUsuario;
import org.example.Exception.ResourceNotFoundException;
import org.example.Repository.ApiTokenRepository;
import org.example.Repository.UsuarioRepository;
import org.example.Request.LoginRequest;
import org.example.Security.jwt.JwtService;
import org.example.Security.jwt.TokenResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AutenticacionService {
    private final ApiTokenRepository apiTokenRepository;
    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;

    public AutenticacionService(ApiTokenRepository apiTokenRepository, AuthenticationManager authenticationManager, UsuarioRepository usuarioRepository, JwtService jwtService) {
        this.apiTokenRepository = apiTokenRepository;
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
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (!usuario.getEstado().equals(EstadoUsuario.ACTIVO)) {
            throw new BadCredentialsException("Usuario inactivo o bloqueado");
        }

        var tokenActivo = apiTokenRepository.findByUsuarioId(usuario.getId());

        String jwtToken;

        if (tokenActivo.isPresent()) {
            jwtToken = tokenActivo.get().getToken();
        } else {
            jwtToken = jwtService.generateToken(usuario);
            var nuevoToken = new ApiToken(usuario, jwtToken);
            apiTokenRepository.save(nuevoToken);
        }

        return new TokenResponse(jwtToken);
    }
}
