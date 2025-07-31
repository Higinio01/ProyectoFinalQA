package org.example.Service;

import org.example.Entity.Producto;
import org.example.Exception.ProductoException;
import org.example.Repository.MovimientoInventarioRepository;
import org.example.Repository.ProductoRepository;
import org.example.Request.ProductoRequest;
import org.example.Request.StockUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProductoServiceTest {
    private ProductoService productoService;
    private ProductoRepository productoRepository;
    private MovimientoInventarioRepository movimientoInventarioRepository;

    @BeforeEach
    void setUp() {
        productoRepository = mock(ProductoRepository.class);
        movimientoInventarioRepository = mock(MovimientoInventarioRepository.class);
        productoService = new ProductoService(productoRepository, movimientoInventarioRepository);
    }

    @Test
    @Tag("critical")
    void crearProducto_exitoso() {
        ProductoRequest request = new ProductoRequest("Mouse", "Mouse inalámbrico", "ELECTRONICA", 20.5f, 15, 5);

        Producto productoMock = new Producto();
        productoMock.setId(1L);
        productoMock.setNombre("Mouse");

        when(productoRepository.save(any(Producto.class))).thenReturn(productoMock);

        Producto result = productoService.crearProducto(request);

        assertEquals("Mouse", result.getNombre());
        verify(productoRepository).save(any(Producto.class));
    }

    @Test
    void crearProducto_categoriaInvalida_lanzaExcepcion() {
        ProductoRequest request = new ProductoRequest("Mouse", "Mouse inalámbrico", "INVALIDA", 25.1f, 15, 5);

        ProductoException.CategoriaInvalida ex = assertThrows(ProductoException.CategoriaInvalida.class,
                () -> productoService.crearProducto(request));

        assertTrue(ex.getMessage().contains("Categoría inválida"));
    }

    @Test
    void crearProducto_precioNegativo_lanzaExcepcion() {
        ProductoRequest request = new ProductoRequest("Teclado", "Teclado mecánico", "ELECTRONICA", -10.0f, 5, 2);

        ProductoException.ValorInvalido ex = assertThrows(ProductoException.ValorInvalido.class,
                () -> productoService.crearProducto(request));

        assertTrue(ex.getMessage().contains("precio no puede ser negativo"));
    }

    @Test
    @Tag("critical")
    void actualizarProducto_exitoso() {
        Producto producto = new Producto();
        producto.setId(1L);

        ProductoRequest request = new ProductoRequest("Teclado", "Teclado gamer", "ELECTRONICA", 30.0f, 10, 3);

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);

        Producto actualizado = productoService.actualizarProducto(1L, request);

        assertEquals("Teclado", actualizado.getNombre());
        assertEquals(30.0, actualizado.getPrecio());
    }

    @Test
    void actualizarStock_salidaExitosa() {
        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Mouse");
        producto.setCantidad(20);

        StockUpdateRequest request = new StockUpdateRequest(5,"SALIDA", "Uso interno","sistema" );

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any())).thenReturn(producto);

        Producto result = productoService.actualizarStock(1L, request);

        assertEquals(15, result.getCantidad());
    }

    @Test
    void actualizarStock_salidaConStockInsuficiente_lanzaExcepcion() {
        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Mouse");
        producto.setCantidad(2);

        StockUpdateRequest request = new StockUpdateRequest(5,"SALIDA", "Uso interno","sistema" );

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        ProductoException.ValorInvalido ex = assertThrows(ProductoException.ValorInvalido.class,
                () -> productoService.actualizarStock(1L, request));

        assertTrue(ex.getMessage().contains("Stock insuficiente"));
    }

    @Test
    @Tag("critical")
    void eliminarProducto_exitoso() {
        Producto producto = new Producto();
        producto.setId(1L);

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        productoService.eliminarProducto(1L);

        verify(movimientoInventarioRepository).deleteByProductoId(1L);
        verify(productoRepository).delete(producto);
    }


    @Test
    void productoPorId_existente_devuelveProducto() {
        Producto producto = new Producto();
        producto.setId(1L);

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        Producto result = productoService.productoPorId(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void productoPorId_noExistente_lanzaExcepcion() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        ProductoException.NoEncontrado ex = assertThrows(ProductoException.NoEncontrado.class,
                () -> productoService.productoPorId(99L));

        assertTrue(ex.getMessage().contains("Producto no encontrado"));
    }

    @Test
    void obtenerTodosLosProductos_devuelveLista() {
        when(productoRepository.findAll()).thenReturn(List.of(new Producto(), new Producto()));

        List<Producto> productos = productoService.obtenerTodosLosProductos();

        assertEquals(2, productos.size());
    }

    @Test
    void obtenerProductosPaginados_devuelvePagina() {
        @SuppressWarnings("unchecked")
        Page<Producto> paginaMock = mock(Page.class);
        when(productoRepository.findAll(any(Pageable.class))).thenReturn(paginaMock);

        Pageable pageable = PageRequest.of(0, 5);
        Page<Producto> resultado = productoService.obtenerProductosPaginados(pageable);

        assertEquals(paginaMock, resultado);
        verify(productoRepository).findAll(pageable);
    }

    @Test
    void buscarProductos_devuelveResultados() {
        @SuppressWarnings("unchecked")
        Page<Producto> paginaMock = mock(Page.class);

        when(productoRepository.findAll(ArgumentMatchers.<Specification<Producto>>any(), any(Pageable.class)))
                .thenReturn(paginaMock);

        Page<Producto> resultado = productoService.buscarProductos("Mouse", "ELECTRONICA", 10.0, 50.0, "", Pageable.unpaged());

        assertEquals(paginaMock, resultado);
        verify(productoRepository).findAll(ArgumentMatchers.<Specification<Producto>>any(), any(Pageable.class));
    }
}
