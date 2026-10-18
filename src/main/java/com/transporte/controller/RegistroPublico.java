package com.transporte.controller;

import com.transporte.model.ReglaNegocioException;
import java.util.List;

/** Registro desde el login: sin Administrador y con confirmación de contraseña. */
public class RegistroPublico extends RegistroUsuario {

    public RegistroPublico(LoginController login) { super(login); }

    @Override
    public List<String> rolesPermitidos() { return LoginController.ROLES_PUBLICOS; }

    @Override
    protected void verificarConfirmacion(String contrasena, String confirmacion) throws ReglaNegocioException {
        if (!contrasena.equals(confirmacion == null ? "" : confirmacion)) {
            throw new ReglaNegocioException("Las contraseñas no coinciden.");
        }
    }

    @Override
    protected String mensajeRolNoPermitido(String rol) {
        return "Administrador".equals(rol)
                ? "El rol Administrador solo puede ser creado por otro administrador."
                : "Seleccione un rol válido: " + String.join(", ", rolesPermitidos()) + ".";
    }
}