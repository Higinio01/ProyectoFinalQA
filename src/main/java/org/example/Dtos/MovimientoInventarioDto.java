package org.example.Dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.example.Entity.Producto;

import java.time.LocalDateTime;

@Getter
@Setter
public class MovimientoInventarioDto {

    private ProductoDto producto;
    private Integer cantidad;
    private String tipoMovimiento;
    private Integer cantidadAnterior;
    private Integer cantidadNueva;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaMovimiento;

    private String motivo;
    private String usuarioResponsable;
}