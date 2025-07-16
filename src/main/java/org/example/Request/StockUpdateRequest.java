package org.example.Request;

public record StockUpdateRequest(
        Integer cantidad,
        String tipoMovimiento,
        String motivo
) {
    public StockUpdateRequest {
        if (cantidad == null || cantidad == 0) {
            throw new IllegalArgumentException("La cantidad no puede ser null o cero");
        }
        if (tipoMovimiento == null || tipoMovimiento.trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de movimiento es requerido");
        }
        if (!tipoMovimiento.equalsIgnoreCase("ENTRADA") && !tipoMovimiento.equalsIgnoreCase("SALIDA")) {
            throw new IllegalArgumentException("El tipo de movimiento debe ser 'ENTRADA' o 'SALIDA'");
        }
    }
}