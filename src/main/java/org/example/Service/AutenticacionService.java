package org.example.Service;

import org.example.Entity.ApiToken;
import org.example.Entity.EstadoUsuario;
import org.example.Exception.ResourceNotFoundException;
import org.example.Repository.ApiTokenRepository;
import org.example.Repository.UsuarioRepository;
import org.example.Request.LoginRequest;
import org.example.Security.jwt.JwtService;
import org.example.Security.jwt.TokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(AutenticacionService.class);

    public AutenticacionService(ApiTokenRepository apiTokenRepository,
                                AuthenticationManager authenticationManager,
                                UsuarioRepository usuarioRepository,
                                JwtService jwtService) {
        this.apiTokenRepository = apiTokenRepository;
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
    }

    public TokenResponse login(LoginRequest loginRequest) {
        try {
            // Validación de input básica
            if (loginRequest.email() == null || loginRequest.email().trim().isEmpty()) {
                log.warn("Intento de login con email vacío");
                throw new BadCredentialsException("Email requerido");
            }

            if (loginRequest.password() == null || loginRequest.password().trim().isEmpty()) {
                log.warn("Intento de login con password vacío");
                throw new BadCredentialsException("Password requerido");
            }

            String cleanEmail = loginRequest.email().trim();
            log.info("Intento de login para usuario: {}", cleanEmail);

            // Autenticación - AQUÍ ES DONDE SE LANZA BadCredentialsException
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            cleanEmail,
                            loginRequest.password()
                    )
            );

            log.info("Autenticación exitosa para usuario: {}", cleanEmail);

            // Buscar usuario en BD
            var usuario = usuarioRepository.findByEmail(cleanEmail)
                    .orElseThrow(() -> {
                        log.warn("Usuario autenticado pero no encontrado en BD: {}", cleanEmail);
                        return new ResourceNotFoundException("Usuario no encontrado");
                    });

            // Verificar estado del usuario
            if (!usuario.getEstado().equals(EstadoUsuario.ACTIVO)) {
                log.warn("Usuario inactivo intentó hacer login: {}", cleanEmail);
                throw new BadCredentialsException("Usuario inactivo o bloqueado");
            }

            // Generar o reutilizar token
            var tokenActivo = apiTokenRepository.findByUsuarioId(usuario.getId());

            String jwtToken;

            if (tokenActivo.isPresent()) {
                jwtToken = tokenActivo.get().getToken();
                log.info("Reutilizando token existente para usuario: {}", cleanEmail);
            } else {
                jwtToken = jwtService.generateToken(usuario);
                var nuevoToken = new ApiToken(usuario, jwtToken);
                apiTokenRepository.save(nuevoToken);
                log.info("Nuevo token generado para usuario: {}", cleanEmail);
            }

            log.info("Login exitoso para usuario: {}", cleanEmail);
            return new TokenResponse(jwtToken);

        } catch (BadCredentialsException e) {
            // Esta excepción se lanza cuando las credenciales son incorrectas
            log.warn("Credenciales inválidas para email: {}", loginRequest.email());
            throw e; // Re-lanzar para que GlobalExceptionHandler la maneje

        } catch (ResourceNotFoundException e) {
            // Usuario no encontrado
            log.warn("Usuario no encontrado: {}", loginRequest.email());
            throw new BadCredentialsException("Credenciales inválidas"); // No revelar que el usuario no existe

        } catch (Exception e) {
            // Cualquier otro error inesperado
            log.error("Error inesperado durante login para email: {}", loginRequest.email(), e);
            throw new RuntimeException("Error interno durante la autenticación", e);
        }
    }
}
