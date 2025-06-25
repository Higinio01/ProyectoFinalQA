package org.example.Repository;

import org.example.Entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Boolean existsByEmail(String correoElectronico);
    boolean existsByEmailAndIdNot(String correoElectronico, Long id);
    Optional<Usuario> findByEmail(String email);
}
