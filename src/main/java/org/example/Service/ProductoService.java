package org.example.Service;

import jakarta.validation.constraints.NotNull;
import org.example.Entity.Categoria;
import org.example.Entity.Producto;
import org.example.Exception.ProductoException;
import org.example.Repository.MovimientoInventarioRepository;
import org.example.Repository.ProductoRepository;
import org.example.Request.ProductoRequest;
import org.example.Request.StockUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductoService {
    private final ProductoRepository productoRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;

    public ProductoService(ProductoRepository productoRepository, MovimientoInventarioRepository movimientoInventarioRepository) {
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

        return productoRepository.save(productoExistente);
    }

    public Producto actualizarStock(Long id, StockUpdateRequest request) {
        var producto = productoRepository.findById(id)
                .orElseThrow(() -> new ProductoException.NoEncontrado("Producto no encontrado con id: " + id));

        int cantidadActual = producto.getCantidad();
        int nuevaCantidad = getNuevaCantidad(request, cantidadActual);

        producto.setCantidad(nuevaCantidad);

        System.out.printf("Movimiento de stock - Producto: %s, Tipo: %s, Cantidad: %d, Stock anterior: %d, Stock nuevo: %d, Motivo: %s%n",
                producto.getNombre(),
                request.tipoMovimiento(),
                request.cantidad(),
                cantidadActual,
                nuevaCantidad,
                request.motivo() != null ? request.motivo() : "No especificado");

        return productoRepository.save(producto);
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

    @Transactional
    public void eliminarProducto(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ProductoException.NoEncontrado("Producto no encontrado con id: " + id));

        // Eliminar movimientos primero
        movimientoInventarioRepository.deleteByProductoId(id);

        // Luego eliminar el producto
        productoRepository.delete(producto);
    }

    public Page<Producto> buscarProductos(String nombre, String categoria,
                                          Double precioMin, Double precioMax, String buscador, Pageable pageable) {
        return productoRepository.buscarConFiltros(nombre, categoria, precioMin, precioMax, buscador, pageable);
    }

    private void validarPrecioYCantidad(Double precio, Integer cantidad) {
        if (precio != null && precio < 0) {
            throw new ProductoException.ValorInvalido("El precio no puede ser negativo: " + precio);
        }
        if (cantidad != null && cantidad < 0) {
            throw new ProductoException.ValorInvalido("La cantidad no puede ser negativa: " + cantidad);
        }
    }

    public Page<Producto> obtenerProductosPaginados(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return productoRepository.findAll(pageable);
    }
}
