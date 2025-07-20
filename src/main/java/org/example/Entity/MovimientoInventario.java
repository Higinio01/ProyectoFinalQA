package org.example.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "movimiento_inventario")
public class MovimientoInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "tipo_movimiento", nullable = false, length = 20)
    private String tipoMovimiento;

    @Column(name = "cantidad_anterior", nullable = false)
    private Integer cantidadAnterior;

    @Column(name = "cantidad_nueva", nullable = false)
    private Integer cantidadNueva;

    @Column(name = "fecha_movimiento", nullable = false)
    private LocalDateTime fechaMovimiento;

    @Column(name = "motivo", length = 500)
    private String motivo;

    @Column(name = "usuario_responsable", length = 100)
    private String usuarioResponsable;

    @PrePersist
    protected void onCreate() {
        if (fechaMovimiento == null) {
            fechaMovimiento = LocalDateTime.now();
        }
    }

    // Constructor útil para crear movimientos
    public MovimientoInventario(Producto producto, Integer cantidad, String tipoMovimiento,
                                Integer cantidadAnterior, Integer cantidadNueva, String motivo, String usuarioResponsable) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.tipoMovimiento = tipoMovimiento;
        this.cantidadAnterior = cantidadAnterior;
        this.cantidadNueva = cantidadNueva;
        this.motivo = motivo;
        this.usuarioResponsable = usuarioResponsable;
        this.fechaMovimiento = LocalDateTime.now();
    }
}