package org.example.Service;

import org.example.Dtos.UsuarioDto;
import org.example.Entity.Usuario;
import org.example.Repository.UsuarioRepository;
import org.example.Request.UsuarioRequest;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final ModelMapper modelMapper;

    public UsuarioService(UsuarioRepository usuarioRepository, ModelMapper modelMapper) {
        this.usuarioRepository = usuarioRepository;
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
        var usuario = new Usuario();
        usuario.setNombre(usuarioRequest.nombre());
        usuario.setApellido(usuarioRequest.apellido());
        usuario.setEmail(usuarioRequest.email());
        usuario.setPassword(usuarioRequest.password());
        usuario.setRol(usuarioRequest.rol());

        Usuario savedUsuario = usuarioRepository.save(usuario);
        return modelMapper.map(savedUsuario, UsuarioDto.class);
    }

    public UsuarioDto actualizarUsuario(Long id, UsuarioRequest usuarioRequest) {
        var usuarioExistente = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id));

        usuarioExistente.setNombre(usuarioRequest.nombre());
        usuarioExistente.setApellido(usuarioRequest.apellido());
        usuarioExistente.setEmail(usuarioRequest.email());
        usuarioExistente.setPassword(usuarioRequest.password());
        usuarioExistente.setRol(usuarioRequest.rol());

        Usuario usuarioActualizado = usuarioRepository.save(usuarioExistente);
        return modelMapper.map(usuarioActualizado, UsuarioDto.class);
    }

    public void eliminarUsuario(Long id) {
        var usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id));
        usuarioRepository.delete(usuario);
    }
}
