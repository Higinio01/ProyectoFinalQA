package org.example.controller;

import org.example.Dtos.ProductoDto;
import org.example.Request.ProductoRequest;
import org.example.Service.ProductoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<List<ProductoDto>> listarProductos() {
        return ResponseEntity.ok(productoService.obtenerTodosLosProductos());
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
}