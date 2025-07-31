package org.example.Specifiation;

import jakarta.persistence.criteria.Predicate;
import org.example.Entity.MovimientoInventario;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MovimientoInventarioSpecification {

    public static Specification<MovimientoInventario> conFiltros(String tipo, String fecha) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filtro por tipo
            if (tipo != null && !tipo.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.upper(root.get("tipoMovimiento")),
                        tipo.toUpperCase()
                ));
            }

            // Filtro por fecha
            if (fecha != null && !fecha.trim().isEmpty()) {
                LocalDate parsedDate = LocalDate.parse(fecha);
                LocalDateTime inicioDelDia = parsedDate.atStartOfDay();
                LocalDateTime finDelDia = parsedDate.plusDays(1).atStartOfDay();

                predicates.add(criteriaBuilder.between(
                        root.get("fechaMovimiento"), inicioDelDia, finDelDia
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}