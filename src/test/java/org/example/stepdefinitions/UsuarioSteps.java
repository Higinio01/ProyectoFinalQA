package org.example.stepdefinitions;

import io.cucumber.java.en.*;
import org.example.Dtos.UsuarioDto;
import org.example.Entity.EstadoUsuario;
import org.example.Entity.Rol;
import org.example.Entity.RolNombre;
import org.example.Entity.Usuario;
import org.example.Repository.RolRepository;
import org.example.Repository.UsuarioRepository;
import org.example.Request.UsuarioRequest;
import org.example.Service.UsuarioService;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.UUID;

import static org.junit.Assert.*;

public class UsuarioSteps {

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;

    public UsuarioSteps(UsuarioService usuarioService,
                        UsuarioRepository usuarioRepository,
                        RolRepository rolRepository) {
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
    }

    private EstadoUsuario estado;
    private UsuarioDto usuarioCreado;
    private Exception exceptionCapturada;
    private Usuario usuarioExistente;

    @Given("existe un rol con nombre {string}")
    public void existeUnRolConNombre(String rolNombre) {
        RolNombre nombre = RolNombre.valueOf(rolNombre.toUpperCase());

        if (rolRepository.findByRolNombre(nombre).isEmpty()) {
            Rol rol = new Rol();
            rol.setRolNombre(nombre);
            rolRepository.save(rol);
        }
    }

    @Given("existe un rol con ID {long}")
    public void existeUnRolConId(Long id) {
        rolRepository.findById(id).orElseGet(() -> {
            Rol rol = new Rol();
            rol.setId(id);
            rol.setRolNombre(RolNombre.CLIENTE);
            return rolRepository.save(rol);
        });
    }

    @When("creo un usuario con nombre {string}, apellido {string}, email {string}, password {string} y rol {string}")
    public void creoUnUsuario(String nombre, String apellido, String email, String password, String rolNombre) {
        try {
            Rol rol = buscarRolPorNombre(rolNombre);
            String emailFinal = asegurarEmailUnico(email);
            usuarioCreado = usuarioService.crearUsuario(new UsuarioRequest(nombre, apellido, emailFinal, password, rol.getId()));
        } catch (Exception e) {
            exceptionCapturada = e;
        }
    }

    @When("creo un usuario con nombre {string}, apellido {string}, email {string}, password {string}, rol {string}")
    public void creoUnUsuarioAlternativo(String nombre, String apellido, String email, String password, String rolNombre) {
        creoUnUsuario(nombre, apellido, email, password, rolNombre);
    }

    @When("intento crear un usuario con nombre {string}, apellido {string}, email {string}, password {string} y rol {string}")
    public void intentoCrearUsuario(String nombre, String apellido, String email, String password, String rolNombre) {
        try {
            Rol rol = buscarRolPorNombre(rolNombre);
            usuarioCreado = usuarioService.crearUsuario(new UsuarioRequest(nombre, apellido, email, password, rol.getId()));
        } catch (Exception e) {
            exceptionCapturada = e;
        }
    }

    @When("intento crear un usuario con nombre {string}, apellido {string}, email {string}, password {string} y rol ID {long}")
    public void intentoCrearUsuarioConRolId(String nombre, String apellido, String email, String password, Long rolId) {
        try {
            usuarioCreado = usuarioService.crearUsuario(new UsuarioRequest(nombre, apellido, email, password, rolId));
        } catch (Exception e) {
            exceptionCapturada = e;
        }
    }

    @Given("existe un usuario con nombre {string}, apellido {string}, email {string}, password {string} y rol {string}")
    public void existeUnUsuario(String nombre, String apellido, String email, String password, String rolNombre) {
        Rol rol = buscarRolPorNombre(rolNombre);
        usuarioExistente = usuarioRepository.findByEmail(email).orElseGet(() ->
                usuarioRepository.save(crearUsuario(nombre, apellido, email, password, rol, EstadoUsuario.ACTIVO))
        );
    }

    @Given("existe un usuario con nombre {string}, apellido {string}, email {string}, password {string} y rol ID {long}")
    public void existeUnUsuarioConRolId(String nombre, String apellido, String email, String password, Long rolId) {
        Rol rol = rolRepository.findById(rolId).orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + rolId));
        usuarioExistente = usuarioRepository.findByEmail(email).orElseGet(() ->
                usuarioRepository.save(crearUsuario(nombre, apellido, email, password, rol, EstadoUsuario.ACTIVO))
        );
    }

    @When("actualizo el usuario con email {string} a nombre {string}, apellido {string}, email {string}, password {string} y rol {string}")
    public void actualizoElUsuario(String emailAntiguo, String nuevoNombre, String nuevoApellido, String nuevoEmail, String nuevaPassword, String rolNombre) {
        try {
            Long idUsuario = obtenerIdPorEmail(emailAntiguo);
            Rol rol = buscarRolPorNombre(rolNombre);
            usuarioService.actualizarUsuario(idUsuario, new UsuarioRequest(
                    nuevoNombre, nuevoApellido, asegurarEmailUnico(nuevoEmail), nuevaPassword, rol.getId()));
        } catch (Exception e) {
            exceptionCapturada = e;
        }
    }

    @When("elimino el usuario con email {string}")
    public void eliminoElUsuario(String email) {
        usuarioRepository.findByEmail(email)
                .ifPresentOrElse(u -> usuarioService.eliminarUsuario(u.getId()),
                        () -> exceptionCapturada = new RuntimeException("Usuario no encontrado"));
    }

    @When("intento eliminar el usuario con email {string}")
    public void intentoEliminarUsuarioInexistente(String email) {
        try {
            eliminoElUsuario(email);
        } catch (Exception e) {
            exceptionCapturada = e;
        }
    }

    @When("bloqueo el usuario con email {string}")
    public void bloqueoElUsuarioConEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado para bloqueo"));
        usuarioService.cambiarEstado(usuario.getId(), EstadoUsuario.BLOQUEADO);
    }

    @Then("el usuario con email {string} aparece en la lista de usuarios")
    public void elUsuarioApareceEnLaLista(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow();
        assertEquals(email, usuario.getEmail());
        assertEquals(EstadoUsuario.ACTIVO, usuario.getEstado());
        assertNotEquals("password123", usuario.getPassword());
    }

    @Then("el usuario con email {string} tiene nombre {string}")
    public void elUsuarioTieneNombre(String email, String nombreEsperado) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado tras actualización"));
        assertEquals(nombreEsperado, usuario.getNombre());
    }

    @Then("el usuario con email {string} tiene rol {string}")
    public void elUsuarioConEmailTieneRol(String email, String rolEsperado) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado al verificar rol"));
        assertEquals(rolEsperado.toUpperCase(), usuario.getRol().getRolNombre().name());
    }

    @Then("el usuario con email {string} tiene estado {string}")
    public void elUsuarioConEmailTieneEstado(String email, String estadoEsperado) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado al verificar estado"));
        assertEquals(EstadoUsuario.valueOf(estadoEsperado.toUpperCase()), usuario.getEstado());
    }

    @Then("el usuario con email {string} ya no existe en la base de datos")
    public void elUsuarioYaNoExiste(String email) {
        assertFalse(usuarioRepository.findByEmail(email).isPresent());
    }

    @Then("ocurre un error con el mensaje {string}")
    public void ocurreUnErrorConMensaje(String mensajeEsperado) {
        assertNotNull(exceptionCapturada);
        Throwable causa = exceptionCapturada;
        while (causa.getCause() != null) causa = causa.getCause();
        assertTrue(causa.getMessage().contains(mensajeEsperado));
    }

    private String asegurarEmailUnico(String email) {
        return usuarioRepository.existsByEmail(email)
                ? email.replace("@", "+" + UUID.randomUUID().toString().substring(0, 8) + "@")
                : email;
    }

    private Rol buscarRolPorNombre(String rolNombre) {
        return rolRepository.findAll().stream()
                .filter(r -> r.getRolNombre().name().equals(rolNombre.toUpperCase()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Rol no encontrado para pruebas"));
    }

    private Long obtenerIdPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"))
                .getId();
    }

    private Usuario crearUsuario(String nombre, String apellido, String email, String password, Rol rol, EstadoUsuario estado) {
        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        usuario.setEmail(email);
        usuario.setPassword(password);
        usuario.setRol(rol);
        usuario.setEstado(estado);
        return usuario;
    }

    @Given("existe un usuario con ID {long} y email {string}")
    public void existeUnUsuarioConIdYEmail(Long id, String email) {
        Rol rol = rolRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: 1"));

        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNombre("Nombre" + id);
        usuario.setApellido("Apellido" + id);
        usuario.setEmail(email);
        usuario.setPassword("password123");
        usuario.setRol(rol);
        usuario.setEstado(EstadoUsuario.ACTIVO);

        usuarioExistente = usuarioRepository.save(usuario);
    }

    @Given("existe otro usuario con ID {long} y email {string}")
    public void existeOtroUsuarioConIdYEmail(Long id, String email) {
        existeUnUsuarioConIdYEmail(id, email);
    }

    @When("intento actualizar el usuario con ID {long} a email {string}")
    public void intentoActualizarUsuarioConIdAEmail(Long id, String nuevoEmail) {
        try {
            Usuario usuario = usuarioRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id));

            UsuarioRequest request = new UsuarioRequest(
                    usuario.getNombre(),
                    usuario.getApellido(),
                    nuevoEmail,
                    "newPassword123",
                    usuario.getRol().getId()
            );

            usuarioService.actualizarUsuario(id, request);
        } catch (Exception e) {
            exceptionCapturada = e;
        }
    }

}
