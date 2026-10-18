package com.transporte.controller;

import com.transporte.model.*;
import java.util.List;

// validamos si el usuario puede entrar o registrarse.
public class LoginController {

    /** Roles que se pueden crear desde el backoffice (solo un administrador puede llegar a esa pantalla). */
    public static final List<String> ROLES_REGISTRABLES = List.of("Estudiante", "Empleado", "Conductor", "Administrador");

    /**
     * Roles que cualquier persona puede crear por su cuenta desde el login. "Administrador" NO está aquí:
     * el único que puede crear un usuario administrador es otro administrador (desde el backoffice).
     */
    public static final List<String> ROLES_PUBLICOS = List.of("Estudiante", "Empleado", "Conductor");

    /** Credenciales del administrador por defecto (el primero, para poder entrar la primera vez). */
    public static final String ADMIN_CORREO = "admin@ucv.ve";
    public static final String ADMIN_CLAVE = "123";

    // iniciar sesión
    public Usuario iniciarSesion(String correo, String contrasena) {
        // usuario existe
        Usuario usuarioEncontrado = CargaDatosLogin.buscarPorCorreo(correo);

        // si existe, verificamos que la contraseña sea correcta
        if (usuarioEncontrado != null && usuarioEncontrado.getContrasena().equals(contrasena)) {
            return usuarioEncontrado;
        }

        return null;
    }

    // registrar usuario
    public boolean registrarUsuario(String nombre, String apellido, String cedula, String correo, String contrasena, String rol) {

        if (CargaDatosLogin.existeUsuario(correo, cedula)) {
            return false; // ya existe
        }

        // la fábrica decide qué subclase crear según el rol
        Usuario nuevoUsuario = UsuarioFactory.crear(rol, nombre, apellido, cedula, correo, contrasena);
        if (nuevoUsuario == null) {
            return false; // rol desconocido
        }

        CargaDatosLogin.agregarUsuario(nuevoUsuario);
        return true;
    }

    /** Crea el administrador por defecto si todavía no existe (llamarlo varias veces no lo duplica). */
    public void crearAdministradorPorDefecto() {
        if (CargaDatosLogin.buscarPorCorreo(ADMIN_CORREO) == null) {
            CargaDatosLogin.agregarUsuario(new Administrador("Admin", "Root", "123", ADMIN_CORREO, ADMIN_CLAVE));
        }
    }
}