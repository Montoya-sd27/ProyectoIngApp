package com.transporte.controller;

import com.transporte.model.CargaDatosLogin;
import com.transporte.model.ReglaNegocioException;
import com.transporte.model.Usuario;
import java.util.ArrayList;
import java.util.List;

/**
 * CONTROLADOR (MVC) del backoffice de usuarios: gestiona la creación y
 * visualización de usuarios desde la vista de administración.
 */
public class UsuarioAdminController {

    private final RegistroUsuario registro;

    public UsuarioAdminController(Usuario solicitante) {
        this.registro = new RegistroAdmin(new LoginController(), solicitante);
    }

    public List<String> rolesDisponibles() {
        return new ArrayList<>(registro.rolesPermitidos());
    }

    public void registrar(String nombre, String apellido, String cedula, String correo, String clave, String rol) throws ReglaNegocioException {
        registro.registrar(nombre, apellido, cedula, correo, clave, clave, rol);
    }

    public List<Usuario> listar() {
        return CargaDatosLogin.listarUsuarios();
    }
}
