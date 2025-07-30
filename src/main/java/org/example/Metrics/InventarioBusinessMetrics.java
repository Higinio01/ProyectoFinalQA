package org.example.Metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.example.Repository.ProductoRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value = "management.metrics.export.prometheus.enabled", havingValue = "true")
public class InventarioBusinessMetrics {

    private final ProductoRepository productoRepository;

    public InventarioBusinessMetrics(MeterRegistry meterRegistry, ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;

        // Para Gauges usa esta sintaxis diferente
        Gauge.builder("inventario_productos_stock_bajo", this, InventarioBusinessMetrics::contarProductosStockBajo)
                .description("Productos con stock por debajo del mínimo")
                .register(meterRegistry);

        Gauge.builder("inventario_productos_total", this, InventarioBusinessMetrics::contarTotalProductos)
                .description("Total de productos en el sistema")
                .register(meterRegistry);
    }

    private double contarProductosStockBajo() {
        try {
            return productoRepository.findAll().stream()
                    .mapToLong(p -> p.getCantidad() < p.getMinimoStock() ? 1 : 0)
                    .sum();
        } catch (Exception e) {
            return 0;
        }
    }

    private double contarTotalProductos() {
        try {
            return productoRepository.count();
        } catch (Exception e) {
            return 0;
        }
    }
}