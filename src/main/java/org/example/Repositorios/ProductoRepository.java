package org.example.Repositorios;

import org.example.Entidades.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface    ProductoRepository extends JpaRepository<Producto, Long> {
}
