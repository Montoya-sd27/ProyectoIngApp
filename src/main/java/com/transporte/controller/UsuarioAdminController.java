package com.transporte.controller;

import com.transporte.model.CargaDatosLogin;
import com.transporte.model.ReglaNegocioException;
import com.transporte.model.Usuario;
import java.util.List;
import java.util.regex.Pattern;

/**
 * CONTROLADOR (MVC) del backoffice de usuarios: valida los datos que escribe el administrador
 * y reutiliza {@link LoginController} para guardar. Como el login lee de la misma fuente
 * ({@link CargaDatosLogin}), el usuario creado aquí puede iniciar sesión de inmediato.
 */
public class UsuarioAdminController {

    private static final Pattern CORREO = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final LoginController login;

    public UsuarioAdminController() {
        this(new LoginController());
    }

    public UsuarioAdminController(LoginController login) {
        this.login = login;
    }

    public List<String> rolesDisponibles() {
        return LoginController.ROLES_REGISTRABLES;
    }

    public List<Usuario> listar() {
        return CargaDatosLogin.listarUsuarios();
    }

    /** Registra un usuario nuevo; si algún dato no es válido lanza una excepción con el motivo. */
    public Usuario registrar(String nombre, String apellido, String cedula,
                             String correo, String contrasena, String rol) throws ReglaNegocioException {
        nombre = limpiar(nombre);
        apellido = limpiar(apellido);
        cedula = limpiar(cedula);
        correo = limpiar(correo);
        contrasena = contrasena == null ? "" : contrasena;

        if (nombre.isEmpty() || apellido.isEmpty() || cedula.isEmpty() || correo.isEmpty() || contrasena.isEmpty()) {
            throw new ReglaNegocioException("Por favor, llene todos los campos.");
        }
        if (!CORREO.matcher(correo).matches()) {
            throw new ReglaNegocioException("El correo no tiene un formato válido (ejemplo: nombre@ucv.ve).");
        }
        if (rol == null || !LoginController.ROLES_REGISTRABLES.contains(rol)) {
            throw new ReglaNegocioException("Rol no permitido: " + rol + ". Solo se registran "
                    + String.join(", ", LoginController.ROLES_REGISTRABLES) + ".");
        }
        if (!login.registrarUsuario(nombre, apellido, cedula, correo, contrasena, rol)) {
            throw new ReglaNegocioException("El correo o la cédula ya están registrados.");
        }
        return CargaDatosLogin.buscarPorCorreo(correo);
    }

    private static String limpiar(String texto) {
        return texto == null ? "" : texto.trim();
    }
}
