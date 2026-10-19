package com.transporte;

import static org.junit.jupiter.api.Assertions.*;

import com.transporte.controller.LoginController;
import com.transporte.controller.RegistroPublicoController;
import com.transporte.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Pruebas del registro que se hace desde el login (sin iniciar sesión; sin rol Administrador). */
class RegistroPublicoControllerTest {

    private RegistroPublicoController registro;
    private LoginController login;

    @BeforeEach
    void setUp() {
        CargaDatosLogin.reiniciar();
        login = new LoginController();
        login.crearAdministradorPorDefecto();
        registro = new RegistroPublicoController(login);
    }

    // 1. Los tres roles públicos se registran, crean su subclase y pueden iniciar sesión
    @Test
    void registraEstudianteEmpleadoYConductorYPuedenEntrar() throws ReglaNegocioException {
        registro.registrar("Ana", "Lopez", "1000001", "ana@gmail.com", "clave123", "clave123", "Estudiante");
        registro.registrar("Beto", "Mora", "1000002", "beto@ucv.ve", "clave123", "clave123", "Empleado");
        registro.registrar("Carla", "Ruiz", "1000003", "carla@hotmail.com", "clave123", "clave123", "Conductor");

        assertTrue(login.iniciarSesion("ana@gmail.com", "clave123") instanceof Estudiante);
        assertTrue(login.iniciarSesion("beto@ucv.ve", "clave123") instanceof Empleado);
        assertTrue(login.iniciarSesion("carla@hotmail.com", "clave123") instanceof UsuarioConductor);
    }

    // 2. El rol Administrador no se ofrece ni se acepta: solo otro administrador puede crear un admin
    @Test
    void nadieSePuedeRegistrarComoAdministrador() {
        assertFalse(registro.rolesDisponibles().contains("Administrador"));
        assertEquals(java.util.List.of("Estudiante", "Empleado", "Conductor"), registro.rolesDisponibles());

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> registro.registrar("Pedro", "Perez", "1000005", "pedro@ucv.ve", "clave123", "clave123", "Administrador"));
        assertTrue(ex.getMessage().toLowerCase().contains("administrador"));
        assertNull(login.iniciarSesion("pedro@ucv.ve", "clave123"), "No debe haberse creado el usuario");
        assertEquals(1, CargaDatosLogin.listarUsuarios().size(), "Solo el administrador por defecto");
    }

    // 3. Un rol inventado o vacío tampoco se acepta
    @Test
    void rechazaRolesDesconocidos() {
        assertThrows(ReglaNegocioException.class,
                () -> registro.registrar("Luis", "Mora", "1000003", "luis@ucv.ve", "x", "x", "Pasajero"));
        assertThrows(ReglaNegocioException.class,
                () -> registro.registrar("Luis", "Mora", "1000003", "luis@ucv.ve", "x", "x", null));
        assertEquals(1, CargaDatosLogin.listarUsuarios().size());
    }

    // 4. Las contraseñas deben coincidir
    @Test
    void rechazaContrasenasQueNoCoinciden() {
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> registro.registrar("Ana", "Lopez", "1000001", "ana@ucv.ve", "clave123", "otra", "Estudiante"));
        assertTrue(ex.getMessage().toLowerCase().contains("contraseñas"));
        assertThrows(ReglaNegocioException.class,
                () -> registro.registrar("Ana", "Lopez", "1000001", "ana@ucv.ve", "clave123", null, "Estudiante"));
        assertNull(login.iniciarSesion("ana@ucv.ve", "clave123"));
    }

    // 5. Las mismas reglas del backoffice: nombre, apellido, cédula y correo
    @Test
    void aplicaLasMismasValidacionesQueElBackoffice() {
        assertThrows(ReglaNegocioException.class,
                () -> registro.registrar("An4", "Lopez", "1000001", "ana@ucv.ve", "x", "x", "Estudiante"));
        assertThrows(ReglaNegocioException.class,
                () -> registro.registrar("Ana", "Lopez!", "1000001", "ana@ucv.ve", "x", "x", "Estudiante"));
        assertThrows(ReglaNegocioException.class,
                () -> registro.registrar("Ana", "Lopez", "12345", "ana@ucv.ve", "x", "x", "Estudiante"));
        assertThrows(ReglaNegocioException.class,
                () -> registro.registrar("Ana", "Lopez", "123456789", "ana@ucv.ve", "x", "x", "Estudiante"));
        assertThrows(ReglaNegocioException.class,
                () -> registro.registrar("Ana", "Lopez", "1000001", "ana@ucv", "x", "x", "Estudiante"));
        assertThrows(ReglaNegocioException.class,
                () -> registro.registrar("", "", "", "", "", "", "Estudiante"));
        assertEquals(1, CargaDatosLogin.listarUsuarios().size());
    }

    // 6. Correo o cédula repetidos (también contra el administrador por defecto)
    @Test
    void rechazaCorreoOCedulaRepetidos() throws ReglaNegocioException {
        registro.registrar("Ana", "Lopez", "1000001", "ana@ucv.ve", "clave123", "clave123", "Estudiante");

        assertThrows(ReglaNegocioException.class,
                () -> registro.registrar("Otra", "Persona", "1000002", "ANA@ucv.ve", "x", "x", "Empleado"));
        assertThrows(ReglaNegocioException.class,
                () -> registro.registrar("Otra", "Persona", "1000001", "otra@ucv.ve", "x", "x", "Empleado"));
        assertThrows(ReglaNegocioException.class,
                () -> registro.registrar("Falso", "Admin", "1000009", LoginController.ADMIN_CORREO, "x", "x", "Estudiante"));
        assertEquals(2, CargaDatosLogin.listarUsuarios().size(), "Solo el admin por defecto y Ana");
    }
}