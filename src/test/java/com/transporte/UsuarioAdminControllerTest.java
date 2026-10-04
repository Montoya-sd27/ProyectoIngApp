package com.transporte;

import static org.junit.jupiter.api.Assertions.*;

import com.transporte.controller.LoginController;
import com.transporte.controller.UsuarioAdminController;
import com.transporte.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Pruebas unitarias del backoffice de usuarios (registro hecho por el administrador). */
class UsuarioAdminControllerTest {

    private UsuarioAdminController backoffice;
    private LoginController login;

    @BeforeEach
    void setUp() {
        CargaDatosLogin.reiniciar();
        login = new LoginController();
        login.crearAdministradorPorDefecto();
        backoffice = new UsuarioAdminController(login);
    }

    // 1. Un usuario registrado desde el backoffice puede iniciar sesión después
    @Test
    void usuarioRegistradoEnBackofficePuedeIniciarSesion() throws ReglaNegocioException {
        backoffice.registrar("Ana", "Lopez", "V-1", "ana@ucv.ve", "clave", "Estudiante");

        Usuario u = login.iniciarSesion("ana@ucv.ve", "clave");

        assertNotNull(u, "El usuario creado por el administrador debe poder entrar");
        assertEquals("Ana", u.getNombre());
        assertEquals("Estudiante", u.getRol());
    }

    // 2. Cada rol registrable crea su subclase
    @Test
    void registraLosCuatroRolesConSuSubclase() throws ReglaNegocioException {
        backoffice.registrar("A", "A", "1", "a@ucv.ve", "x", "Estudiante");
        backoffice.registrar("B", "B", "2", "b@ucv.ve", "x", "Empleado");
        backoffice.registrar("C", "C", "3", "c@ucv.ve", "x", "Conductor");
        backoffice.registrar("D", "D", "4", "d@ucv.ve", "x", "Administrador");

        assertTrue(login.iniciarSesion("d@ucv.ve", "x") instanceof Administrador);
        assertTrue(login.iniciarSesion("a@ucv.ve", "x") instanceof Estudiante);
        assertTrue(login.iniciarSesion("b@ucv.ve", "x") instanceof Empleado);
        assertTrue(login.iniciarSesion("c@ucv.ve", "x") instanceof UsuarioConductor);
    }

    // 3. Campos vacíos o solo espacios
    @Test
    void rechazaCamposVacios() {
        assertThrows(ReglaNegocioException.class,
                () -> backoffice.registrar("", "Lopez", "V-1", "ana@ucv.ve", "clave", "Estudiante"));
        assertThrows(ReglaNegocioException.class,
                () -> backoffice.registrar("Ana", "Lopez", "   ", "ana@ucv.ve", "clave", "Estudiante"));
        assertThrows(ReglaNegocioException.class,
                () -> backoffice.registrar("Ana", "Lopez", "V-1", "ana@ucv.ve", "", "Estudiante"));
        assertThrows(ReglaNegocioException.class,
                () -> backoffice.registrar(null, null, null, null, null, "Estudiante"));
    }

    // 4. Correo con formato inválido
    @Test
    void rechazaCorreoConFormatoInvalido() {
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> backoffice.registrar("Ana", "Lopez", "V-1", "ana.ucv.ve", "clave", "Estudiante"));
        assertTrue(ex.getMessage().toLowerCase().contains("correo"));
        assertNull(login.iniciarSesion("ana.ucv.ve", "clave"));
    }

    // 5. Correo o cédula repetidos
    @Test
    void rechazaCorreoOCedulaRepetidos() throws ReglaNegocioException {
        backoffice.registrar("Ana", "Lopez", "V-1", "ana@ucv.ve", "clave", "Estudiante");

        assertThrows(ReglaNegocioException.class,
                () -> backoffice.registrar("Otra", "Persona", "V-2", "ANA@UCV.VE", "clave", "Empleado"));
        assertThrows(ReglaNegocioException.class,
                () -> backoffice.registrar("Otra", "Persona", "V-1", "otra@ucv.ve", "clave", "Empleado"));
        assertEquals(2, backoffice.listar().size(), "Solo el admin por defecto y Ana");
    }

    // 6. Un administrador nuevo creado desde el backoffice puede entrar; un rol inventado o vacío se rechaza
    @Test
    void registraAdministradorYRechazaRolesDesconocidos() throws ReglaNegocioException {
        backoffice.registrar("Pedro", "Perez", "V-5", "pedro@ucv.ve", "clave", "Administrador");
        Usuario nuevoAdmin = login.iniciarSesion("pedro@ucv.ve", "clave");
        assertTrue(nuevoAdmin instanceof Administrador, "El nuevo administrador debe poder iniciar sesion");

        assertThrows(ReglaNegocioException.class,
                () -> backoffice.registrar("Pedro", "Perez", "V-5", "pedro@ucv.ve", "clave", "Pasajero"));
        assertThrows(ReglaNegocioException.class,
                () -> backoffice.registrar("Pedro", "Perez", "V-5", "pedro@ucv.ve", "clave", null));
        assertEquals(2, backoffice.listar().size(), "Solo el admin por defecto y Pedro");
    }

    // 7. Se quitan los espacios sobrantes de nombre y correo
    @Test
    void recortaEspaciosDeLosDatos() throws ReglaNegocioException {
        Usuario creado = backoffice.registrar("  Luis ", " Mora ", " V-3 ", "  luis@ucv.ve ", "clave", "Conductor");

        assertEquals("Luis", creado.getNombre());
        assertEquals("luis@ucv.ve", creado.getCorreo());
        assertNotNull(login.iniciarSesion("luis@ucv.ve", "clave"));
    }

    // 8. El listado incluye al admin por defecto y a los nuevos, y no se puede modificar desde afuera
    @Test
    void listarMuestraLosUsuariosYEsDeSoloLectura() throws ReglaNegocioException {
        assertEquals(1, backoffice.listar().size(), "Al inicio solo existe el administrador");
        backoffice.registrar("Ana", "Lopez", "V-1", "ana@ucv.ve", "clave", "Estudiante");

        assertEquals(2, backoffice.listar().size());
        assertThrows(UnsupportedOperationException.class, () -> backoffice.listar().clear());
    }

    // 9. El formulario ofrece los cuatro roles
    @Test
    void rolesDisponiblesSonLosCuatroRoles() {
        assertEquals(java.util.List.of("Estudiante", "Empleado", "Conductor", "Administrador"), backoffice.rolesDisponibles());
    }
}
