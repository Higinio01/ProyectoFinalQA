package org.example.Service;

import jakarta.transaction.Transactional;
import org.example.Dtos.UsuarioDto;
import org.example.Entity.ApiToken;
import org.example.Entity.EstadoUsuario;
import org.example.Entity.Rol;
import org.example.Entity.Usuario;
import org.example.Exception.UsuarioException;
import org.example.Repository.ApiTokenRepository;
import org.example.Repository.RolRepository;
import org.example.Repository.UsuarioRepository;
import org.example.Request.UsuarioRequest;
import org.example.Security.jwt.JwtService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {
    private final ApiTokenRepository apiTokenRepository;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    public UsuarioService(ApiTokenRepository apiTokenRepository, JwtService jwtService,
                          UsuarioRepository usuarioRepository, RolRepository rolRepository,
                          PasswordEncoder passwordEncoder, ModelMapper modelMapper) {
        this.apiTokenRepository = apiTokenRepository;
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
    }

    public UsuarioDto usuarioPorId(Long id) {
        var usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioException.NoEncontrado("Usuario no encontrado con id: " + id));
        return modelMapper.map(usuario, UsuarioDto.class);
    }

    public List<UsuarioDto> obtenerTodosLosUsuarios() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        return usuarios.stream()
                .map(usuario -> modelMapper.map(usuario, UsuarioDto.class))
                .toList();
    }

    public UsuarioDto crearUsuario(UsuarioRequest usuarioRequest) {
        if (usuarioRepository.existsByEmail(usuarioRequest.email())) {
            throw new UsuarioException.EmailDuplicado("El correo electrónico ya está en uso: " + usuarioRequest.email());
        }

        Rol rol = rolRepository.findById(usuarioRequest.idRol())
                .orElseThrow(() -> new UsuarioException.RolNoEncontrado("Rol no encontrado con ID: " + usuarioRequest.idRol()));

        Usuario usuario = new Usuario();
        usuario.setNombre(usuarioRequest.nombre());
        usuario.setApellido(usuarioRequest.apellido());
        usuario.setEmail(usuarioRequest.email());
        usuario.setRol(rol);

        String contraseniaCodificada = passwordEncoder.encode(usuarioRequest.password());
        usuario.setPassword(contraseniaCodificada);
        usuario.setEstado(EstadoUsuario.ACTIVO);

        Usuario savedUsuario = usuarioRepository.save(usuario);

        String jwt = jwtService.generateToken(savedUsuario);
        ApiToken apiToken = new ApiToken(savedUsuario, jwt);
        apiTokenRepository.save(apiToken);

        return modelMapper.map(savedUsuario, UsuarioDto.class);
    }

    public UsuarioDto actualizarUsuario(Long id, UsuarioRequest usuarioRequest) {
        Usuario usuarioExistente = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioException.NoEncontrado("Usuario no encontrado con id: " + id));

        if (usuarioRepository.existsByEmailAndIdNot(usuarioRequest.email(), id)) {
            throw new UsuarioException.EmailDuplicado("El correo electrónico ya está en uso por otro usuario: " + usuarioRequest.email());
        }

        Rol rol = rolRepository.findById(usuarioRequest.idRol())
                .orElseThrow(() -> new UsuarioException.RolNoEncontrado("Rol no encontrado con ID: " + usuarioRequest.idRol()));

        usuarioExistente.setNombre(usuarioRequest.nombre());
        usuarioExistente.setApellido(usuarioRequest.apellido());
        usuarioExistente.setEmail(usuarioRequest.email());
        usuarioExistente.setRol(rol);

        if (usuarioRequest.password() != null && !usuarioRequest.password().trim().isEmpty()) {
            usuarioExistente.setPassword(passwordEncoder.encode(usuarioRequest.password()));
        }

        Usuario usuarioActualizado = usuarioRepository.save(usuarioExistente);
        return modelMapper.map(usuarioActualizado, UsuarioDto.class);
    }

    @Transactional
    public String cambiarEstado(Long id, EstadoUsuario nuevoEstado) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioException.NoEncontrado("Usuario no encontrado con id: " + id));

        usuario.setEstado(nuevoEstado);
        usuarioRepository.save(usuario);

        return "Estado del usuario actualizado a " + nuevoEstado;
    }

    public void eliminarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioException.NoEncontrado("Usuario no encontrado con id: " + id));
        usuarioRepository.delete(usuario);
    }

    public Page<UsuarioDto> obtenerUsuariosPaginados(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<Usuario> usuariosPage = usuarioRepository.findAll(pageable);
        return usuariosPage.map(usuario -> modelMapper.map(usuario, UsuarioDto.class));
    }

}
