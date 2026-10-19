package com.transporte;

import com.transporte.controller.LoginController;
import com.transporte.controller.UsuarioAdminController;
import com.transporte.model.Administrador;
import com.transporte.model.CargaDatosLogin;
import com.transporte.model.Empleado;
import com.transporte.model.Estudiante;
import com.transporte.model.ReglaNegocioException;
import com.transporte.model.Usuario;
import com.transporte.model.UsuarioConductor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UsuarioAdminControllerTest {

    private UsuarioAdminController backoffice;
    private LoginController login;

    @BeforeEach
    void setUp() {
        CargaDatosLogin.reiniciar();
        login = new LoginController();
        login.crearAdministradorPorDefecto();
        Usuario adminDefecto = CargaDatosLogin.buscarPorCorreo(LoginController.ADMIN_CORREO);
        backoffice = new UsuarioAdminController(adminDefecto);
    }

    @Test
    void usuarioRegistradoEnBackofficeSeGuarda() throws Exception {
        backoffice.registrar("Ana", "Lopez", "1000001", "ana@ucv.ve", "clave1234", "Estudiante");
        assertEquals(2, backoffice.listar().size());
        assertNotNull(CargaDatosLogin.buscarPorCorreo("ana@ucv.ve"));
    }

    @Test
    void rolesDisponiblesDevuelveLista() {
        List<String> roles = backoffice.rolesDisponibles();
        assertNotNull(roles);
        assertTrue(roles.contains("Estudiante"));
        assertTrue(roles.contains("Administrador"));
    }

    @Test
    void listarDevuelveListaDeUsuarios() {
        List<Usuario> lista = backoffice.listar();
        assertNotNull(lista);
    }

    @Test
    void registraTodosLosRolesYPuedenIniciarSesion() throws Exception {
        backoffice.registrar("Ana", "Lopez", "1000001", "ana@ucv.ve", "clave1234", "Estudiante");
        backoffice.registrar("Beto", "Mora", "1000002", "beto@ucv.ve", "clave1234", "Empleado");
        backoffice.registrar("Carla", "Ruiz", "1000003", "carla@ucv.ve", "clave1234", "Conductor");
        backoffice.registrar("Diego", "Paz", "1000004", "diego@ucv.ve", "clave1234", "Administrador");

        assertTrue(login.iniciarSesion("ana@ucv.ve", "clave1234") instanceof Estudiante);
        assertTrue(login.iniciarSesion("beto@ucv.ve", "clave1234") instanceof Empleado);
        assertTrue(login.iniciarSesion("carla@ucv.ve", "clave1234") instanceof UsuarioConductor);
        assertTrue(login.iniciarSesion("diego@ucv.ve", "clave1234") instanceof Administrador);
    }

    @Test
    void unAdministradorNuevoPuedeRegistrarOtrosUsuarios() throws Exception {
        backoffice.registrar("Diego", "Paz", "1000004", "diego@ucv.ve", "clave1234", "Administrador");
        Usuario nuevoAdmin = login.iniciarSesion("diego@ucv.ve", "clave1234");
        UsuarioAdminController otro = new UsuarioAdminController(nuevoAdmin);

        otro.registrar("Eva", "Sol", "1000005", "eva@ucv.ve", "clave1234", "Estudiante");

        assertNotNull(login.iniciarSesion("eva@ucv.ve", "clave1234"));
    }

    @Test
    void noSePermiteCorreoOCedulaRepetidos() throws Exception {
        backoffice.registrar("Ana", "Lopez", "1000001", "ana@ucv.ve", "clave1234", "Estudiante");

        assertThrows(ReglaNegocioException.class,
                () -> backoffice.registrar("Otra", "Lopez", "1000009", "ana@ucv.ve", "clave1234", "Empleado"));
        assertThrows(ReglaNegocioException.class,
                () -> backoffice.registrar("Otra", "Lopez", "1000001", "otra@ucv.ve", "clave1234", "Empleado"));
    }

    @Test
    void soloUnAdministradorPuedeRegistrar() {
        Usuario estudiante = new Estudiante("Ana", "Lopez", "1000001", "ana@ucv.ve", "clave1234");
        UsuarioAdminController sinPermiso = new UsuarioAdminController(estudiante);

        assertThrows(ReglaNegocioException.class,
                () -> sinPermiso.registrar("Beto", "Mora", "1000002", "beto@ucv.ve", "clave1234", "Empleado"));
        assertNull(CargaDatosLogin.buscarPorCorreo("beto@ucv.ve"));
    }
}
