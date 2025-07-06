package org.example.controller;

import org.example.Dtos.RolDto;
import org.example.Service.RolService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RolController {
    private final RolService rolService;

    public RolController(RolService rolService) {
        this.rolService = rolService;
    }

    @GetMapping
    public ResponseEntity<List<RolDto>> listarRoles() {
        List<RolDto> roles = rolService.listarRoles();
        return ResponseEntity.ok(roles);
    }
}
