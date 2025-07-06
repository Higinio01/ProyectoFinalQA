package org.example.Service;

import org.example.Dtos.RolDto;
import org.example.Repository.RolRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RolService {
    private final RolRepository rolRepository;

    public RolService(RolRepository rolRepository) {
        this.rolRepository = rolRepository;
    }

    public List<RolDto> listarRoles() {
        return rolRepository.findAll().stream()
                .map(rol -> new RolDto(rol.getId(), rol.getRolNombre().name()))
                .toList();
    }
}
