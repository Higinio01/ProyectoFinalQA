package org.example.Service;

import org.example.Dtos.ProductoDto;
import org.example.Entity.Categoria;
import org.example.Entity.Producto;
import org.example.Exception.ProductoException;
import org.example.Repository.ProductoRepository;
import org.example.Request.ProductoRequest;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
                .orElseThrow(() -> new ProductoException.NoEncontrado("Producto no encontrado con id: " + id));
        return modelMapper.map(producto, ProductoDto.class);
    }

    public List<ProductoDto> obtenerTodosLosProductos() {
        List<Producto> productos = productoRepository.findAll();
        return productos.stream()
                .map(producto -> modelMapper.map(producto, ProductoDto.class))
                .toList();
    }

    public ProductoDto crearProducto(ProductoRequest request) {
        validarPrecioYCantidad((double) request.precio(), request.cantidad());

        var producto = new Producto();
        producto.setNombre(request.nombre());
        producto.setDescripcion(request.descripcion());

        try {
            Categoria categoria = Categoria.valueOf(request.categoria().toUpperCase());
            producto.setCategoria(categoria);
        } catch (IllegalArgumentException e) {
            throw new ProductoException.CategoriaInvalida("Categoría inválida: " + request.categoria());
        }

        producto.setPrecio(request.precio());
        producto.setCantidad(request.cantidad());

        var productoGuardado = productoRepository.save(producto);
        return modelMapper.map(productoGuardado, ProductoDto.class);
    }

    public ProductoDto actualizarProducto(Long id, ProductoRequest request) {
        var productoExistente = productoRepository.findById(id)
                .orElseThrow(() -> new ProductoException.NoEncontrado("Producto no encontrado con id: " + id));

        validarPrecioYCantidad((double) request.precio(), request.cantidad());

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

        var productoActualizado = productoRepository.save(productoExistente);
        return modelMapper.map(productoActualizado, ProductoDto.class);
    }

    public void eliminarProducto(Long id) {
        var productoExistente = productoRepository.findById(id)
                .orElseThrow(() -> new ProductoException.NoEncontrado("Producto no encontrado con id: " + id));
        productoRepository.delete(productoExistente);
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

    public Page<ProductoDto> obtenerProductosPaginados(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Producto> productosPage = productoRepository.findAll(pageable);

        return productosPage.map(producto -> modelMapper.map(producto, ProductoDto.class));
    }

}
