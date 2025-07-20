package org.example.controller;

import org.example.Dtos.MovimientoInventarioDto;
import org.example.Dtos.ProductoDto;
import org.example.Entity.MovimientoInventario;
import org.example.Entity.Producto;
import org.example.Request.StockUpdateRequest;
import org.example.Service.InventarioService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inventario")
public class InventarioController {

    private final InventarioService inventarioService;
    private final ModelMapper modelMapper;

    public InventarioController(InventarioService inventarioService, ModelMapper modelMapper) {
        this.inventarioService = inventarioService;
        this.modelMapper = modelMapper;
    }

    @PatchMapping("/productos/{productoId}/stock")
    public ResponseEntity<ProductoDto> actualizarStock(
            @PathVariable Long productoId,
            @RequestBody StockUpdateRequest request) {

        Producto producto = inventarioService.actualizarStock(productoId, request);

        ProductoDto dto = modelMapper.map(producto, ProductoDto.class);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/historial")
    public ResponseEntity<Page<MovimientoInventarioDto>> obtenerHistorialDeMovimientos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<MovimientoInventario> historial = inventarioService.obtenerHistorialDeMovimientos(page, size);
        return ResponseEntity.ok(historial.map(movimiento -> modelMapper.map(movimiento, MovimientoInventarioDto.class)));
    }

    @GetMapping("/historial/producto/{productoId}")
    public ResponseEntity<List<MovimientoInventarioDto>> obtenerHistorialPorProducto(@PathVariable Long productoId) {
        List<MovimientoInventario> historial = inventarioService.obtenerHistorialPorProducto(productoId);
        return ResponseEntity.ok(historial.stream().map(movimiento -> modelMapper.map(movimiento, MovimientoInventarioDto.class)).toList());

    }

    @GetMapping("/historial/tipo/{tipoMovimiento}")
    public ResponseEntity<List<MovimientoInventarioDto>> obtenerMovimientosPorTipo(
            @PathVariable String tipoMovimiento
    ) {
        List<MovimientoInventario> movimientos = inventarioService.obtenerMovimientosPorTipo(tipoMovimiento);

        return ResponseEntity.ok(movimientos.stream().map(movimiento -> modelMapper.map(movimiento, MovimientoInventarioDto.class)).toList());
    }

    @GetMapping("/historial/usuario/{usuarioResponsable}")
    public ResponseEntity<List<MovimientoInventarioDto>> obtenerMovimientosPorUsuario(
            @PathVariable String usuarioResponsable
    ) {
        List<MovimientoInventario> movimientos = inventarioService.obtenerMovimientosPorUsuario(usuarioResponsable);
        return ResponseEntity.ok(movimientos.stream().map(movimiento -> modelMapper.map(movimiento, MovimientoInventarioDto.class)).toList());
    }
}