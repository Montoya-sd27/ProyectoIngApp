package com.transporte;

import static org.junit.jupiter.api.Assertions.*;

import com.transporte.controller.LoginController;
import com.transporte.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias del módulo de Login y Registro (TH-09: verificación de datos).
 * Las pruebas 1 a 4 son las escritas originalmente en TH09PruebaUnitariaVerificacionDeDatos.
 */
class LoginControllerTest {

    private LoginController controlador;

    @BeforeEach
    void setUp() {
        // La "base de datos" es estática: se vacía para que cada prueba sea independiente.
        CargaDatosLogin.reiniciar();
        // controlador que se prueba
        controlador = new LoginController();
    }

    // ===================== REGISTRO =====================

    // 1. Registrar un usuario nuevo
    @Test
    void registroExitosoDeUsuarioNuevo() {
        boolean resultado = controlador.registrarUsuario("Juan", "Perez", "V-12345678", "juan@ucv.ve", "password123", "Estudiante");
        assertTrue(resultado, "El registro deberia ser exitoso para un usuario nuevo");
    }

    // 2. Registrar exactamente el mismo usuario dos veces
    @Test
    void registroDuplicadoEsRechazado() {
        controlador.registrarUsuario("Maria", "Garcia", "V-87654321", "maria@ucv.ve", "pass123", "Empleado");
        boolean resultado = controlador.registrarUsuario("Maria", "Garcia", "V-87654321", "maria@ucv.ve", "pass123", "Empleado");
        assertFalse(resultado, "No deberia permitir registrar un usuario duplicado");
    }

    // 3. Mismo correo con otra cédula, y escrito con mayúsculas
    @Test
    void rechazaCorreoRepetidoAunqueCambieCedulaYMayusculas() {
        controlador.registrarUsuario("Juan", "Perez", "V-1", "juan@ucv.ve", "clave", "Estudiante");
        boolean resultado = controlador.registrarUsuario("Otro", "Usuario", "V-2", "JUAN@UCV.VE", "clave", "Estudiante");
        assertFalse(resultado, "El correo ya registrado no debe repetirse (sin distinguir mayusculas)");
    }

    // 4. Misma cédula con otro correo
    @Test
    void rechazaCedulaRepetidaConOtroCorreo() {
        controlador.registrarUsuario("Juan", "Perez", "V-1", "juan@ucv.ve", "clave", "Estudiante");
        boolean resultado = controlador.registrarUsuario("Otro", "Usuario", "V-1", "otro@ucv.ve", "clave", "Empleado");
        assertFalse(resultado, "La cedula ya registrada no debe repetirse");
    }

    // 5. Un rol que no existe no crea ningún usuario
    @Test
    void rolDesconocidoNoRegistraNada() {
        boolean resultado = controlador.registrarUsuario("Luis", "Mora", "V-3", "luis@ucv.ve", "clave", "Pasajero");
        assertFalse(resultado, "Un rol invalido no debe registrar al usuario");
        assertNull(controlador.iniciarSesion("luis@ucv.ve", "clave"), "El usuario rechazado no debe poder iniciar sesion");
    }

    // 6. Cada rol registrable crea su subclase y conserva su rol
    @Test
    void creaLaSubclaseCorrectaSegunElRol() {
        controlador.registrarUsuario("A", "A", "1", "a@ucv.ve", "x", "Estudiante");
        controlador.registrarUsuario("B", "B", "2", "b@ucv.ve", "x", "Empleado");
        controlador.registrarUsuario("C", "C", "3", "c@ucv.ve", "x", "Conductor");
        controlador.registrarUsuario("D", "D", "4", "d@ucv.ve", "x", "Administrador");

        assertTrue(controlador.iniciarSesion("a@ucv.ve", "x") instanceof Estudiante);
        assertTrue(controlador.iniciarSesion("b@ucv.ve", "x") instanceof Empleado);
        assertTrue(controlador.iniciarSesion("c@ucv.ve", "x") instanceof UsuarioConductor);
        assertTrue(controlador.iniciarSesion("d@ucv.ve", "x") instanceof Administrador);
        assertEquals("Conductor", controlador.iniciarSesion("c@ucv.ve", "x").getRol());
    }

    // 7. Se puede registrar un administrador (solo se llega aquí desde el backoffice) y puede iniciar sesión
    @Test
    void sePuedeRegistrarUnAdministrador() {
        boolean resultado = controlador.registrarUsuario("Pedro", "Perez", "V-5", "pedro@ucv.ve", "clave", "Administrador");

        assertTrue(resultado, "El rol Administrador debe poder registrarse");
        Usuario admin = controlador.iniciarSesion("pedro@ucv.ve", "clave");
        assertTrue(admin instanceof Administrador);
        assertEquals("Administrador", admin.getRol());
    }

    // ===================== INICIO DE SESIÓN =====================

    // 8. Datos correctos (TH-09 original, adaptada: ahora con un empleado)
    @Test
    void loginExitosoConCredencialesValidas() {
        controlador.registrarUsuario("Pedro", "Perez", "V-11111111", "pedro@ucv.ve", "claveSegura", "Empleado");

        Usuario usuario = controlador.iniciarSesion("pedro@ucv.ve", "claveSegura");

        assertNotNull(usuario, "El login deberia ser exitoso con credenciales validas");
        assertEquals("Empleado", usuario.getRol(), "El rol devuelto deberia ser Empleado");
        assertEquals("Pedro", usuario.getNombre());
    }

    // 9. El administrador por defecto puede iniciar sesión y no se duplica
    @Test
    void administradorPorDefectoIniciaSesionYNoSeDuplica() {
        controlador.crearAdministradorPorDefecto();
        controlador.crearAdministradorPorDefecto();   // llamarlo dos veces no lo duplica

        Usuario admin = controlador.iniciarSesion("admin@ucv.ve", "123");

        assertNotNull(admin, "El administrador por defecto debe poder iniciar sesion");
        assertTrue(admin instanceof Administrador);
        assertEquals("Administrador", admin.getRol());
        // nadie más puede tomar su correo (el correo es único)
        assertFalse(controlador.registrarUsuario("Falso", "Admin", "999", "admin@ucv.ve", "x", "Estudiante"));
    }

    // 10. Contraseña incorrecta (TH-09 original)
    @Test
    void loginFallaConContrasenaIncorrecta() {
        controlador.registrarUsuario("Ana", "Lopez", "V-22222222", "ana@ucv.ve", "miClave", "Estudiante");

        Usuario usuario = controlador.iniciarSesion("ana@ucv.ve", "claveIncorrecta");

        assertNull(usuario, "El login deberia fallar con contraseña incorrecta");
    }

    // 11. Correo que no está registrado
    @Test
    void loginFallaConCorreoNoRegistrado() {
        assertNull(controlador.iniciarSesion("nadie@ucv.ve", "clave"), "Un correo inexistente no debe iniciar sesion");
    }

    // 12. La contraseña distingue mayúsculas de minúsculas
    @Test
    void contrasenaDistingueMayusculas() {
        controlador.registrarUsuario("Ana", "Lopez", "V-22222222", "ana@ucv.ve", "claveSegura", "Estudiante");
        assertNull(controlador.iniciarSesion("ana@ucv.ve", "CLAVESEGURA"));
        assertNotNull(controlador.iniciarSesion("ana@ucv.ve", "claveSegura"));
    }

    // 13. El correo NO distingue mayúsculas de minúsculas
    @Test
    void correoNoDistingueMayusculasAlIniciarSesion() {
        controlador.registrarUsuario("Ana", "Lopez", "V-22222222", "ana@ucv.ve", "clave", "Estudiante");
        assertNotNull(controlador.iniciarSesion("ANA@UCV.VE", "clave"));
    }
}
