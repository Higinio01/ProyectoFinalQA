package org.example.Service;

import jakarta.validation.constraints.NotNull;
import org.example.Entity.Categoria;
import org.example.Entity.MovimientoInventario;
import org.example.Entity.Producto;
import org.example.Exception.ProductoException;
import org.example.Specifiation.ProductoSpecification;
import org.example.Repository.MovimientoInventarioRepository;
import org.example.Repository.ProductoRepository;
import org.example.Request.ProductoRequest;
import org.example.Request.StockUpdateRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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

    public Producto productoPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ProductoException.NoEncontrado("Producto no encontrado con id: " + id));
    }

    public List<Producto> obtenerTodosLosProductos() {
        return productoRepository.findAll();
    }

    public Producto crearProducto(ProductoRequest request) {
        validarPrecioYCantidad((double) request.precio(), request.cantidad());
        var producto = new Producto();
        return getProducto(request, producto);
    }

    public Producto actualizarProducto(Long id, ProductoRequest request) {
        var productoExistente = productoRepository.findById(id)
                .orElseThrow(() -> new ProductoException.NoEncontrado("Producto no encontrado con id: " + id));

        validarPrecioYCantidad((double) request.precio(), request.cantidad());
        return getProducto(request, productoExistente);
    }

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

    @Transactional
    public void eliminarProducto(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ProductoException.NoEncontrado("Producto no encontrado con id: " + id));

        movimientoInventarioRepository.deleteByProductoId(id);
        productoRepository.delete(producto);
    }

    public Page<Producto> buscarProductos(String nombre, String categoria, Double precioMin,
                                          Double precioMax, String busqueda, Pageable pageable) {

        Specification<Producto> spec = ProductoSpecification.conFiltros(
                nombre, categoria, precioMin, precioMax, busqueda);

        return productoRepository.findAll(spec, pageable);
    }

    public Page<Producto> obtenerProductosPaginados(Pageable pageable) {
        return productoRepository.findAll(pageable);
    }

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