package org.example.Repository;

import org.example.Entity.MovimientoInventario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long>, JpaSpecificationExecutor<MovimientoInventario> {

    List<MovimientoInventario> findByProductoIdOrderByFechaMovimientoDesc(Long productoId);

    List<MovimientoInventario> findByTipoMovimientoOrderByFechaMovimientoDesc(String tipoMovimiento);

    List<MovimientoInventario> findByUsuarioResponsableOrderByFechaMovimientoDesc(String usuarioResponsable);

    Page<MovimientoInventario> findAllByOrderByFechaMovimientoDesc(Pageable pageable);

    void deleteByProductoId(Long id);

    List<MovimientoInventario> findByTipoMovimiento(String salida);

}