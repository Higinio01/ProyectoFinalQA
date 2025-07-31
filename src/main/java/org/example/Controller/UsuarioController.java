package org.example.Controller;

import org.example.Dtos.UsuarioDto;
import org.example.Entity.EstadoUsuario;
import org.example.Entity.Usuario;
import org.example.Request.UsuarioRequest;
import org.example.Service.UsuarioService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    private final UsuarioService usuarioService;
    private final ModelMapper modelMapper;

    @Autowired
    public UsuarioController(UsuarioService usuarioService, ModelMapper modelMapper) {
        this.usuarioService = usuarioService;
        this.modelMapper = modelMapper;
    }

//    @GetMapping
//    public ResponseEntity<Page<UsuarioDto>> obtenerTodos(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size
//    ) {
//        Page<Usuario> usuariosPage = usuarioService.obtenerUsuariosPaginados(page, size);
//        return ResponseEntity.ok(usuariosPage.map(usuario -> modelMapper.map(usuario, UsuarioDto.class)));
//    }

    @GetMapping
    public ResponseEntity<Page<UsuarioDto>> obtenerTodos(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<Usuario> usuariosPage = usuarioService.obtenerUsuariosPaginados(pageable);
        return ResponseEntity.ok(usuariosPage.map(usuario -> modelMapper.map(usuario, UsuarioDto.class)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDto> obtenerPorId(@PathVariable Long id) {
        Usuario usuario = usuarioService.usuarioPorId(id);
        return ResponseEntity.ok(modelMapper.map(usuario, UsuarioDto.class));
    }

    @PostMapping
    public ResponseEntity<UsuarioDto> crearUsuario(@RequestBody UsuarioRequest usuarioRequest) {
        Usuario savedUsuario = usuarioService.crearUsuario(usuarioRequest);
        return ResponseEntity.ok(modelMapper.map(savedUsuario, UsuarioDto.class));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioDto> actualizarUsuario(@PathVariable Long id, @RequestBody UsuarioRequest usuarioRequest) {
        Usuario usuario = usuarioService.actualizarUsuario(id, usuarioRequest);
        UsuarioDto usuarioDto = modelMapper.map(usuario, UsuarioDto.class);
        return ResponseEntity.ok(usuarioDto);
    }

    @PutMapping("/{id}/estado/{nuevoEstado}")
    public ResponseEntity<String> actualizarEstadoUsuario(
            @PathVariable Long id,
            @PathVariable String nuevoEstado) {

        try {
            EstadoUsuario estado = EstadoUsuario.valueOf(nuevoEstado.toUpperCase());
            String mensaje = usuarioService.cambiarEstado(id, estado);
            return ResponseEntity.ok(mensaje);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Estado inválido. Usa: ACTIVO, INACTIVO o BLOQUEADO.");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }
}
