package org.example.Repository;

import org.example.Entity.MovimientoInventario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long>, JpaSpecificationExecutor<MovimientoInventario> {

    // Buscar movimientos por producto
    List<MovimientoInventario> findByProductoIdOrderByFechaMovimientoDesc(Long productoId);

    // Buscar movimientos por producto con paginación
    Page<MovimientoInventario> findByProductoIdOrderByFechaMovimientoDesc(Long productoId, Pageable pageable);

    // Buscar movimientos por tipo
    List<MovimientoInventario> findByTipoMovimientoOrderByFechaMovimientoDesc(String tipoMovimiento);

    // Buscar movimientos en un rango de fechas
    @Query("SELECT m FROM MovimientoInventario m WHERE m.fechaMovimiento BETWEEN :fechaInicio AND :fechaFin ORDER BY m.fechaMovimiento DESC")
    List<MovimientoInventario> findByFechaMovimientoBetween(
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin);

    // Buscar movimientos por usuario
    List<MovimientoInventario> findByUsuarioResponsableOrderByFechaMovimientoDesc(String usuarioResponsable);

    // Buscar movimientos con filtros múltiples
    @Query("SELECT m FROM MovimientoInventario m WHERE " +
            "(:productoId IS NULL OR m.producto.id = :productoId) AND " +
            "(:tipoMovimiento IS NULL OR m.tipoMovimiento = :tipoMovimiento) AND " +
            "(:usuarioResponsable IS NULL OR m.usuarioResponsable = :usuarioResponsable) AND " +
            "(:fechaInicio IS NULL OR m.fechaMovimiento >= :fechaInicio) AND " +
            "(:fechaFin IS NULL OR m.fechaMovimiento <= :fechaFin) " +
            "ORDER BY m.fechaMovimiento DESC")
    Page<MovimientoInventario> findMovimientosConFiltros(
            @Param("productoId") Long productoId,
            @Param("tipoMovimiento") String tipoMovimiento,
            @Param("usuarioResponsable") String usuarioResponsable,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin,
            Pageable pageable);

    // Obtener últimos movimientos
    Page<MovimientoInventario> findAllByOrderByFechaMovimientoDesc(Pageable pageable);

    void deleteByProductoId(Long id);

    List<MovimientoInventario> findByTipoMovimiento(String salida);

    Page<MovimientoInventario> findByTipoMovimientoIgnoreCase(String tipoMovimiento, Pageable pageable);

    Page<MovimientoInventario> findByFechaMovimientoBetween(LocalDateTime inicio, LocalDateTime fin, Pageable pageable);

    Page<MovimientoInventario> findByTipoMovimientoAndFechaMovimientoBetween(
            String tipoMovimiento,
            LocalDateTime inicio,
            LocalDateTime fin,
            Pageable pageable
    );

}