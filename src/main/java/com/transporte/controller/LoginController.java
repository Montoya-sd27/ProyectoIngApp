package com.transporte.controller;

import com.transporte.model.*;
import java.util.List;

// validamos si el usuario puede entrar o registrarse.
public class LoginController {

    /** Roles que se pueden crear desde el backoffice (solo un administrador puede llegar a esa pantalla). */
    public static final List<String> ROLES_REGISTRABLES = List.of("Estudiante", "Empleado", "Conductor", "Administrador");

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

        // creamos según el rol que eligió 
        Usuario nuevoUsuario = null;
        
        if ("Estudiante".equals(rol)) {
            nuevoUsuario = new Estudiante(nombre, apellido, cedula, correo, contrasena);
        } else if ("Empleado".equals(rol)) {
            nuevoUsuario = new Empleado(nombre, apellido, cedula, correo, contrasena);
        } else if ("Conductor".equals(rol)) {
            nuevoUsuario = new UsuarioConductor(nombre, apellido, cedula, correo, contrasena);
        } else if ("Administrador".equals(rol)) {
            nuevoUsuario = new Administrador(nombre, apellido, cedula, correo, contrasena);
        }

        // sii se creó lo guardamos
        if (nuevoUsuario != null) {
            CargaDatosLogin.agregarUsuario(nuevoUsuario);
            return true; 
        }
        
        return false; 
    }

    /** Crea el administrador por defecto si todavía no existe (llamarlo varias veces no lo duplica). */
    public void crearAdministradorPorDefecto() {
        if (CargaDatosLogin.buscarPorCorreo(ADMIN_CORREO) == null) {
            CargaDatosLogin.agregarUsuario(new Administrador("Admin", "Root", "123", ADMIN_CORREO, ADMIN_CLAVE));
        }
    }
}
