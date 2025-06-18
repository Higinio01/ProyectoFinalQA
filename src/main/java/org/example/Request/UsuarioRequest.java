package org.example.Request;

import org.example.Entity.Rol;

public record UsuarioRequest(String nombre, String apellido, String email, String password, Rol rol) {
}
