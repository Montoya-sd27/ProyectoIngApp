package com.transporte.controller;

import com.transporte.model.CargaDatosLogin;
import com.transporte.model.ReglaNegocioException;
import com.transporte.model.Usuario;
import java.util.List;

//Controlador del backoffice de usuarios: delega el registro en RegistroAdmin.
//Esta pantalla solo la abre un administrador, por eso aquí sí se puede crear otro administrador.
public class UsuarioAdminController {

    private final RegistroUsuario registro;

    //@param solicitante el usuario con sesión iniciada que usa el backoffice (debe ser Administrador). 
    public UsuarioAdminController(Usuario solicitante) {
        this(new LoginController(), solicitante);
    }

    // Inicializa el servicio de registro pasándole el admin actual para validar permisos
    public UsuarioAdminController(LoginController login, Usuario solicitante) {
        this.registro = new RegistroAdmin(login, solicitante);
    }

    // Devuelve todos los roles posibles, incluyendo el rol de Administrador
    public List<String> rolesDisponibles() {
        return registro.rolesPermitidos();
    }

    // Obtiene la lista global de usuarios registrados en el sistema
    public List<Usuario> listar() {
        return CargaDatosLogin.listarUsuarios();
    }

    // Registra un nuevo usuario desde el módulo admin (sin pedir confirmación de clave)
    public Usuario registrar(String nombre, String apellido, String cedula,
                             String correo, String contrasena, String rol) throws ReglaNegocioException {
        // el administrador no confirma contraseña: se pasa null y RegistroAdmin no lo exige
        return registro.registrar(nombre, apellido, cedula, correo, contrasena, null, rol);
    }
}