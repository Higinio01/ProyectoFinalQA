package org.example.Service;


import org.example.Repository.MovimientoInventarioRepository;
import org.springframework.stereotype.Service;

@Service
public class InventarioService {

    private final MovimientoInventarioRepository movimientoStockRepository;

    public InventarioService(MovimientoInventarioRepository movimientoStockRepository) {
        this.movimientoStockRepository = movimientoStockRepository;
    }

    public void actualizarStock(Long productoId, Integer cantidad, String tipoMovimiento, String descripcion) {
        // TODO: Lógica para actualizar el stock del producto y guardar el movimiento.
    }

    public void obtenerHistorialDeMovimientos() {
        // TODO: Lógica para devolver el historial de movimientos de stock.
    }
}
