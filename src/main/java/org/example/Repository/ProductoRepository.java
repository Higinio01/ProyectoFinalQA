package org.example.Repository;

import org.example.Entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long>, JpaSpecificationExecutor<Producto> {

    @Query("SELECT p FROM Producto p WHERE p.cantidad <= p.minimoStock")
    List<Producto> findProductosConStockBajo();

}
