package com.transporte.controller;

import com.transporte.model.Administrador;
import com.transporte.model.ReglaNegocioException;
import com.transporte.model.Usuario;
import java.util.List;

/**
 * Registro del backoffice: permite todos los roles, incluido Administrador, pero solo si quien lo pide
 * es un administrador con sesión iniciada (así un admin solo lo puede crear otro admin).
 */
public class RegistroAdmin extends RegistroUsuario {

    private final Usuario solicitante;

    public RegistroAdmin(LoginController login, Usuario solicitante) {
        super(login);
        this.solicitante = solicitante;
    }

    @Override
    public List<String> rolesPermitidos() { return LoginController.ROLES_REGISTRABLES; }

    @Override
    protected void verificarSolicitante() throws ReglaNegocioException {
        if (!(solicitante instanceof Administrador)) {
            throw new ReglaNegocioException("Solo un administrador puede registrar usuarios desde el backoffice.");
        }
    }
}