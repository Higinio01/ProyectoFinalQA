package org.example.Service;

import org.example.Entity.MovimientoInventario;
import org.example.Entity.Producto;
import org.example.Exception.ProductoException;
import org.example.Repository.MovimientoInventarioRepository;
import org.example.Repository.ProductoRepository;
import org.example.Request.StockUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InventarioService {

    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final ProductoRepository productoRepository;

    public InventarioService(MovimientoInventarioRepository movimientoInventarioRepository,
                             ProductoRepository productoRepository) {
        this.movimientoInventarioRepository = movimientoInventarioRepository;
        this.productoRepository = productoRepository;
    }

    /**
     * Actualiza el stock del producto y registra el movimiento
     */
    @Transactional
    public Producto actualizarStock(Long productoId, StockUpdateRequest request) {

        // Buscar el producto
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ProductoException.NoEncontrado("Producto no encontrado con id: " + productoId));

        int cantidadAnterior = producto.getCantidad();
        int nuevaCantidad;

        // Calcular nueva cantidad
        if (request.tipoMovimiento().equalsIgnoreCase("ENTRADA")) {
            nuevaCantidad = cantidadAnterior + request.cantidad();
        } else {
            // Validar stock suficiente para salida
            if (cantidadAnterior < request.cantidad()) {
                throw new ProductoException.ValorInvalido(
                        String.format("Stock insuficiente. Stock actual: %d, cantidad solicitada: %d",
                                cantidadAnterior, request.cantidad())
                );
            }
            nuevaCantidad = cantidadAnterior - request.cantidad();
        }

        // Actualizar el producto
        producto.setCantidad(nuevaCantidad);
        Producto productoActualizado = productoRepository.save(producto);

        // Registrar el movimiento
        MovimientoInventario movimiento = new MovimientoInventario(
                producto,
                request.cantidad(),
                request.tipoMovimiento().toUpperCase(),
                cantidadAnterior,
                nuevaCantidad,
                request.motivo(),
                request.usuarioResponsable() != null ? request.usuarioResponsable() : "Sistema"
        );

        movimientoInventarioRepository.save(movimiento);

        return productoActualizado;
    }

    /**
     * Obtiene el historial completo de movimientos con paginación
     */
    public Page<MovimientoInventario> obtenerHistorialDeMovimientos(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return movimientoInventarioRepository.findAllByOrderByFechaMovimientoDesc(pageable);
    }

    /**
     * Obtiene el historial de movimientos de un producto específico
     */
    public List<MovimientoInventario> obtenerHistorialPorProducto(Long productoId) {
        return movimientoInventarioRepository.findByProductoIdOrderByFechaMovimientoDesc(productoId);
    }

    /**
     * Obtiene movimientos con filtros múltiples
     */
    public Page<MovimientoInventario> obtenerMovimientosConFiltros(
            Long productoId, String tipoMovimiento, String usuarioResponsable,
            LocalDateTime fechaInicio, LocalDateTime fechaFin, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        return movimientoInventarioRepository.findMovimientosConFiltros(
                productoId, tipoMovimiento, usuarioResponsable, fechaInicio, fechaFin, pageable);
    }

    /**
     * Obtiene movimientos por tipo (ENTRADA o SALIDA)
     */
    public List<MovimientoInventario> obtenerMovimientosPorTipo(String tipoMovimiento) {
        return movimientoInventarioRepository.findByTipoMovimientoOrderByFechaMovimientoDesc(tipoMovimiento.toUpperCase());
    }

    /**
     * Obtiene movimientos por usuario responsable
     */
    public List<MovimientoInventario> obtenerMovimientosPorUsuario(String usuarioResponsable) {
        return movimientoInventarioRepository.findByUsuarioResponsableOrderByFechaMovimientoDesc(usuarioResponsable);
    }
}