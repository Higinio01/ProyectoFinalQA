package org.example.Service;

import org.example.Dtos.UsuarioDto;
import org.example.Entity.ApiToken;
import org.example.Entity.Rol;
import org.example.Entity.Usuario;
import org.example.Repository.ApiTokenRepository;
import org.example.Repository.RolRepository;
import org.example.Repository.UsuarioRepository;
import org.example.Request.UsuarioRequest;
import org.example.Security.jwt.JwtService;
import org.modelmapper.ModelMapper;
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

    public UsuarioService(ApiTokenRepository apiTokenRepository, JwtService jwtService, UsuarioRepository usuarioRepository, RolRepository rolRepository, PasswordEncoder passwordEncoder, ModelMapper modelMapper) {
        this.apiTokenRepository = apiTokenRepository;
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
    }

    public UsuarioDto usuarioPorId(Long id) {
        var usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id));
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
            throw new IllegalArgumentException("El correo electrónico ya está en uso.");
        }

        Rol rol = rolRepository.findById(usuarioRequest.idRol())
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado con ID: " + usuarioRequest.idRol()));

        var usuario = new Usuario();
        usuario.setNombre(usuarioRequest.nombre());
        usuario.setApellido(usuarioRequest.apellido());
        usuario.setEmail(usuarioRequest.email());
        usuario.setRol(rol);

        String contraseniaTemp = usuarioRequest.password();
        String contraseniaCodificada = passwordEncoder.encode(contraseniaTemp);
        usuario.setPassword(contraseniaCodificada);

        String jwt = jwtService.generateToken(usuario);
        ApiToken apiToken = new ApiToken(usuario, jwt);
        apiTokenRepository.save(apiToken);

        Usuario savedUsuario = usuarioRepository.save(usuario);
        return modelMapper.map(savedUsuario, UsuarioDto.class);
    }

    public UsuarioDto actualizarUsuario(Long id, UsuarioRequest usuarioRequest) {
        var usuarioExistente = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id));

        if (usuarioRepository.existsByEmailAndIdNot(usuarioRequest.email(), id)) {
            throw new IllegalArgumentException("El correo electrónico ya está en uso por otro usuario.");
        }

        Rol rol = rolRepository.findById(usuarioRequest.idRol())
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado con ID: " + usuarioRequest.idRol()));

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

    public void eliminarUsuario(Long id) {
        var usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id));
        usuarioRepository.delete(usuario);
    }
}
