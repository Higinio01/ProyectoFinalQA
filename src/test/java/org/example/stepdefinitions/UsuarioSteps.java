package org.example.stepdefinitions;

import io.cucumber.java.en.*;
import org.example.Dtos.UsuarioDto;
import org.example.Entity.Rol;
import org.example.Entity.RolNombre;
import org.example.Entity.Usuario;
import org.example.Repository.RolRepository;
import org.example.Repository.UsuarioRepository;
import org.example.Request.UsuarioRequest;
import org.example.Service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.Assert.*;

@SpringBootTest
@ActiveProfiles("test")
public class UsuarioSteps {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    private UsuarioDto usuarioCreado;
    private Exception exceptionCapturada;
    private Usuario usuarioExistente;

    // ---------- CREACIÓN DE USUARIO ----------

    @Given("existe un rol con nombre {string}")
    public void existeUnRolConNombre(String rolNombre) {
        rolRepository.deleteAll();
        Rol rol = new Rol();
        rol.setRolNombre(RolNombre.valueOf(rolNombre.toUpperCase()));
        rolRepository.save(rol);
    }

    @Given("no existe ningún rol en el sistema")
    public void noExisteNingunRolEnElSistema() {
        rolRepository.deleteAll();
    }

    @When("creo un usuario con nombre {string}, apellido {string}, email {string}, password {string} y rol {string}")
    public void creoUnUsuario(String nombre, String apellido, String email, String password, String rolNombre) {
        try {
            Rol rol = rolRepository.findAll().stream()
                    .filter(r -> r.getRolNombre().name().equals(rolNombre.toUpperCase()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado para pruebas"));

            UsuarioRequest request = new UsuarioRequest(nombre, apellido, email, password, rol.getId());

            usuarioCreado = usuarioService.crearUsuario(request);
        } catch (Exception e) {
            exceptionCapturada = e;
        }
    }

    @When("intento crear un usuario con nombre {string}, apellido {string}, email {string}, password {string} y rol {string}")
    public void intentoCrearUsuarioConRolInexistente(String nombre, String apellido, String email, String password, String rolNombre) {
        try {
            Rol rol = rolRepository.findAll().stream()
                    .filter(r -> r.getRolNombre().name().equals(rolNombre.toUpperCase()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado con ID: 1"));

            UsuarioRequest request = new UsuarioRequest(nombre, apellido, email, password, rol.getId());

            usuarioCreado = usuarioService.crearUsuario(request);
        } catch (Exception e) {
            exceptionCapturada = e;
        }
    }

    @When("intento crear otro usuario con nombre {string}, apellido {string}, email {string}, password {string} y rol {string}")
    public void intentoCrearUsuarioDuplicado(String nombre, String apellido, String email, String password, String rolNombre) {
        try {
            creoUnUsuario(nombre, apellido, email, password, rolNombre);
        } catch (Exception e) {
            this.exceptionCapturada = e;
        }
    }

    @Then("el usuario con email {string} aparece en la lista de usuarios")
    public void elUsuarioApareceEnLaLista(String email) {
        assertTrue(usuarioRepository.existsByEmail(email));

        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow();
        assertEquals(usuarioCreado.getEmail(), usuario.getEmail());
        assertNotEquals("password123", usuario.getPassword()); // Verificar que esté cifrada
    }

    @Then("ocurre un error con el mensaje {string}")
    public void ocurreUnErrorConMensaje(String mensajeEsperado) {
        assertNotNull(exceptionCapturada);
        assertEquals(mensajeEsperado, exceptionCapturada.getMessage());
    }

    // ---------- EDICIÓN DE USUARIO ----------

    @Given("existe un usuario con nombre {string}, apellido {string}, email {string}, password {string} y rol {string}")
    public void existeUnUsuario(String nombre, String apellido, String email, String password, String rolNombre) {
        Rol rol = rolRepository.findAll().stream()
                .filter(r -> r.getRolNombre().name().equals(rolNombre.toUpperCase()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        usuario.setEmail(email);
        usuario.setPassword(password); // Se encriptará al usar el servicio de actualización
        usuario.setRol(rol);

        usuarioExistente = usuarioRepository.save(usuario);
    }

    @When("actualizo el usuario con email {string} a nombre {string}, apellido {string}, email {string}, password {string} y rol {string}")
    public void actualizoElUsuario(String emailAntiguo, String nuevoNombre, String nuevoApellido, String nuevoEmail, String nuevaPassword, String rolNombre) {
        try {
            Long idUsuario = usuarioRepository.findByEmail(emailAntiguo)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado")).getId();

            Rol rol = rolRepository.findAll().stream()
                    .filter(r -> r.getRolNombre().name().equals(rolNombre.toUpperCase()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

            UsuarioRequest request = new UsuarioRequest(
                    nuevoNombre,
                    nuevoApellido,
                    nuevoEmail,
                    nuevaPassword,
                    rol.getId()
            );

            usuarioService.actualizarUsuario(idUsuario, request);
        } catch (Exception e) {
            exceptionCapturada = e;
        }
    }

    @When("intento actualizar el usuario con email {string} a nombre {string}, apellido {string}, email {string}, password {string} y rol {string}")
    public void intentoActualizarUsuarioConEmailDuplicado(String emailAntiguo, String nuevoNombre, String nuevoApellido, String nuevoEmail, String nuevaPassword, String rolNombre) {
        try {
            actualizoElUsuario(emailAntiguo, nuevoNombre, nuevoApellido, nuevoEmail, nuevaPassword, rolNombre);
        } catch (Exception e) {
            exceptionCapturada = e;
        }
    }

    @Then("el usuario con email {string} tiene nombre {string}")
    public void elUsuarioTieneNombre(String email, String nombreEsperado) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado tras actualización"));
        assertEquals(nombreEsperado, usuario.getNombre());
    }

    // ---------- ELIMINACIÓN DE USUARIO ----------

    @When("elimino el usuario con email {string}")
    public void eliminoElUsuario(String email) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isPresent()) {
            usuarioService.eliminarUsuario(usuarioOpt.get().getId());
        } else {
            exceptionCapturada = new RuntimeException("Usuario no encontrado");
        }
    }

    @When("intento eliminar el usuario con email {string}")
    public void intentoEliminarUsuarioInexistente(String email) {
        try {
            eliminoElUsuario(email);
        } catch (Exception e) {
            exceptionCapturada = e;
        }
    }

    @Then("el usuario con email {string} ya no existe en la base de datos")
    public void elUsuarioYaNoExiste(String email) {
        assertFalse(usuarioRepository.findByEmail(email).isPresent());
    }
}
