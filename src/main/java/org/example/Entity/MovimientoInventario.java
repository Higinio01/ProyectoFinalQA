package org.example.Entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

public class MovimientoInventario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    private Integer cantidad; // Positivo: entrada, negativo: salida
    private String tipoMovimiento; // "ENTRADA" o "SALIDA"
    private LocalDateTime fechaMovimiento;
}
