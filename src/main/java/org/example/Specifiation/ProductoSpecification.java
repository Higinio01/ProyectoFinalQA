package org.example.Specifiation;

import jakarta.persistence.criteria.Predicate;
import org.example.Entity.Producto;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ProductoSpecification {

    public static Specification<Producto> conFiltros(String nombre, String categoria,
                                                     Double precioMin, Double precioMax, String busqueda) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (nombre != null && !nombre.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("nombre")),
                        "%" + nombre.toLowerCase() + "%"
                ));
            }

            if (categoria != null && !categoria.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("categoria"), categoria));
            }

            if (precioMin != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("precio"), precioMin));
            }

            if (precioMax != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("precio"), precioMax));
            }

            if (busqueda != null && !busqueda.trim().isEmpty()) {
                String searchPattern = "%" + busqueda.toLowerCase() + "%";
                Predicate nombrePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("nombre")), searchPattern);
                Predicate descripcionPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("descripcion")), searchPattern);
                Predicate categoriaPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("categoria")), searchPattern);

                predicates.add(criteriaBuilder.or(nombrePredicate, descripcionPredicate, categoriaPredicate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}