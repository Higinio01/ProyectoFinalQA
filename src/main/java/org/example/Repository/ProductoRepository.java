package org.example.Repository;

import org.example.Entity.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    @Query(value = "SELECT * FROM productos p WHERE " +
            "(:nombre IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', CAST(:nombre AS TEXT), '%'))) AND " +
            "(:categoria IS NULL OR p.categoria = :categoria) AND " +
            "(:precioMin IS NULL OR p.precio >= :precioMin) AND " +
            "(:precioMax IS NULL OR p.precio <= :precioMax) AND " +
            "(:busqueda IS NULL OR " +
            "  LOWER(p.nombre) LIKE LOWER(CONCAT('%', CAST(:busqueda AS TEXT), '%')) OR " +
            "  LOWER(p.descripcion) LIKE LOWER(CONCAT('%', CAST(:busqueda AS TEXT), '%')) OR " +
            "  LOWER(p.categoria) LIKE LOWER(CONCAT('%', CAST(:busqueda AS TEXT), '%')))",
            nativeQuery = true)
    Page<Producto> buscarConFiltros(@Param("nombre") String nombre,
                                    @Param("categoria") String categoria,
                                    @Param("precioMin") Double precioMin,
                                    @Param("precioMax") Double precioMax,
                                    @Param("busqueda") String busqueda,
                                    Pageable pageable);
}
