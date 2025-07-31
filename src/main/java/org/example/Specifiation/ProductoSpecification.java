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

            // Filtro por nombre
            if (nombre != null && !nombre.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("nombre")),
                        "%" + nombre.toLowerCase() + "%"
                ));
            }

            // Filtro por categoría
            if (categoria != null && !categoria.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("categoria"), categoria));
            }

            // Filtro por precio mínimo
            if (precioMin != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("precio"), precioMin));
            }

            // Filtro por precio máximo
            if (precioMax != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("precio"), precioMax));
            }

            // Búsqueda general (nombre, descripción, categoría)
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