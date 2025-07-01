package org.example.stepdefinitions;

import io.cucumber.java.en.*;
import org.example.Entity.Categoria;
import org.example.Entity.Producto;
import org.example.Repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.Assert.*;

@SpringBootTest
@ActiveProfiles("test")
public class ProductoSteps {

    @Autowired
    private ProductoRepository productoRepository;

    private Producto productoCreado;
    private Producto productoExistente;
    private Exception excepcionCapturada;

    // ---------- CREACIÓN DE PRODUCTO ----------

    @Given("el inventario de productos está vacío")
    public void elInventarioDeProductosEstaVacio() {
        productoRepository.deleteAll();
    }

    @When("creo un producto con nombre {string}, descripcion {string}, categoria {string}, precio {double} y cantidad {int}")
    public void creoUnProducto(String nombre, String descripcion, String categoriaString, double precio, int cantidad) {
        try {
            Categoria categoriaEnum = Categoria.valueOf(categoriaString.toUpperCase());
            Producto producto = new Producto();
            producto.setNombre(nombre);
            producto.setDescripcion(descripcion);
            producto.setCategoria(categoriaEnum);
            producto.setPrecio((float) precio);
            producto.setCantidad(cantidad);
            productoCreado = productoRepository.save(producto);
        } catch (Exception e) {
            excepcionCapturada = e;
        }
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

    // ---------- EDICIÓN DE PRODUCTO ----------

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
        productoExistente = productoRepository.save(producto);
    }

    @When("actualizo el producto a nombre {string}, descripcion {string}, categoria {string}, precio {double} y cantidad {int}")
    public void actualizoElProducto(String nombre, String descripcion, String categoriaString, double precio, int cantidad) {
        try {
            Categoria categoriaEnum = Categoria.valueOf(categoriaString.toUpperCase());
            productoExistente.setNombre(nombre);
            productoExistente.setDescripcion(descripcion);
            productoExistente.setCategoria(categoriaEnum);
            productoExistente.setPrecio((float) precio);
            productoExistente.setCantidad(cantidad);
            productoExistente = productoRepository.save(productoExistente);
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

    // ---------- ELIMINACIÓN DE PRODUCTO ----------

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

    @Then("ocurre un error al eliminar el producto con el mensaje {string}")
    public void ocurreUnErrorAlEliminarProductoConMensaje(String mensajeEsperado) {
        assertNotNull(excepcionCapturada);
        assertEquals(mensajeEsperado, excepcionCapturada.getMessage());
    }
}
