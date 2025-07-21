package org.example.Service;

import org.example.Entity.*;
import org.example.Exception.UsuarioException;
import org.example.Repository.ApiTokenRepository;
import org.example.Repository.RolRepository;
import org.example.Repository.UsuarioRepository;
import org.example.Request.UsuarioRequest;
import org.example.Security.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UsuarioServiceTest {
    private UsuarioService usuarioService;
    private ApiTokenRepository apiTokenRepository;
    private JwtService jwtService;
    private UsuarioRepository usuarioRepository;
    private RolRepository rolRepository;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        apiTokenRepository = mock(ApiTokenRepository.class);
        jwtService = mock(JwtService.class);
        usuarioRepository = mock(UsuarioRepository.class);
        rolRepository = mock(RolRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);

        usuarioService = new UsuarioService(apiTokenRepository, jwtService, usuarioRepository, rolRepository, passwordEncoder);
    }

    @Test
    void crearUsuario_exitoso() {
        UsuarioRequest request = new UsuarioRequest("Juan", "Pérez", "juan@example.com", "123456", 1L);
        Rol rol = new Rol();
        rol.setId(1L);

        when(usuarioRepository.existsByEmail("juan@example.com")).thenReturn(false);
        when(rolRepository.findById(1L)).thenReturn(Optional.of(rol));
        when(passwordEncoder.encode("123456")).thenReturn("hashedPassword");

        Usuario usuarioGuardado = new Usuario();
        usuarioGuardado.setId(1L);
        usuarioGuardado.setEmail("juan@example.com");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioGuardado);
        when(jwtService.generateToken(any(Usuario.class))).thenReturn("fake-jwt");

        Usuario result = usuarioService.crearUsuario(request);

        assertNotNull(result);
        assertEquals("juan@example.com", result.getEmail());
        verify(apiTokenRepository).save(any(ApiToken.class));
    }

    @Test
    void crearUsuario_emailDuplicado_lanzaExcepcion() {
        UsuarioRequest request = new UsuarioRequest("Juan", "Pérez", "juan@example.com", "123456", 1L);

        when(usuarioRepository.existsByEmail("juan@example.com")).thenReturn(true);

        UsuarioException.EmailDuplicado ex = assertThrows(UsuarioException.EmailDuplicado.class,
                () -> usuarioService.crearUsuario(request));

        assertEquals("El correo electrónico ya está en uso: juan@example.com", ex.getMessage());
    }

    @Test
    void usuarioPorId_existente_devuelveUsuario() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        Usuario result = usuarioService.usuarioPorId(1L);
        assertEquals(1L, result.getId());
    }

    @Test
    void usuarioPorId_noExistente_lanzaExcepcion() {
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        UsuarioException.NoEncontrado ex = assertThrows(UsuarioException.NoEncontrado.class,
                () -> usuarioService.usuarioPorId(999L));

        assertTrue(ex.getMessage().contains("Usuario no encontrado con id:"));
    }

    @Test
    void cambiarEstado_actualizaEstadoCorrectamente() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEstado(EstadoUsuario.INACTIVO);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        String result = usuarioService.cambiarEstado(1L, EstadoUsuario.ACTIVO);

        assertEquals(EstadoUsuario.ACTIVO, usuario.getEstado());
        assertTrue(result.contains("Estado del usuario actualizado"));
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void actualizarUsuario_exitoso_actualizaDatosCorrectamente() {
        Long userId = 1L;
        Rol rol = new Rol();
        rol.setId(2L);

        Usuario usuarioExistente = new Usuario();
        usuarioExistente.setId(userId);
        usuarioExistente.setEmail("old@example.com");

        UsuarioRequest request = new UsuarioRequest("NuevoNombre", "NuevoApellido", "nuevo@example.com", "nuevaPass", 2L);

        when(usuarioRepository.findById(userId)).thenReturn(Optional.of(usuarioExistente));
        when(usuarioRepository.existsByEmailAndIdNot("nuevo@example.com", userId)).thenReturn(false);
        when(rolRepository.findById(2L)).thenReturn(Optional.of(rol));
        when(passwordEncoder.encode("nuevaPass")).thenReturn("hashedNuevaPass");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        Usuario actualizado = usuarioService.actualizarUsuario(userId, request);

        assertEquals("nuevo@example.com", actualizado.getEmail());
        assertEquals("NuevoNombre", actualizado.getNombre());
        assertEquals("NuevoApellido", actualizado.getApellido());
        assertEquals(rol, actualizado.getRol());
        assertEquals("hashedNuevaPass", actualizado.getPassword());
    }

    @Test
    void actualizarUsuario_emailDuplicado_lanzaExcepcion() {
        Long userId = 1L;

        Usuario usuarioExistente = new Usuario();
        usuarioExistente.setId(userId);

        UsuarioRequest request = new UsuarioRequest("Nombre", "Apellido", "duplicado@example.com", null, 2L);

        when(usuarioRepository.findById(userId)).thenReturn(Optional.of(usuarioExistente));
        when(usuarioRepository.existsByEmailAndIdNot("duplicado@example.com", userId)).thenReturn(true);

        UsuarioException.EmailDuplicado ex = assertThrows(UsuarioException.EmailDuplicado.class,
                () -> usuarioService.actualizarUsuario(userId, request));

        assertEquals("El correo electrónico ya está en uso por otro usuario: duplicado@example.com", ex.getMessage());
    }

    @Test
    void eliminarUsuario_existente_eliminaCorrectamente() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        usuarioService.eliminarUsuario(1L);

        verify(usuarioRepository).delete(usuario);
    }

    @Test
    void eliminarUsuario_noExistente_lanzaExcepcion() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        UsuarioException.NoEncontrado ex = assertThrows(UsuarioException.NoEncontrado.class,
                () -> usuarioService.eliminarUsuario(99L));

        assertTrue(ex.getMessage().contains("Usuario no encontrado con id:"));
    }

    @Test
    void obtenerTodosLosUsuarios_devuelveLista() {
        when(usuarioRepository.findAll()).thenReturn(List.of(new Usuario(), new Usuario()));

        List<Usuario> usuarios = usuarioService.obtenerTodosLosUsuarios();

        assertEquals(2, usuarios.size());
    }

    @Test
    void obtenerUsuariosPaginados_devuelvePagina() {
        @SuppressWarnings("unchecked")
        Page<Usuario> paginaMock = (Page<Usuario>) mock(Page.class);

        when(usuarioRepository.findAll(any(Pageable.class))).thenReturn(paginaMock);

        Page<Usuario> resultado = usuarioService.obtenerUsuariosPaginados(0, 10);

        assertEquals(paginaMock, resultado);

        verify(usuarioRepository).findAll(any(Pageable.class));
    }
}

