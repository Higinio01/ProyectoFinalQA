package org.example.stepdefinitions;

import io.cucumber.java.en.*;
import org.example.Entity.Producto;
import org.example.Repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
public class EditarProductoSteps {

    @Autowired
    private ProductoRepository productoRepository;

    private Producto productoExistente;

    @Given("existe un producto con nombre {string}, descripcion {string}, categoria {string}, precio {double} y cantidad {int}")
    public void existeUnProducto(String nombre, String descripcion, String categoria, double precio, int cantidad) {
        productoRepository.deleteAll();
        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setDescripcion(descripcion);
        producto.setCategoria(categoria);
        producto.setPrecio((float) precio);
        producto.setCantidad(cantidad);
        productoExistente = productoRepository.save(producto);
    }

    @When("actualizo el producto a nombre {string}, descripcion {string}, categoria {string}, precio {double} y cantidad {int}")
    public void actualizoElProducto(String nombre, String descripcion, String categoria, double precio, int cantidad) {
        productoExistente.setNombre(nombre);
        productoExistente.setDescripcion(descripcion);
        productoExistente.setCategoria(categoria);
        productoExistente.setPrecio((float) precio);
        productoExistente.setCantidad(cantidad);
        productoExistente = productoRepository.save(productoExistente);
    }

    @Then("el producto tiene nombre {string} y cantidad {int}")
    public void elProductoTieneNombreYCantidad(String nombre, int cantidad) {
        Optional<Producto> encontrado = productoRepository.findById(productoExistente.getId());
        assertTrue(encontrado.isPresent());
        assertEquals(nombre, encontrado.get().getNombre());
        assertEquals(Integer.valueOf(cantidad), encontrado.get().getCantidad());
    }
}
