package org.example.Entidades;

import jakarta.persistence.*;
@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private String descripcion;

    @Column(nullable = false)
    private String categoria;

    @Column(nullable = false)
    private float precio;

    @Column(name = "stock", nullable = false)
    private Integer cantidad;
}