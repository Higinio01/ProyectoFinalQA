package org.example.controller;

import org.example.Dtos.UsuarioDto;
import org.example.Entity.EstadoUsuario;
import org.example.Request.UsuarioRequest;
import org.example.Service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    private final UsuarioService usuarioService;

    @Autowired
    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // Obtener todos los usuarios
    @GetMapping
    public ResponseEntity<Page<UsuarioDto>> obtenerTodos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(usuarioService.obtenerUsuariosPaginados(page, size));
    }


    // Obtener un usuario por ID
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDto> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.usuarioPorId(id));
    }

    // Crear un nuevo usuario
    @PostMapping
    public ResponseEntity<UsuarioDto> crearUsuario(@RequestBody UsuarioRequest usuarioRequest) {
        return ResponseEntity.ok(usuarioService.crearUsuario(usuarioRequest));
    }

    // Actualizar un usuario existente
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioDto> actualizarUsuario(@PathVariable Long id, @RequestBody UsuarioRequest usuarioRequest) {
        return ResponseEntity.ok(usuarioService.actualizarUsuario(id, usuarioRequest));
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

    // Eliminar un usuario
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }
}
