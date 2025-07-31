package org.example.Controller;

import org.example.Dtos.ProductoDto;
import org.example.Entity.Categoria;
import org.example.Entity.Producto;
import org.example.Request.ProductoRequest;
import org.example.Request.StockUpdateRequest;
import org.example.Service.InventarioService;
import org.example.Service.ProductoService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;
    private final InventarioService inventarioService;
    private final ModelMapper modelMapper;

    public ProductoController(ProductoService productoService, InventarioService inventarioService, ModelMapper modelMapper) {
        this.productoService = productoService;
        this.inventarioService = inventarioService;
        this.modelMapper = modelMapper;
    }

    @GetMapping
    public ResponseEntity<Page<ProductoDto>> listarProductos(
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<Producto> productos = productoService.obtenerProductosPaginados(pageable);
        return ResponseEntity.ok(productos.map(producto -> modelMapper.map(producto, ProductoDto.class)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoDto> obtenerProducto(@PathVariable Long id) {
        Producto producto = productoService.productoPorId(id);
        ProductoDto dto = modelMapper.map(producto, ProductoDto.class);
        return ResponseEntity.ok(dto);
    }
    @GetMapping("/filtro")
    public ResponseEntity<Page<ProductoDto>> listarProductosFiltrados(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) Double precioMin,
            @RequestParam(required = false) Double precioMax,
            @RequestParam(required = false) String busqueda,
            Pageable pageable) {

        Page<Producto> productos = productoService.buscarProductos(
                nombre, categoria, precioMin, precioMax, busqueda, pageable);

        Page<ProductoDto> dtoPage = productos.map(producto ->
                modelMapper.map(producto, ProductoDto.class));

        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/listar-categorias")
    public ResponseEntity<List<String>> listarCategorias() {
        List<String> categorias = Arrays.stream(Categoria.values())
                .map(Enum::name)
                .collect(Collectors.toList());
        return ResponseEntity.ok(categorias);
    }

    @PostMapping
    public ResponseEntity<ProductoDto> crearProducto(@RequestBody ProductoRequest request) {
        Producto producto = productoService.crearProducto(request);
        ProductoDto productoDto = modelMapper.map(producto, ProductoDto.class);
        return ResponseEntity.ok(productoDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoDto> actualizarProducto(@PathVariable Long id, @RequestBody ProductoRequest request) {
        Producto producto = productoService.actualizarProducto(id, request);
        ProductoDto dto = modelMapper.map(producto, ProductoDto.class);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        productoService.eliminarProducto(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<ProductoDto> actualizarStock(
            @PathVariable Long id,
            @RequestBody StockUpdateRequest request) {

        Producto producto = inventarioService.actualizarStock(id, request);

        ProductoDto dto = modelMapper.map(producto, ProductoDto.class);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/dashboard-metricas")
    public ResponseEntity<Map<String, Object>> obtenerMetricasProductos() {
        Map<String, Object> metricas = productoService.obtenerMetricasDashboard();
        return ResponseEntity.ok(metricas);
    }

    @GetMapping("/stock-bajo")
    public ResponseEntity<List<ProductoDto>> obtenerProductosConStockBajo() {
        List<Producto> productosBajoStock = inventarioService.obtenerProductosConStockBajo();
        return ResponseEntity.ok(productosBajoStock.stream().map(producto -> modelMapper.map(producto, ProductoDto.class)).toList());
    }
}