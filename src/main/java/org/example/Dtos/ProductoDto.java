package org.example.Dtos;

import lombok.Getter;
import lombok.Setter;
import org.example.Entity.Categoria;

@Getter
@Setter
public class ProductoDto {
    private Long id;
    private String nombre;
    private String descripcion;
    private Categoria categoria;
    private float precio;
    private int cantidad;

    public ProductoDto() {
    }

    public ProductoDto(Long id, String nombre, String descripcion, Categoria categoria, float precio, int cantidad) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.precio = precio;
        this.cantidad = cantidad;
    }
}
