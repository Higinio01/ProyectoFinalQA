package org.example.stepdefinitions;

import io.cucumber.java.en.*;
import org.example.Dtos.ProductoDto;
import org.example.Entity.Categoria;
import org.example.Entity.Producto;
import org.example.Repository.ProductoRepository;
import org.example.Request.ProductoRequest;
import org.example.Service.ProductoService;
import org.modelmapper.ModelMapper;

import java.util.Optional;

import static org.junit.Assert.*;

public class ProductoSteps {

    private final ProductoRepository productoRepository;
    private final ProductoService productoService;
    private final ModelMapper modelMapper;

    public ProductoSteps(ProductoRepository productoRepository, ProductoService productoService, ModelMapper modelMapper) {
        this.productoRepository = productoRepository;
        this.productoService = productoService;
        this.modelMapper = modelMapper;
    }

    private ProductoDto productoCreado;
    private ProductoDto productoObtenido;
    private Producto productoExistente;
    private Exception excepcionCapturada;

    @Given("el inventario de productos está vacío")
    public void elInventarioDeProductosEstaVacio() {
        productoRepository.deleteAll();
    }

    @When("creo un producto con nombre {string}, descripcion {string}, categoria {string}, precio {double} y cantidad {int}")
    public void creoUnProducto(String nombre, String descripcion, String categoriaString, double precio, int cantidad) {
        try {
            // ✅ CORREGIDO: Agregado minimoStock (línea 41)
            ProductoRequest request = new ProductoRequest(nombre, descripcion, categoriaString, (float) precio, cantidad, 5);
            // Asignar el resultado para evitar NPE
            Producto producto = productoService.crearProducto(request);
            productoCreado = modelMapper.map(producto, ProductoDto.class);
        } catch (Exception e) {
            excepcionCapturada = e;
        }
    }

    @When("solicito el producto por su ID")
    public void solicitoElProductoPorSuID() {
        try {
            Producto producto = productoService.productoPorId(productoExistente.getId());
            productoObtenido = modelMapper.map(producto, ProductoDto.class);
        } catch (Exception e) {
            excepcionCapturada = e;
        }
    }

    @Then("el producto obtenido tiene nombre {string}")
    public void elProductoObtenidoTieneNombre(String nombreEsperado) {
        assertNotNull(productoObtenido);
        assertEquals(nombreEsperado, productoObtenido.getNombre());
    }

    @Then("el producto {string} aparece en la lista de productos")
    public void elProductoApareceEnLaLista(String nombre) {
        Optional<Producto> encontrado = productoRepository.findById(productoCreado.getId());
        assertTrue(encontrado.isPresent());
        assertEquals(nombre, encontrado.get().getNombre());
    }

    @Then("ocurre un error al crear el producto con el mensaje {string}")
    public void ocurreUnErrorAlCrearProductoConMensaje(String mensajeEsperado) {
        assertNotNull(excepcionCapturada);
        assertEquals(mensajeEsperado, excepcionCapturada.getMessage());
    }

    @Given("existe un producto con nombre {string}, descripcion {string}, categoria {string}, precio {double} y cantidad {int}")
    public void existeUnProducto(String nombre, String descripcion, String categoriaString, double precio, int cantidad) {
        productoRepository.deleteAll();
        Categoria categoriaEnum = Categoria.valueOf(categoriaString.toUpperCase());
        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setDescripcion(descripcion);
        producto.setCategoria(categoriaEnum);
        producto.setPrecio((float) precio);
        producto.setCantidad(cantidad);
        // ✅ También necesitas setear minimoStock aquí
        producto.setMinimoStock(3); // valor por defecto para productos existentes
        productoExistente = productoRepository.save(producto);
    }

    @When("actualizo el producto a nombre {string}, descripcion {string}, categoria {string}, precio {double} y cantidad {int}")
    public void actualizoElProducto(String nombre, String descripcion, String categoriaString, double precio, int cantidad) {
        try {
            // ✅ CORREGIDO: Agregado minimoStock (línea 95)
            ProductoRequest request = new ProductoRequest(
                    nombre,
                    descripcion,
                    categoriaString,
                    (float) precio,
                    cantidad,
                    3  // minimoStock
            );
            productoService.actualizarProducto(productoExistente.getId(), request);
        } catch (Exception e) {
            excepcionCapturada = e;
        }
    }

    @Then("el producto tiene nombre {string} y cantidad {int}")
    public void elProductoTieneNombreYCantidad(String nombre, int cantidad) {
        Optional<Producto> encontrado = productoRepository.findById(productoExistente.getId());
        assertTrue(encontrado.isPresent());
        assertEquals(nombre, encontrado.get().getNombre());
        assertEquals(Integer.valueOf(cantidad), encontrado.get().getCantidad());
    }

    @Then("ocurre un error al actualizar el producto con el mensaje {string}")
    public void ocurreUnErrorAlActualizarProductoConMensaje(String mensajeEsperado) {
        assertNotNull(excepcionCapturada);
        assertEquals(mensajeEsperado, excepcionCapturada.getMessage());
    }

    @Given("existe un producto para eliminar con nombre {string}, descripcion {string}, categoria {string}, precio {double} y cantidad {int}")
    public void existeUnProductoParaEliminar(String nombre, String descripcion, String categoriaString, double precio, int cantidad) {
        productoRepository.deleteAll();
        Categoria categoriaEnum = Categoria.valueOf(categoriaString.toUpperCase());
        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setDescripcion(descripcion);
        producto.setCategoria(categoriaEnum);
        producto.setPrecio((float) precio);
        producto.setCantidad(cantidad);
        // ✅ También necesitas setear minimoStock aquí
        producto.setMinimoStock(3); // valor por defecto para productos a eliminar
        productoExistente = productoRepository.save(producto);
    }

    @When("elimino el producto")
    public void eliminoElProducto() {
        try {
            productoRepository.deleteById(productoExistente.getId());
        } catch (Exception e) {
            excepcionCapturada = e;
        }
    }

    @When("intento eliminar un producto inexistente")
    public void intentoEliminarProductoInexistente() {
        try {
            productoRepository.deleteById(-1L); // ID inválido
        } catch (Exception e) {
            excepcionCapturada = e;
        }
    }

    @Then("el producto ya no existe en la base de datos")
    public void elProductoYaNoExiste() {
        Optional<Producto> encontrado = productoRepository.findById(productoExistente.getId());
        assertFalse(encontrado.isPresent());
    }

    @Then("el producto no existe y no ocurre error")
    public void elProductoNoExisteYNoOcurreError() {
        assertNull(excepcionCapturada);
    }

    @When("solicito el producto con ID {long}")
    public void solicitoElProductoConID(Long id) {
        try {
            Producto producto = productoService.productoPorId(id);
            productoObtenido = modelMapper.map(producto, ProductoDto.class);
        } catch (Exception e) {
            excepcionCapturada = e;
        }
    }

    @Then("ocurre un error al obtener el producto con el mensaje {string}")
    public void ocurreUnErrorAlObtenerProductoConMensaje(String mensajeEsperado) {
        assertNotNull(excepcionCapturada);

        Throwable causa = excepcionCapturada;
        while (causa.getCause() != null) {
            causa = causa.getCause();
        }

        String mensajeReal = causa.getMessage();
        System.out.println("⚠️ Mensaje real de la excepción: " + mensajeReal);

        assertEquals(mensajeEsperado, mensajeReal);
    }

}