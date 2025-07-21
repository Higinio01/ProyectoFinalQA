package org.example.Service;

import org.example.Entity.ApiToken;
import org.example.Entity.EstadoUsuario;
import org.example.Entity.Usuario;
import org.example.Exception.ResourceNotFoundException;
import org.example.Repository.ApiTokenRepository;
import org.example.Repository.UsuarioRepository;
import org.example.Request.LoginRequest;
import org.example.Security.jwt.TokenResponse;
import org.example.Security.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AutenticacionServiceTest {

    private AutenticacionService autenticacionService;
    private ApiTokenRepository apiTokenRepository;
    private AuthenticationManager authenticationManager;
    private UsuarioRepository usuarioRepository;
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        apiTokenRepository = mock(ApiTokenRepository.class);
        authenticationManager = mock(AuthenticationManager.class);
        usuarioRepository = mock(UsuarioRepository.class);
        jwtService = mock(JwtService.class);

        autenticacionService = new AutenticacionService(
                apiTokenRepository, authenticationManager, usuarioRepository, jwtService
        );
    }

    @Test
    void login_usuarioActivo_sinTokenPrevio_generarYGuardarToken() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("juan@example.com");
        usuario.setEstado(EstadoUsuario.ACTIVO);

        when(usuarioRepository.findByEmail("juan@example.com")).thenReturn(Optional.of(usuario));
        when(apiTokenRepository.findByUsuarioId(1L)).thenReturn(Optional.empty());
        when(jwtService.generateToken(usuario)).thenReturn("nuevo-token");

        LoginRequest request = new LoginRequest("juan@example.com", "1234");

        TokenResponse response = autenticacionService.login(request);

        assertEquals("nuevo-token", response.getJwtToken());
        verify(apiTokenRepository).save(any(ApiToken.class));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_usuarioActivo_conTokenPrevio_retornaTokenExistente() {
        Usuario usuario = new Usuario();
        usuario.setId(2L);
        usuario.setEmail("maria@example.com");
        usuario.setEstado(EstadoUsuario.ACTIVO);

        ApiToken token = new ApiToken(usuario, "token-existente");

        when(usuarioRepository.findByEmail("maria@example.com")).thenReturn(Optional.of(usuario));
        when(apiTokenRepository.findByUsuarioId(2L)).thenReturn(Optional.of(token));

        LoginRequest request = new LoginRequest("maria@example.com", "abcd");

        TokenResponse response = autenticacionService.login(request);

        assertEquals("token-existente", response.getJwtToken());
        verify(apiTokenRepository, never()).save(any());
    }

    @Test
    void login_usuarioNoExiste_lanzaExcepcion() {
        when(usuarioRepository.findByEmail("no@existe.com")).thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest("no@existe.com", "1234");

        assertThrows(ResourceNotFoundException.class, () -> autenticacionService.login(request));
    }

    @Test
    void login_usuarioInactivo_lanzaBadCredentialsException() {
        Usuario usuario = new Usuario();
        usuario.setEmail("inactivo@example.com");
        usuario.setEstado(EstadoUsuario.BLOQUEADO); // o INACTIVO

        when(usuarioRepository.findByEmail("inactivo@example.com")).thenReturn(Optional.of(usuario));

        LoginRequest request = new LoginRequest("inactivo@example.com", "pass");

        assertThrows(BadCredentialsException.class, () -> autenticacionService.login(request));
    }

    @Test
    void login_autenticacionFalla_lanzaExcepcion() {
        LoginRequest request = new LoginRequest("fallo@example.com", "wrongpass");

        doThrow(new BadCredentialsException("Credenciales inválidas"))
                .when(authenticationManager)
                .authenticate(any());

        assertThrows(BadCredentialsException.class, () -> autenticacionService.login(request));
    }
}
