package org.example.Request;

import org.example.Entity.Rol;
import org.example.Entity.RolNombre;

import java.io.Serializable;

public record UsuarioRequest(String nombre, String apellido, String email, String password, Long idRol) implements Serializable {
}