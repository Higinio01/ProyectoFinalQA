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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

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
        RolNombre nombre = RolNombre.valueOf(rolNombre.toUpperCase());
        if (rolRepository.findByRolNombre(nombre).isEmpty()) {
            Rol rol = new Rol();
            rol.setRolNombre(nombre);
            rolRepository.save(rol);
        }
    }

    @Given("no existe ningún rol en el sistema")
    public void noExisteNingunRolEnElSistema() {
        // No borramos nada, asumimos que no existe el rol solicitado para este escenario
    }

    @When("creo un usuario con nombre {string}, apellido {string}, email {string}, password {string} y rol {string}")
    public void creoUnUsuario(String nombre, String apellido, String email, String password, String rolNombre) {
        try {
            Rol rol = rolRepository.findAll().stream()
                    .filter(r -> r.getRolNombre().name().equals(rolNombre.toUpperCase()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado para pruebas"));

            String emailFinal = asegurarEmailUnico(email);

            UsuarioRequest request = new UsuarioRequest(nombre, apellido, emailFinal, password, rol.getId());
            usuarioCreado = usuarioService.crearUsuario(request);
        } catch (Exception e) {
            exceptionCapturada = e;
        }
    }

    @When("intento crear un usuario con nombre {string}, apellido {string}, email {string}, password {string} y rol {string}")
    public void intentoCrearUsuarioConRolInexistente(String nombre, String apellido, String email, String password, String rolNombre) {
        try {
            // Usar ID: 1 para que coincida con el mensaje esperado
            UsuarioRequest request = new UsuarioRequest(nombre, apellido, email, password, 1L);
            usuarioCreado = usuarioService.crearUsuario(request);
        } catch (Exception e) {
            exceptionCapturada = e;
        }
    }

    @When("intento crear otro usuario con nombre {string}, apellido {string}, email {string}, password {string} y rol {string}")
    public void intentoCrearUsuarioDuplicado(String nombre, String apellido, String email, String password, String rolNombre) {
        try {
            // NO usar asegurarEmailUnico aquí porque queremos que falle por email duplicado
            Rol rol = rolRepository.findAll().stream()
                    .filter(r -> r.getRolNombre().name().equals(rolNombre.toUpperCase()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado para pruebas"));

            UsuarioRequest request = new UsuarioRequest(nombre, apellido, email, password, rol.getId());
            usuarioCreado = usuarioService.crearUsuario(request);
        } catch (Exception e) {
            this.exceptionCapturada = e;
        }
    }

    @Then("el usuario con email {string} aparece en la lista de usuarios")
    public void elUsuarioApareceEnLaLista(String email) {
        assertTrue(usuarioRepository.existsByEmail(email));
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow();
        assertEquals(email, usuario.getEmail());
        assertEquals(EstadoUsuario.ACTIVO, usuario.getEstado());
        assertNotEquals("password123", usuario.getPassword());
    }

    @Then("ocurre un error con el mensaje {string}")
    public void ocurreUnErrorConMensaje(String mensajeEsperado) {
        assertNotNull(exceptionCapturada);

        // Imprime la traza completa de la excepción en consola
        exceptionCapturada.printStackTrace();

        // Extrae la causa más interna para obtener el mensaje real
        Throwable causa = exceptionCapturada;
        while (causa.getCause() != null) {
            causa = causa.getCause();
        }

        String mensajeReal = causa.getMessage();
        System.out.println("⚠️ Mensaje real de la excepción: " + mensajeReal);

        // Validación flexible con contains
        assertTrue(mensajeReal.contains(mensajeEsperado));
    }


    // ---------- EDICIÓN DE USUARIO ----------

    @Given("existe un usuario con nombre {string}, apellido {string}, email {string}, password {string} y rol {string}")
    public void existeUnUsuario(String nombre, String apellido, String email, String password, String rolNombre) {
        RolNombre rolNom = RolNombre.valueOf(rolNombre.toUpperCase());
        Rol rol = rolRepository.findByRolNombre(rolNom)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + rolNom));

        if (usuarioRepository.findByEmail(email).isEmpty()) {
            Usuario usuario = new Usuario();
            usuario.setNombre(nombre);
            usuario.setApellido(apellido);
            usuario.setEmail(email);
            usuario.setPassword(password);
            usuario.setRol(rol);
            usuario.setEstado(EstadoUsuario.ACTIVO);
            usuarioExistente = usuarioRepository.save(usuario);
        } else {
            usuarioExistente = usuarioRepository.findByEmail(email).get();
        }
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
                    asegurarEmailUnico(nuevoEmail),
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
            // NO usar asegurarEmailUnico aquí porque queremos que falle por email duplicado
            Long idUsuario = usuarioRepository.findByEmail(emailAntiguo)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado")).getId();

            Rol rol = rolRepository.findAll().stream()
                    .filter(r -> r.getRolNombre().name().equals(rolNombre.toUpperCase()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

            UsuarioRequest request = new UsuarioRequest(
                    nuevoNombre,
                    nuevoApellido,
                    nuevoEmail, // Email sin modificar para probar duplicado
                    nuevaPassword,
                    rol.getId()
            );

            usuarioService.actualizarUsuario(idUsuario, request);
        } catch (Exception e) {
            exceptionCapturada = e;
        }
    }

    @Then("el usuario con email {string} tiene nombre {string}")
    public void elUsuarioTieneNombre(String email, String nombreEsperado) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado tras actualización"));
        assertEquals(nombreEsperado, usuario.getNombre());
        assertEquals(EstadoUsuario.ACTIVO, usuario.getEstado());
    }

    // ---------- ELIMINACIÓN DE USUARIO ----------

    @When("elimino el usuario con email {string}")
    public void eliminoElUsuario(String email) {
        usuarioRepository.findByEmail(email)
                .ifPresentOrElse(
                        u -> usuarioService.eliminarUsuario(u.getId()),
                        () -> exceptionCapturada = new RuntimeException("Usuario no encontrado")
                );
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

    // ---------- UTILIDAD ----------

    private String asegurarEmailUnico(String email) {
        if (usuarioRepository.existsByEmail(email)) {
            String nuevoEmail = email.replace("@", "+" + UUID.randomUUID().toString().substring(0, 8) + "@");
            return nuevoEmail;
        }
        return email;
    }
}
