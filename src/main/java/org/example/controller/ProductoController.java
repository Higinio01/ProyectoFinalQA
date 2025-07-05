package org.example.controller;

import org.example.Dtos.ProductoDto;
import org.example.Entity.Categoria;
import org.example.Entity.Producto;
import org.example.Request.ProductoRequest;
import org.example.Service.ProductoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @PostMapping
    public ResponseEntity<ProductoDto> crearProducto(@RequestBody ProductoRequest request) {
        ProductoDto creado = productoService.crearProducto(request);
        return ResponseEntity.ok(creado);
    }

    @GetMapping
    public ResponseEntity<Page<ProductoDto>> listarProductos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<ProductoDto> productos = productoService.obtenerProductosPaginados(page, size);
        return ResponseEntity.ok(productos);
    }



    @GetMapping("/{id}")
    public ResponseEntity<ProductoDto> obtenerProducto(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.productoPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoDto> actualizarProducto(@PathVariable Long id, @RequestBody ProductoRequest request) {
        return ResponseEntity.ok(productoService.actualizarProducto(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        productoService.eliminarProducto(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/filtro")
    public ResponseEntity<Page<ProductoDto>> listarProductosFiltrados(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) Double precioMin,
            @RequestParam(required = false) Double precioMax,
            @RequestParam(required = false) String busqueda,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Producto> productos = productoService.buscarProductos(nombre, categoria, precioMin, precioMax, busqueda, pageable);
        Page<ProductoDto> dtoPage = productos.map(p -> new ProductoDto(p.getId(),p.getNombre(),p.getDescripcion(), p.getCategoria(), p.getPrecio(), p.getCantidad()));

        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/listar-categorias")
    public ResponseEntity<List<String>> listarCategorias() {
        List<String> categorias = Arrays.stream(Categoria.values())
                .map(Enum::name)
                .collect(Collectors.toList());
        return ResponseEntity.ok(categorias);
    }

}