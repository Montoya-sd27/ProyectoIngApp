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
<<<<<<< HEAD
        backoffice = new UsuarioAdminController(login, CargaDatosLogin.buscarPorCorreo(LoginController.ADMIN_CORREO));
=======
        backoffice = new UsuarioAdminController(login);
>>>>>>> origin/main
    }

    // 1. Un usuario registrado desde el backoffice puede iniciar sesión después
    @Test
    void usuarioRegistradoEnBackofficePuedeIniciarSesion() throws ReglaNegocioException {
<<<<<<< HEAD
        backoffice.registrar("Ana", "Lopez", "1000001", "ana@ucv.ve", "clave", "Estudiante");
=======
        backoffice.registrar("Ana", "Lopez", "V-1", "ana@ucv.ve", "clave", "Estudiante");
>>>>>>> origin/main

        Usuario u = login.iniciarSesion("ana@ucv.ve", "clave");

        assertNotNull(u, "El usuario creado por el administrador debe poder entrar");
        assertEquals("Ana", u.getNombre());
        assertEquals("Estudiante", u.getRol());
    }

    // 2. Cada rol registrable crea su subclase
    @Test
    void registraLosCuatroRolesConSuSubclase() throws ReglaNegocioException {
<<<<<<< HEAD
        backoffice.registrar("Ana", "Lopez", "1000001", "a@ucv.ve", "x", "Estudiante");
        backoffice.registrar("Beto", "Mora", "1000002", "b@ucv.ve", "x", "Empleado");
        backoffice.registrar("Carla", "Ruiz", "1000003", "c@ucv.ve", "x", "Conductor");
        backoffice.registrar("Daniel", "Paz", "1000004", "d@ucv.ve", "x", "Administrador");
=======
        backoffice.registrar("A", "A", "1", "a@ucv.ve", "x", "Estudiante");
        backoffice.registrar("B", "B", "2", "b@ucv.ve", "x", "Empleado");
        backoffice.registrar("C", "C", "3", "c@ucv.ve", "x", "Conductor");
        backoffice.registrar("D", "D", "4", "d@ucv.ve", "x", "Administrador");
>>>>>>> origin/main

        assertTrue(login.iniciarSesion("d@ucv.ve", "x") instanceof Administrador);
        assertTrue(login.iniciarSesion("a@ucv.ve", "x") instanceof Estudiante);
        assertTrue(login.iniciarSesion("b@ucv.ve", "x") instanceof Empleado);
        assertTrue(login.iniciarSesion("c@ucv.ve", "x") instanceof UsuarioConductor);
    }

    // 3. Campos vacíos o solo espacios
    @Test
    void rechazaCamposVacios() {
        assertThrows(ReglaNegocioException.class,
<<<<<<< HEAD
                () -> backoffice.registrar("", "Lopez", "1000001", "ana@ucv.ve", "clave", "Estudiante"));
        assertThrows(ReglaNegocioException.class,
                () -> backoffice.registrar("Ana", "Lopez", "   ", "ana@ucv.ve", "clave", "Estudiante"));
        assertThrows(ReglaNegocioException.class,
                () -> backoffice.registrar("Ana", "Lopez", "1000001", "ana@ucv.ve", "", "Estudiante"));
=======
                () -> backoffice.registrar("", "Lopez", "V-1", "ana@ucv.ve", "clave", "Estudiante"));
        assertThrows(ReglaNegocioException.class,
                () -> backoffice.registrar("Ana", "Lopez", "   ", "ana@ucv.ve", "clave", "Estudiante"));
        assertThrows(ReglaNegocioException.class,
                () -> backoffice.registrar("Ana", "Lopez", "V-1", "ana@ucv.ve", "", "Estudiante"));
>>>>>>> origin/main
        assertThrows(ReglaNegocioException.class,
                () -> backoffice.registrar(null, null, null, null, null, "Estudiante"));
    }

    // 4. Correo con formato inválido
    @Test
    void rechazaCorreoConFormatoInvalido() {
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
<<<<<<< HEAD
                () -> backoffice.registrar("Ana", "Lopez", "1000001", "ana.ucv.ve", "clave", "Estudiante"));
=======
                () -> backoffice.registrar("Ana", "Lopez", "V-1", "ana.ucv.ve", "clave", "Estudiante"));
>>>>>>> origin/main
        assertTrue(ex.getMessage().toLowerCase().contains("correo"));
        assertNull(login.iniciarSesion("ana.ucv.ve", "clave"));
    }

<<<<<<< HEAD
    // 4b. Cualquier correo usable se acepta (no solo @ucv.ve) y los inusables se rechazan
    @Test
    void validaCualquierCorreoUsable() throws ReglaNegocioException {
        backoffice.registrar("Ana", "Lopez", "1000001", "ana.lopez@gmail.com", "x", "Estudiante");
        backoffice.registrar("Beto", "Mora", "1000002", "beto+uni@correo.edu.ve", "x", "Empleado");
        backoffice.registrar("Carla", "Ruiz", "1000003", "carla_r@hotmail.com", "x", "Conductor");

        String[] invalidos = {"a@b", "a@b.c", "ana@@ucv.ve", "a..b@ucv.ve", ".ana@ucv.ve", "ana.@ucv.ve",
                "ana lopez@ucv.ve", "ana@-ucv.ve", "ana@ucv..ve", "ana@ucv.ve.", "ana@ucv.v3", "@ucv.ve", "ana@",
                "ana|x@ucv.ve"};
        int cedula = 2000000;
        for (String correo : invalidos) {
            final String c = correo;
            final String ced = String.valueOf(cedula++);
            assertThrows(ReglaNegocioException.class,
                    () -> backoffice.registrar("Pedro", "Perez", ced, c, "x", "Estudiante"),
                    "No debería aceptar el correo: " + c);
        }
    }

    // 5. Correo o cédula repetidos
    @Test
    void rechazaCorreoOCedulaRepetidos() throws ReglaNegocioException {
        backoffice.registrar("Ana", "Lopez", "1000001", "ana@ucv.ve", "clave", "Estudiante");

        assertThrows(ReglaNegocioException.class,
                () -> backoffice.registrar("Otra", "Persona", "1000002", "ANA@UCV.VE", "clave", "Empleado"));
        assertThrows(ReglaNegocioException.class,
                () -> backoffice.registrar("Otra", "Persona", "1000001", "otra@ucv.ve", "clave", "Empleado"));
=======
    // 5. Correo o cédula repetidos
    @Test
    void rechazaCorreoOCedulaRepetidos() throws ReglaNegocioException {
        backoffice.registrar("Ana", "Lopez", "V-1", "ana@ucv.ve", "clave", "Estudiante");

        assertThrows(ReglaNegocioException.class,
                () -> backoffice.registrar("Otra", "Persona", "V-2", "ANA@UCV.VE", "clave", "Empleado"));
        assertThrows(ReglaNegocioException.class,
                () -> backoffice.registrar("Otra", "Persona", "V-1", "otra@ucv.ve", "clave", "Empleado"));
>>>>>>> origin/main
        assertEquals(2, backoffice.listar().size(), "Solo el admin por defecto y Ana");
    }

    // 6. Un administrador nuevo creado desde el backoffice puede entrar; un rol inventado o vacío se rechaza
    @Test
    void registraAdministradorYRechazaRolesDesconocidos() throws ReglaNegocioException {
<<<<<<< HEAD
        backoffice.registrar("Pedro", "Perez", "1000005", "pedro@ucv.ve", "clave", "Administrador");
=======
        backoffice.registrar("Pedro", "Perez", "V-5", "pedro@ucv.ve", "clave", "Administrador");
>>>>>>> origin/main
        Usuario nuevoAdmin = login.iniciarSesion("pedro@ucv.ve", "clave");
        assertTrue(nuevoAdmin instanceof Administrador, "El nuevo administrador debe poder iniciar sesion");

        assertThrows(ReglaNegocioException.class,
<<<<<<< HEAD
                () -> backoffice.registrar("Pedro", "Perez", "1000005", "pedro@ucv.ve", "clave", "Pasajero"));
        assertThrows(ReglaNegocioException.class,
                () -> backoffice.registrar("Pedro", "Perez", "1000005", "pedro@ucv.ve", "clave", null));
        assertEquals(2, backoffice.listar().size(), "Solo el admin por defecto y Pedro");
    }

    // 7. Se quitan los espacios sobrantes de nombre, cédula y correo
    @Test
    void recortaEspaciosDeLosDatos() throws ReglaNegocioException {
        Usuario creado = backoffice.registrar("  Luis  Alberto ", " Mora ", " 1000003 ", "  luis@ucv.ve ", "clave", "Conductor");

        assertEquals("Luis Alberto", creado.getNombre());
        assertEquals("1000003", creado.getCedula());
=======
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
>>>>>>> origin/main
        assertEquals("luis@ucv.ve", creado.getCorreo());
        assertNotNull(login.iniciarSesion("luis@ucv.ve", "clave"));
    }

    // 8. El listado incluye al admin por defecto y a los nuevos, y no se puede modificar desde afuera
    @Test
    void listarMuestraLosUsuariosYEsDeSoloLectura() throws ReglaNegocioException {
        assertEquals(1, backoffice.listar().size(), "Al inicio solo existe el administrador");
<<<<<<< HEAD
        backoffice.registrar("Ana", "Lopez", "1000001", "ana@ucv.ve", "clave", "Estudiante");
=======
        backoffice.registrar("Ana", "Lopez", "V-1", "ana@ucv.ve", "clave", "Estudiante");
>>>>>>> origin/main

        assertEquals(2, backoffice.listar().size());
        assertThrows(UnsupportedOperationException.class, () -> backoffice.listar().clear());
    }

    // 9. El formulario ofrece los cuatro roles
    @Test
    void rolesDisponiblesSonLosCuatroRoles() {
        assertEquals(java.util.List.of("Estudiante", "Empleado", "Conductor", "Administrador"), backoffice.rolesDisponibles());
    }
<<<<<<< HEAD

    // 10. Nombre y apellido: solo letras (con tildes, ñ y espacios entre palabras)
    @Test
    void nombreYApellidoSoloAceptanLetras() throws ReglaNegocioException {
        Usuario u = backoffice.registrar("María José", "Núñez Peña", "1000001", "maria@ucv.ve", "x", "Estudiante");
        assertEquals("María José", u.getNombre());
        assertEquals("Núñez Peña", u.getApellido());

        String[] invalidos = {"Ana1", "4na", "Ana_", "Ana-Lopez", "Ana.", "A", "Ana@", "123", "Ana|Perez",
                "x".repeat(41)};
        int cedula = 2000000;
        for (String texto : invalidos) {
            final String t = texto;
            final String ced = String.valueOf(cedula++);
            assertThrows(ReglaNegocioException.class,
                    () -> backoffice.registrar(t, "Lopez", ced, "n" + ced + "@ucv.ve", "x", "Estudiante"),
                    "No debería aceptar el nombre: " + t);
            final String ced2 = String.valueOf(cedula++);
            assertThrows(ReglaNegocioException.class,
                    () -> backoffice.registrar("Ana", t, ced2, "a" + ced2 + "@ucv.ve", "x", "Estudiante"),
                    "No debería aceptar el apellido: " + t);
        }
        assertEquals(2, backoffice.listar().size(), "Solo el admin por defecto y María");
    }

    // 11. Rango de cédula: solo números, de 6 a 8 dígitos y sin empezar por 0
    @Test
    void cedulaDebeEstarEnElRango() throws ReglaNegocioException {
        backoffice.registrar("Ana", "Lopez", "100000", "min@ucv.ve", "x", "Estudiante");        // 6 dígitos (mínimo)
        backoffice.registrar("Beto", "Mora", "99999999", "max@ucv.ve", "x", "Empleado");        // 8 dígitos (máximo)

        String[] invalidas = {"12345", "123456789", "V-1234567", "12.345.678", "12a45678", "012345678", "0123456",
                "1234 5678", "-1234567"};
        int n = 0;
        for (String cedula : invalidas) {
            final String c = cedula;
            final String correo = "c" + (n++) + "@ucv.ve";
            assertThrows(ReglaNegocioException.class,
                    () -> backoffice.registrar("Pedro", "Perez", c, correo, "x", "Estudiante"),
                    "No debería aceptar la cédula: " + c);
        }
        assertEquals(3, backoffice.listar().size(), "Solo el admin por defecto, Ana y Beto");
    }

        // 12. Solo un administrador con sesión iniciada puede usar el backoffice (y por tanto crear otro admin)
    @Test
    void soloUnAdministradorPuedeRegistrarDesdeElBackoffice() {
        Usuario estudiante = new Estudiante("Ana", "Lopez", "1000001", "ana@ucv.ve", "x");
        UsuarioAdminController comoEstudiante = new UsuarioAdminController(login, estudiante);
        UsuarioAdminController sinSesion = new UsuarioAdminController(login, null);

        assertThrows(ReglaNegocioException.class,
                () -> comoEstudiante.registrar("Beto", "Mora", "1000002", "beto@ucv.ve", "x", "Administrador"));
        assertThrows(ReglaNegocioException.class,
                () -> sinSesion.registrar("Beto", "Mora", "1000002", "beto@ucv.ve", "x", "Administrador"));
        assertEquals(1, backoffice.listar().size(), "Solo el administrador por defecto");
    }
}
=======
}
>>>>>>> origin/main
