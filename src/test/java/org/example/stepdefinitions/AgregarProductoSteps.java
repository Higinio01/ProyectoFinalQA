package org.example.stepdefinitions;

import io.cucumber.java.en.*;
import org.example.Entity.Producto;
import org.example.Repository.ProductoRepository;
import org.example.controller.ProductoController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@ActiveProfiles("test")
public class AgregarProductoSteps {

    @Autowired
    private ProductoRepository productoRepository;

    private Producto productoCreado;

    @Given("el inventario de productos está vacío")
    public void elInventarioDeProductosEstaVacio() {
        productoRepository.deleteAll();
    }

    @When("creo un producto con nombre {string}, descripcion {string}, categoria {string}, precio {double} y cantidad {int}")
    public void creoUnProducto(String nombre, String descripcion, String categoria, double precio, int cantidad) {
        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setDescripcion(descripcion);
        producto.setCategoria(categoria);
        producto.setPrecio((float) precio);
        producto.setCantidad(cantidad);
        productoCreado = productoRepository.save(producto);
    }

    @Then("el producto {string} aparece en la lista de productos")
    public void elProductoApareceEnLaLista(String nombre) {
        Optional<Producto> encontrado = productoRepository.findById(productoCreado.getId());
        assertTrue(encontrado.isPresent());
        assertEquals(nombre, encontrado.get().getNombre());
    }
}