package org.example.Service;

import org.example.Entity.MovimientoInventario;
import org.example.Entity.Producto;
import org.example.Exception.ProductoException;
import org.example.Repository.MovimientoInventarioRepository;
import org.example.Repository.ProductoRepository;
import org.example.Request.StockUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class InventarioServiceTest {

    private InventarioService inventarioService;
    private MovimientoInventarioRepository movimientoRepo;
    private ProductoRepository productoRepo;

    @BeforeEach
    void setUp() {
        movimientoRepo = mock(MovimientoInventarioRepository.class);
        productoRepo = mock(ProductoRepository.class);
        inventarioService = new InventarioService(movimientoRepo, productoRepo);
    }

    @Test
    @Tag("critical")
    void actualizarStock_entrada_exitoso() {
        Producto producto = new Producto();
        producto.setId(1L);
        producto.setCantidad(10);

        StockUpdateRequest request = new StockUpdateRequest(5, "ENTRADA", "Reabastecimiento", "admin");

        when(productoRepo.findById(1L)).thenReturn(Optional.of(producto));
        when(productoRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        Producto actualizado = inventarioService.actualizarStock(1L, request);

        assertEquals(15, actualizado.getCantidad());
        verify(movimientoRepo).save(any(MovimientoInventario.class));
    }

    @Test
    @Tag("critical")
    void actualizarStock_salida_exitoso() {
        Producto producto = new Producto();
        producto.setId(1L);
        producto.setCantidad(10);

        StockUpdateRequest request = new StockUpdateRequest(3, "SALIDA", "Venta", "admin");

        when(productoRepo.findById(1L)).thenReturn(Optional.of(producto));
        when(productoRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        Producto actualizado = inventarioService.actualizarStock(1L, request);

        assertEquals(7, actualizado.getCantidad());
        verify(movimientoRepo).save(any(MovimientoInventario.class));
    }

    @Test
    void actualizarStock_salida_stockInsuficiente_lanzaExcepcion() {
        Producto producto = new Producto();
        producto.setId(1L);
        producto.setCantidad(2);

        StockUpdateRequest request = new StockUpdateRequest(5, "SALIDA", "Venta", "admin");

        when(productoRepo.findById(1L)).thenReturn(Optional.of(producto));

        ProductoException.ValorInvalido ex = assertThrows(ProductoException.ValorInvalido.class,
                () -> inventarioService.actualizarStock(1L, request));

        assertTrue(ex.getMessage().contains("Stock insuficiente"));
    }

    @Test
    void actualizarStock_productoNoExiste_lanzaExcepcion() {
        when(productoRepo.findById(99L)).thenReturn(Optional.empty());

        StockUpdateRequest request = new StockUpdateRequest(5, "ENTRADA", "Carga inicial", "sistema");

        ProductoException.NoEncontrado ex = assertThrows(ProductoException.NoEncontrado.class,
                () -> inventarioService.actualizarStock(99L, request));

        assertTrue(ex.getMessage().contains("Producto no encontrado"));
    }

    @Test
    void obtenerHistorialDeMovimientos_devuelvePaginado() {
        @SuppressWarnings("unchecked")
        Page<MovimientoInventario> paginaMock = mock(Page.class);
        when(movimientoRepo.findAllByOrderByFechaMovimientoDesc(any(Pageable.class)))
                .thenReturn(paginaMock);

        Page<MovimientoInventario> result = inventarioService.obtenerHistorialDeMovimientos(0, 5);

        assertEquals(paginaMock, result);
    }

    @Test
    void obtenerHistorialPorProducto_devuelveLista() {
        List<MovimientoInventario> movimientos = List.of(new MovimientoInventario(), new MovimientoInventario());
        when(movimientoRepo.findByProductoIdOrderByFechaMovimientoDesc(1L)).thenReturn(movimientos);

        List<MovimientoInventario> result = inventarioService.obtenerHistorialPorProducto(1L);

        assertEquals(2, result.size());
    }

    @Test
    void obtenerMovimientosConFiltros_devuelvePaginado() {
        @SuppressWarnings("unchecked")
        Page<MovimientoInventario> paginaMock = mock(Page.class);

        when(movimientoRepo.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(paginaMock);

        String tipo = "ENTRADA";
        String fecha = "2025-07-30";
        Pageable pageable = PageRequest.of(0, 10);

        Page<MovimientoInventario> result = inventarioService.obtenerHistorialDeMovimientos(tipo, fecha, pageable);

        assertEquals(paginaMock, result);
        verify(movimientoRepo).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void obtenerMovimientosPorTipo_devuelveLista() {
        when(movimientoRepo.findByTipoMovimientoOrderByFechaMovimientoDesc("SALIDA"))
                .thenReturn(List.of(new MovimientoInventario()));

        List<MovimientoInventario> result = inventarioService.obtenerMovimientosPorTipo("salida");

        assertEquals(1, result.size());
    }

    @Test
    void obtenerMovimientosPorUsuario_devuelveLista() {
        when(movimientoRepo.findByUsuarioResponsableOrderByFechaMovimientoDesc("admin"))
                .thenReturn(List.of(new MovimientoInventario()));

        List<MovimientoInventario> result = inventarioService.obtenerMovimientosPorUsuario("admin");

        assertEquals(1, result.size());
    }
}
