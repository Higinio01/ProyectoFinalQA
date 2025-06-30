package org.example.Service;

import org.example.Dtos.ProductoDto;
import org.example.Entity.Categoria;
import org.example.Entity.Producto;
import org.example.Repository.ProductoRepository;
import org.example.Request.ProductoRequest;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoService {
    private final ProductoRepository productoRepository;
    private final ModelMapper modelMapper;

    public ProductoService(ProductoRepository productoRepository, ModelMapper modelMapper) {
        this.productoRepository = productoRepository;
        this.modelMapper = modelMapper;
    }

    public ProductoDto productoPorId(Long id) {
        var producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        return modelMapper.map(producto, ProductoDto.class);
    }

    public List<ProductoDto> obtenerTodosLosProductos() {
        List<Producto> productos = productoRepository.findAll();
        return productos.stream()
                .map(producto -> modelMapper.map(producto, ProductoDto.class))
                .toList();
    }

    public ProductoDto crearProducto(ProductoRequest request) {
        var producto = new Producto();
        producto.setNombre(request.nombre());
        producto.setDescripcion(request.descripcion());
        try {
            Categoria categoria = Categoria.valueOf(request.categoria().toUpperCase());
            producto.setCategoria(categoria);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Categoría inválida: " + request.categoria());
        }
        producto.setPrecio(request.precio());
        producto.setCantidad(request.cantidad());

        var productoGuardado = productoRepository.save(producto);
        return modelMapper.map(productoGuardado, ProductoDto.class);
    }

    public ProductoDto actualizarProducto(Long id, ProductoRequest request) {
        var productoExistente = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        productoExistente.setNombre(request.nombre());
        productoExistente.setDescripcion(request.descripcion());

        try {
            Categoria categoria = Categoria.valueOf(request.categoria().toUpperCase());
            productoExistente.setCategoria(categoria);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Categoría inválida: " + request.categoria());
        }

        productoExistente.setPrecio(request.precio());
        productoExistente.setCantidad(request.cantidad());

        var productoActualizado = productoRepository.save(productoExistente);
        return modelMapper.map(productoActualizado, ProductoDto.class);
    }

    public void eliminarProducto(Long id) {
        var productoExistente = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        productoRepository.delete(productoExistente);
    }

    public Page<Producto> buscarProductos(String nombre, String categoria,
                                          Double precioMin, Double precioMax, Pageable pageable) {
        return productoRepository.buscarConFiltros(nombre, categoria, precioMin, precioMax, pageable);
    }

}
