package org.example.Service;

import jakarta.validation.constraints.NotNull;
import org.example.Entity.Categoria;
import org.example.Entity.MovimientoInventario;
import org.example.Entity.Producto;
import org.example.Exception.ProductoException;
import org.example.Repository.MovimientoInventarioRepository;
import org.example.Repository.ProductoRepository;
import org.example.Request.ProductoRequest;
import org.example.Request.StockUpdateRequest;
import org.example.Metrics.Counted;
import org.example.Metrics.Timed;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductoService {
    private final ProductoRepository productoRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;

    public ProductoService(ProductoRepository productoRepository,
                           MovimientoInventarioRepository movimientoInventarioRepository) {
        this.productoRepository = productoRepository;
        this.movimientoInventarioRepository = movimientoInventarioRepository;
    }

    @Timed(value = "inventario_producto_busqueda_tiempo", description = "Tiempo de búsqueda de producto por ID")
    public Producto productoPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ProductoException.NoEncontrado("Producto no encontrado con id: " + id));
    }

    @Timed(value = "inventario_productos_listado_tiempo", description = "Tiempo de listado de todos los productos")
    public List<Producto> obtenerTodosLosProductos() {
        return productoRepository.findAll();
    }

    @Counted(value = "inventario_productos_creados_total", description = "Total de productos creados")
    @Timed(value = "inventario_producto_creacion_tiempo", description = "Tiempo de creación de producto")
    public Producto crearProducto(ProductoRequest request) {
        validarPrecioYCantidad((double) request.precio(), request.cantidad());
        var producto = new Producto();
        return getProducto(request, producto);
    }

    @Timed(value = "inventario_producto_actualizacion_tiempo", description = "Tiempo de actualización de producto")
    public Producto actualizarProducto(Long id, ProductoRequest request) {
        var productoExistente = productoRepository.findById(id)
                .orElseThrow(() -> new ProductoException.NoEncontrado("Producto no encontrado con id: " + id));

        validarPrecioYCantidad((double) request.precio(), request.cantidad());
        return getProducto(request, productoExistente);
    }

    @Counted(value = "inventario_stock_actualizaciones_total", description = "Total de actualizaciones de stock")
    @Timed(value = "inventario_stock_actualizacion_tiempo", description = "Tiempo de actualización de stock")
    public Producto actualizarStock(Long id, StockUpdateRequest request) {
        var producto = productoRepository.findById(id)
                .orElseThrow(() -> new ProductoException.NoEncontrado("Producto no encontrado con id: " + id));

        int cantidadActual = producto.getCantidad();
        int nuevaCantidad = getNuevaCantidad(request, cantidadActual);
        producto.setCantidad(nuevaCantidad);

        System.out.printf("Movimiento de stock - Producto: %s, Tipo: %s, Cantidad: %d, Stock anterior: %d, Stock nuevo: %d, Motivo: %s%n",
                producto.getNombre(), request.tipoMovimiento(), request.cantidad(),
                cantidadActual, nuevaCantidad, request.motivo() != null ? request.motivo() : "No especificado");

        return productoRepository.save(producto);
    }

    @Counted(value = "inventario_productos_eliminados_total", description = "Total de productos eliminados")
    @Transactional
    public void eliminarProducto(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ProductoException.NoEncontrado("Producto no encontrado con id: " + id));

        movimientoInventarioRepository.deleteByProductoId(id);
        productoRepository.delete(producto);
    }

    @Timed(value = "inventario_busqueda_filtrada_tiempo", description = "Tiempo de búsqueda filtrada")
    public Page<Producto> buscarProductos(String nombre, String categoria, Double precioMin,
                                          Double precioMax, String buscador, Pageable pageable) {
        return productoRepository.buscarConFiltros(nombre, categoria, precioMin, precioMax, buscador, pageable);
    }

    @Timed(value = "inventario_productos_paginados_tiempo", description = "Tiempo de consulta paginada")
    public Page<Producto> obtenerProductosPaginados(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return productoRepository.findAll(pageable);
    }

    // === MÉTODOS PRIVADOS (SIN CAMBIOS) ===
    @NotNull
    private Producto getProducto(ProductoRequest request, Producto productoExistente) {
        productoExistente.setNombre(request.nombre());
        productoExistente.setDescripcion(request.descripcion());

        try {
            Categoria categoria = Categoria.valueOf(request.categoria().toUpperCase());
            productoExistente.setCategoria(categoria);
        } catch (IllegalArgumentException e) {
            throw new ProductoException.CategoriaInvalida("Categoría inválida: " + request.categoria());
        }

        productoExistente.setPrecio(request.precio());
        productoExistente.setCantidad(request.cantidad());
        productoExistente.setMinimoStock(request.minimoStock());

        return productoRepository.save(productoExistente);
    }

    private static int getNuevaCantidad(StockUpdateRequest request, int cantidadActual) {
        int nuevaCantidad;

        if (request.tipoMovimiento().equalsIgnoreCase("ENTRADA")) {
            nuevaCantidad = cantidadActual + Math.abs(request.cantidad());
        } else {
            int cantidadSalida = Math.abs(request.cantidad());

            if (cantidadActual < cantidadSalida) {
                throw new ProductoException.ValorInvalido(
                        String.format("Stock insuficiente. Stock actual: %d, cantidad solicitada: %d",
                                cantidadActual, cantidadSalida)
                );
            }

            nuevaCantidad = cantidadActual - cantidadSalida;
        }
        return nuevaCantidad;
    }

    private void validarPrecioYCantidad(Double precio, Integer cantidad) {
        if (precio != null && precio < 0) {
            throw new ProductoException.ValorInvalido("El precio no puede ser negativo: " + precio);
        }
        if (cantidad != null && cantidad < 0) {
            throw new ProductoException.ValorInvalido("La cantidad no puede ser negativa: " + cantidad);
        }
    }

    public Map<String, Object> obtenerMetricasDashboard() {
        List<Producto> productos = productoRepository.findAll();

        long total = productos.size();
        long stockBajo = productos.stream().filter(p -> p.getCantidad() < p.getMinimoStock()).count();

        List<Map<String, Object>> actividad = movimientoInventarioRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        m -> m.getProducto().getNombre(),
                        Collectors.summingInt(MovimientoInventario::getCantidad)))
                .entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(5)
                .map(entry -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("nombre", entry.getKey());
                    map.put("cantidad", entry.getValue());
                    return map;
                })
                .toList();

        Map<String, Object> datos = new HashMap<>();
        datos.put("totalProductos", total);
        datos.put("stockBajo", stockBajo);
        datos.put("topActividad", actividad);

        return datos;
    }
}