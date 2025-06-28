package org.example.controller;

import org.example.Service.InventarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventario")
public class InventarioController {

    private final InventarioService inventarioService;

    public InventarioController(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    @PostMapping("/actualizar")
    public ResponseEntity<Void> actualizarStock(
            @RequestParam Long productoId,
            @RequestParam Integer cantidad,
            @RequestParam String tipoMovimiento,
            @RequestParam(required = false) String descripcion
    ) {
        inventarioService.actualizarStock(productoId, cantidad, tipoMovimiento, descripcion);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/historial")
    public ResponseEntity<?> obtenerHistorialDeMovimientos() {
        inventarioService.obtenerHistorialDeMovimientos();
        return ResponseEntity.ok().build();
    }
}

