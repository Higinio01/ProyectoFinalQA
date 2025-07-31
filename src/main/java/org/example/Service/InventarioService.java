package org.example.Service;

import org.example.Entity.MovimientoInventario;
import org.example.Entity.Producto;
import org.example.Exception.ProductoException;
import org.example.Repository.MovimientoInventarioRepository;
import org.example.Repository.ProductoRepository;
import org.example.Request.StockUpdateRequest;
import org.example.Specifiation.MovimientoInventarioSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InventarioService {

    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final ProductoRepository productoRepository;

    public InventarioService(MovimientoInventarioRepository movimientoInventarioRepository,
                             ProductoRepository productoRepository) {
        this.movimientoInventarioRepository = movimientoInventarioRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional
    public Producto actualizarStock(Long productoId, StockUpdateRequest request) {

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ProductoException.NoEncontrado("Producto no encontrado con id: " + productoId));

        int cantidadAnterior = producto.getCantidad();
        int nuevaCantidad = getNuevaCantidad(request, cantidadAnterior);

        producto.setCantidad(nuevaCantidad);
        Producto productoActualizado = productoRepository.save(producto);

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

    private static int getNuevaCantidad(StockUpdateRequest request, int cantidadAnterior) {
        int nuevaCantidad;

        if (request.tipoMovimiento().equalsIgnoreCase("ENTRADA")) {
            nuevaCantidad = cantidadAnterior + request.cantidad();
        } else {
            if (cantidadAnterior < request.cantidad()) {
                throw new ProductoException.ValorInvalido(
                        String.format("Stock insuficiente. Stock actual: %d, cantidad solicitada: %d",
                                cantidadAnterior, request.cantidad())
                );
            }
            nuevaCantidad = cantidadAnterior - request.cantidad();
        }
        return nuevaCantidad;
    }

    public Page<MovimientoInventario> obtenerHistorialDeMovimientos(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return movimientoInventarioRepository.findAllByOrderByFechaMovimientoDesc(pageable);
    }

    public Page<MovimientoInventario> obtenerHistorialDeMovimientos(String tipo, String fecha, Pageable pageable) {

        if ((tipo == null || tipo.isBlank()) && (fecha == null || fecha.isBlank())) {
            return movimientoInventarioRepository.findAll(pageable);
        }

        Specification<MovimientoInventario> spec = MovimientoInventarioSpecification.conFiltros(tipo, fecha);
        return movimientoInventarioRepository.findAll(spec, pageable);
    }



    public List<MovimientoInventario> obtenerHistorialPorProducto(Long productoId) {
        return movimientoInventarioRepository.findByProductoIdOrderByFechaMovimientoDesc(productoId);
    }

//    public Page<MovimientoInventario> obtenerMovimientosConFiltros(
//            Long productoId, String tipoMovimiento, String usuarioResponsable,
//            LocalDateTime fechaInicio, LocalDateTime fechaFin, int page, int size) {
//
//        Pageable pageable = PageRequest.of(page, size);
//        return movimientoInventarioRepository.findMovimientosConFiltros(
//                productoId, tipoMovimiento, usuarioResponsable, fechaInicio, fechaFin, pageable);
//    }

    public List<MovimientoInventario> obtenerMovimientosPorTipo(String tipoMovimiento) {
        return movimientoInventarioRepository.findByTipoMovimientoOrderByFechaMovimientoDesc(tipoMovimiento.toUpperCase());
    }

    public List<MovimientoInventario> obtenerMovimientosPorUsuario(String usuarioResponsable) {
        return movimientoInventarioRepository.findByUsuarioResponsableOrderByFechaMovimientoDesc(usuarioResponsable);
    }

    public Map<String, Object> obtenerEstadisticasGenerales() {
        List<Producto> productos = productoRepository.findAll();
        List<MovimientoInventario> movimientos = movimientoInventarioRepository.findAll();

        Map<String, Long> movimientosPorTipo = movimientos.stream()
                .collect(Collectors.groupingBy(MovimientoInventario::getTipoMovimiento, Collectors.counting()));

        Map<String, Long> stockPorCategoria = productos.stream()
                .collect(Collectors.groupingBy(p -> p.getCategoria().toString(),
                        Collectors.summingLong(Producto::getCantidad)));

        Map<String, Object> result = new HashMap<>();
        result.put("totalProductos", productos.size());
        result.put("stockTotal", productos.stream().mapToInt(Producto::getCantidad).sum());
        result.put("movimientosPorTipo", movimientosPorTipo);
        result.put("stockPorCategoria", stockPorCategoria);

        return result;
    }

    public List<Map<String, Object>> obtenerTopProductosPorVentas() {
        List<MovimientoInventario> movimientos = movimientoInventarioRepository.findByTipoMovimiento("SALIDA");

        Map<Producto, Float> ventasPorProducto = new HashMap<>();

        for (MovimientoInventario mov : movimientos) {
            Producto prod = mov.getProducto();
            float totalVenta = prod.getPrecio() * mov.getCantidad();
            ventasPorProducto.put(prod, ventasPorProducto.getOrDefault(prod, 0f) + totalVenta);
        }

        return ventasPorProducto.entrySet().stream()
                .sorted((a, b) -> Float.compare(b.getValue(), a.getValue()))
                .limit(5)
                .map(entry -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("nombre", entry.getKey().getNombre());
                    map.put("valorTotal", entry.getValue());
                    return map;
                }).toList();
    }

    public List<Producto> obtenerProductosConStockBajo() {
        return productoRepository.findProductosConStockBajo()
                .stream()
                .map(p -> new Producto(
                        p.getId(),
                        p.getNombre(),
                        p.getDescripcion(),
                        p.getCategoria(),
                        p.getPrecio(),
                        p.getCantidad()
                ))
                .toList();
    }




}