package org.example.Dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class UsuarioDto {
    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private String rolNombre;
    private String estado;

}
