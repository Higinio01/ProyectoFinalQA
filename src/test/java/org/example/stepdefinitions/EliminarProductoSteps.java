package org.example.stepdefinitions;

import io.cucumber.java.en.*;
import org.example.Entity.Producto;
import org.example.Repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.Assert.assertFalse;

@SpringBootTest
@ActiveProfiles("test")
public class EliminarProductoSteps {

    @Autowired
    private ProductoRepository productoRepository;

    private Producto productoExistente;

    @Given("existe un producto para eliminar con nombre {string}, descripcion {string}, categoria {string}, precio {double} y cantidad {int}")
    public void existeUnProductoParaEliminar(String nombre, String descripcion, String categoria, double precio, int cantidad) {
        productoRepository.deleteAll();
        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setDescripcion(descripcion);
        producto.setCategoria(categoria);
        producto.setPrecio((float) precio);
        producto.setCantidad(cantidad);
        productoExistente = productoRepository.save(producto);
    }

    @When("elimino el producto")
    public void eliminoElProducto() {
        productoRepository.deleteById(productoExistente.getId());
    }

    @Then("el producto ya no existe en la base de datos")
    public void elProductoYaNoExiste() {
        Optional<Producto> encontrado = productoRepository.findById(productoExistente.getId());
        assertFalse(encontrado.isPresent());
    }
}
